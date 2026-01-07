package com.hegemonia.nations.service

import com.hegemonia.core.database.DatabaseManager
import com.hegemonia.nations.dao.NationTables.Territories
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Représente un territoire/région
 */
data class Territory(
    val regionId: String,
    val nationId: Int?,
    val claimedAt: Instant?,
    val isCapital: Boolean,
    val development: Int,
    val fortification: Int,
    val population: Int
)

/**
 * Service de gestion des territoires
 */
class TerritoryService(private val db: DatabaseManager) {

    // Cache des territoires par région
    private val territoryCache = ConcurrentHashMap<String, Territory>()

    // Cache des territoires par nation
    private val nationTerritories = ConcurrentHashMap<Int, MutableSet<String>>()

    /**
     * Récupère un territoire par son ID de région
     */
    fun getTerritory(regionId: String): Territory? {
        territoryCache[regionId]?.let { return it }

        return db.transaction {
            Territories.select { Territories.regionId eq regionId }
                .singleOrNull()
                ?.toTerritory()
                ?.also { territoryCache[regionId] = it }
        }
    }

    /**
     * Récupère tous les territoires d'une nation
     */
    fun getNationTerritories(nationId: Int): List<Territory> {
        return db.transaction {
            Territories.select { Territories.nationId eq nationId }
                .map { it.toTerritory() }
                .also { territories ->
                    nationTerritories[nationId] = territories.map { it.regionId }.toMutableSet()
                    territories.forEach { territoryCache[it.regionId] = it }
                }
        }
    }

    /**
     * Revendique un territoire pour une nation
     */
    fun claimTerritory(regionId: String, nationId: Int): Result<Territory> {
        val existing = getTerritory(regionId)

        if (existing?.nationId != null && existing.nationId != nationId) {
            return Result.failure(IllegalStateException("Ce territoire appartient déjà à une autre nation"))
        }

        val now = Instant.now()

        return db.transaction {
            Territories.upsert {
                it[Territories.regionId] = regionId
                it[Territories.nationId] = nationId
                it[claimedAt] = now
                it[isCapital] = false
                it[development] = 0
                it[fortification] = 0
                it[population] = 0
            }

            val territory = Territory(
                regionId = regionId,
                nationId = nationId,
                claimedAt = now,
                isCapital = false,
                development = 0,
                fortification = 0,
                population = 0
            )

            // Mettre à jour le cache
            territoryCache[regionId] = territory
            nationTerritories.getOrPut(nationId) { mutableSetOf() }.add(regionId)

            Result.success(territory)
        }
    }

    /**
     * Abandonne un territoire
     */
    fun unclaimTerritory(regionId: String): Boolean {
        val territory = getTerritory(regionId) ?: return false
        val nationId = territory.nationId ?: return false

        return db.transaction {
            Territories.update({ Territories.regionId eq regionId }) {
                it[Territories.nationId] = null
                it[claimedAt] = null
                it[isCapital] = false
            } > 0
        }.also { success ->
            if (success) {
                territoryCache[regionId] = territory.copy(nationId = null, claimedAt = null, isCapital = false)
                nationTerritories[nationId]?.remove(regionId)
            }
        }
    }

    /**
     * Définit la capitale d'une nation
     */
    fun setCapital(regionId: String, nationId: Int): Boolean {
        return db.transaction {
            // Enlever l'ancienne capitale
            Territories.update({
                (Territories.nationId eq nationId) and (Territories.isCapital eq true)
            }) {
                it[isCapital] = false
            }

            // Définir la nouvelle capitale
            Territories.update({
                (Territories.regionId eq regionId) and (Territories.nationId eq nationId)
            }) {
                it[isCapital] = true
            } > 0
        }
    }

    /**
     * Vérifie si une région est revendiquée
     */
    fun isClaimed(regionId: String): Boolean {
        return getTerritory(regionId)?.nationId != null
    }

    /**
     * Récupère la nation propriétaire d'un territoire
     */
    fun getOwner(regionId: String): Int? {
        return getTerritory(regionId)?.nationId
    }

    /**
     * Compte les territoires d'une nation
     */
    fun countTerritories(nationId: Int): Int {
        return nationTerritories[nationId]?.size ?: db.transaction {
            Territories.select { Territories.nationId eq nationId }.count().toInt()
        }
    }

    /**
     * Augmente le développement d'un territoire
     */
    fun addDevelopment(regionId: String, amount: Int): Boolean {
        return db.transaction {
            Territories.update({ Territories.regionId eq regionId }) {
                with(SqlExpressionBuilder) {
                    it[development] = development + amount
                }
            } > 0
        }.also { success ->
            if (success) {
                territoryCache[regionId]?.let {
                    territoryCache[regionId] = it.copy(development = it.development + amount)
                }
            }
        }
    }

    /**
     * Augmente la fortification d'un territoire
     */
    fun addFortification(regionId: String, amount: Int): Boolean {
        return db.transaction {
            Territories.update({ Territories.regionId eq regionId }) {
                with(SqlExpressionBuilder) {
                    it[fortification] = fortification + amount
                }
            } > 0
        }.also { success ->
            if (success) {
                territoryCache[regionId]?.let {
                    territoryCache[regionId] = it.copy(fortification = it.fortification + amount)
                }
            }
        }
    }

    /**
     * Transfère un territoire d'une nation à une autre
     */
    fun transferTerritory(regionId: String, fromNationId: Int, toNationId: Int): Boolean {
        val territory = getTerritory(regionId) ?: return false
        if (territory.nationId != fromNationId) return false

        val now = Instant.now()

        return db.transaction {
            Territories.update({ Territories.regionId eq regionId }) {
                it[nationId] = toNationId
                it[claimedAt] = now
                it[isCapital] = false  // La capitale ne peut pas être transférée
            } > 0
        }.also { success ->
            if (success) {
                territoryCache[regionId] = territory.copy(
                    nationId = toNationId,
                    claimedAt = now,
                    isCapital = false
                )
                nationTerritories[fromNationId]?.remove(regionId)
                nationTerritories.getOrPut(toNationId) { mutableSetOf() }.add(regionId)
            }
        }
    }

    /**
     * Convertit un ResultRow en Territory
     */
    private fun ResultRow.toTerritory(): Territory {
        return Territory(
            regionId = this[Territories.regionId],
            nationId = this[Territories.nationId],
            claimedAt = this[Territories.claimedAt],
            isCapital = this[Territories.isCapital],
            development = this[Territories.development],
            fortification = this[Territories.fortification],
            population = this[Territories.population]
        )
    }
}
