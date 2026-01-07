package com.hegemonia.war.service

import com.hegemonia.core.database.DatabaseManager
import com.hegemonia.war.dao.WarTables.Sieges
import com.hegemonia.war.model.Siege
import com.hegemonia.war.model.SiegeEquipment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.concurrent.ConcurrentHashMap

/**
 * Service de gestion des sièges
 */
class SiegeService(
    private val db: DatabaseManager,
    private val battleService: BattleService
) {
    // Cache des sièges actifs
    private val activeSieges = ConcurrentHashMap<Int, Siege>()

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Crée un siège pour une bataille
     */
    fun createSiege(
        battleId: Int,
        fortificationLevel: Int,
        wallsHealth: Int = 1000,
        gatesHealth: Int = 500
    ): Siege? {
        val battle = battleService.getBattle(battleId) ?: return null

        return db.transaction {
            Sieges.insert {
                it[Sieges.battleId] = battleId
                it[Sieges.fortificationLevel] = fortificationLevel
                it[Sieges.wallsHealth] = wallsHealth
                it[maxWallsHealth] = wallsHealth
                it[Sieges.gatesHealth] = gatesHealth
                it[maxGatesHealth] = gatesHealth
                it[siegeProgress] = 0
                it[siegeEquipment] = "[]"
            }

            val siege = Siege(
                battleId = battleId,
                fortificationLevel = fortificationLevel,
                wallsHealth = wallsHealth,
                maxWallsHealth = wallsHealth,
                gatesHealth = gatesHealth,
                maxGatesHealth = gatesHealth,
                siegeProgress = 0,
                siegeEquipmentUsed = emptyList()
            )

            activeSieges[battleId] = siege
            siege
        }
    }

    /**
     * Récupère un siège
     */
    fun getSiege(battleId: Int): Siege? {
        activeSieges[battleId]?.let { return it }

        return db.transaction {
            Sieges.select { Sieges.battleId eq battleId }
                .singleOrNull()
                ?.toSiege()
                ?.also { activeSieges[battleId] = it }
        }
    }

    /**
     * Applique des dégâts aux murs
     */
    fun damageWalls(battleId: Int, damage: Int): Boolean {
        val siege = getSiege(battleId) ?: return false

        val newHealth = (siege.wallsHealth - damage).coerceAtLeast(0)

        return db.transaction {
            Sieges.update({ Sieges.battleId eq battleId }) {
                it[wallsHealth] = newHealth
            }
            true
        }.also { success ->
            if (success) {
                activeSieges[battleId] = siege.copy(wallsHealth = newHealth)
            }
        }
    }

    /**
     * Applique des dégâts aux portes
     */
    fun damageGates(battleId: Int, damage: Int): Boolean {
        val siege = getSiege(battleId) ?: return false

        val newHealth = (siege.gatesHealth - damage).coerceAtLeast(0)

        return db.transaction {
            Sieges.update({ Sieges.battleId eq battleId }) {
                it[gatesHealth] = newHealth
            }
            true
        }.also { success ->
            if (success) {
                activeSieges[battleId] = siege.copy(gatesHealth = newHealth)
            }
        }
    }

    /**
     * Utilise un équipement de siège
     */
    fun useEquipment(battleId: Int, equipment: SiegeEquipment): Boolean {
        val siege = getSiege(battleId) ?: return false

        // Appliquer les dégâts
        if (equipment.damageToWalls > 0) {
            damageWalls(battleId, equipment.damageToWalls)
        }
        if (equipment.damageToGates > 0) {
            damageGates(battleId, equipment.damageToGates)
        }

        // Enregistrer l'utilisation
        val updatedEquipment = siege.siegeEquipmentUsed + equipment

        return db.transaction {
            Sieges.update({ Sieges.battleId eq battleId }) {
                it[siegeEquipment] = json.encodeToString(updatedEquipment.map { e -> e.name })
            }
            true
        }.also { success ->
            if (success) {
                activeSieges[battleId] = siege.copy(siegeEquipmentUsed = updatedEquipment)
            }
        }
    }

    /**
     * Met à jour la progression du siège
     */
    fun updateProgress(battleId: Int, progressChange: Int): Boolean {
        val siege = getSiege(battleId) ?: return false

        val newProgress = (siege.siegeProgress + progressChange).coerceIn(0, 100)

        return db.transaction {
            Sieges.update({ Sieges.battleId eq battleId }) {
                it[siegeProgress] = newProgress
            }
            true
        }.also { success ->
            if (success) {
                activeSieges[battleId] = siege.copy(siegeProgress = newProgress)
            }
        }
    }

    /**
     * Répare les murs (défenseur)
     */
    fun repairWalls(battleId: Int, amount: Int): Boolean {
        val siege = getSiege(battleId) ?: return false

        val newHealth = (siege.wallsHealth + amount).coerceAtMost(siege.maxWallsHealth)

        return db.transaction {
            Sieges.update({ Sieges.battleId eq battleId }) {
                it[wallsHealth] = newHealth
            }
            true
        }.also { success ->
            if (success) {
                activeSieges[battleId] = siege.copy(wallsHealth = newHealth)
            }
        }
    }

    /**
     * Répare les portes (défenseur)
     */
    fun repairGates(battleId: Int, amount: Int): Boolean {
        val siege = getSiege(battleId) ?: return false

        val newHealth = (siege.gatesHealth + amount).coerceAtMost(siege.maxGatesHealth)

        return db.transaction {
            Sieges.update({ Sieges.battleId eq battleId }) {
                it[gatesHealth] = newHealth
            }
            true
        }.also { success ->
            if (success) {
                activeSieges[battleId] = siege.copy(gatesHealth = newHealth)
            }
        }
    }

    /**
     * Vérifie si le siège est réussi (brèche ouverte)
     */
    fun isBreached(battleId: Int): Boolean {
        val siege = getSiege(battleId) ?: return false
        return siege.isBreached
    }

    /**
     * Vérifie si le siège est complet (100% progression)
     */
    fun isComplete(battleId: Int): Boolean {
        val siege = getSiege(battleId) ?: return false
        return siege.siegeProgress >= 100
    }

    /**
     * Calcule le bonus défensif basé sur les fortifications
     */
    fun getDefenseBonus(battleId: Int): Double {
        val siege = getSiege(battleId) ?: return 0.0

        // Bonus de base par niveau de fortification
        val baseBonus = siege.fortificationLevel * 0.1

        // Réduction si les murs sont endommagés
        val wallsRatio = siege.wallsHealth.toDouble() / siege.maxWallsHealth
        val gatesRatio = siege.gatesHealth.toDouble() / siege.maxGatesHealth

        // Moyenne pondérée (murs plus importants)
        val structureIntegrity = (wallsRatio * 0.7 + gatesRatio * 0.3)

        return baseBonus * structureIntegrity
    }

    /**
     * Invalide le cache
     */
    fun invalidateCache(battleId: Int) {
        activeSieges.remove(battleId)
    }

    /**
     * Convertit un ResultRow en Siege
     */
    private fun ResultRow.toSiege(): Siege {
        val equipmentNames = try {
            json.decodeFromString<List<String>>(this[Sieges.siegeEquipment])
        } catch (e: Exception) {
            emptyList()
        }

        val equipment = equipmentNames.mapNotNull { name ->
            SiegeEquipment.entries.find { it.name == name }
        }

        return Siege(
            battleId = this[Sieges.battleId],
            fortificationLevel = this[Sieges.fortificationLevel],
            wallsHealth = this[Sieges.wallsHealth],
            maxWallsHealth = this[Sieges.maxWallsHealth],
            gatesHealth = this[Sieges.gatesHealth],
            maxGatesHealth = this[Sieges.maxGatesHealth],
            siegeProgress = this[Sieges.siegeProgress],
            siegeEquipmentUsed = equipment
        )
    }
}
