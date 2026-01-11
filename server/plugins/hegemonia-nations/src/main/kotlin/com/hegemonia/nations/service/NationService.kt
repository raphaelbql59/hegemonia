package com.hegemonia.nations.service

import com.hegemonia.core.database.DatabaseManager
import com.hegemonia.nations.dao.NationTables.NationMembers
import com.hegemonia.nations.dao.NationTables.NationRelations
import com.hegemonia.nations.dao.NationTables.Nations
import com.hegemonia.nations.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Service de gestion des nations
 */
class NationService(private val db: DatabaseManager) {

    // Cache des nations en mémoire
    private val nationCache = ConcurrentHashMap<Int, Nation>()
    private val nationByName = ConcurrentHashMap<String, Int>()
    private val nationByTag = ConcurrentHashMap<String, Int>()

    /**
     * Crée une nouvelle nation
     */
    fun createNation(
        name: String,
        tag: String,
        leaderId: UUID,
        governmentType: GovernmentType = GovernmentType.DEMOCRACY,
        description: String = ""
    ): Result<Nation> {
        // Validation
        if (name.length < Nation.MIN_NAME_LENGTH || name.length > Nation.MAX_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("Le nom doit faire entre ${Nation.MIN_NAME_LENGTH} et ${Nation.MAX_NAME_LENGTH} caractères"))
        }
        if (tag.length != Nation.TAG_LENGTH) {
            return Result.failure(IllegalArgumentException("Le tag doit faire exactement ${Nation.TAG_LENGTH} caractères"))
        }
        if (nationByName.containsKey(name.lowercase())) {
            return Result.failure(IllegalArgumentException("Une nation avec ce nom existe déjà"))
        }
        if (nationByTag.containsKey(tag.uppercase())) {
            return Result.failure(IllegalArgumentException("Une nation avec ce tag existe déjà"))
        }

        return db.transaction {
            val nationUuid = UUID.randomUUID()
            val now = Instant.now()

            val nationId = Nations.insertAndGetId {
                it[Nations.uuid] = nationUuid
                it[Nations.name] = name
                it[Nations.tag] = tag.uppercase()
                it[Nations.description] = description
                it[Nations.governmentType] = governmentType
                it[Nations.leaderId] = leaderId
                it[Nations.createdAt] = now
                it[Nations.balance] = 1000.0  // Capital de départ
                it[Nations.power] = 100
                it[Nations.stability] = 50
            }

            // Ajouter le leader comme membre
            NationMembers.insert {
                it[NationMembers.nationId] = nationId.value
                it[playerId] = leaderId
                it[role] = NationRole.LEADER
                it[joinedAt] = now
            }

            val nation = Nation(
                id = nationId.value,
                uuid = nationUuid,
                name = name,
                tag = tag.uppercase(),
                description = description,
                governmentType = governmentType,
                leaderId = leaderId,
                capitalRegion = null,
                createdAt = now,
                balance = 1000.0,
                power = 100,
                color = "#FFFFFF",
                banner = null,
                motto = null,
                anthem = null,
                isOpen = false,
                taxRate = 10.0,
                warWeariness = 0,
                stability = 50,
                reputation = 0
            )

            // Mettre en cache
            nationCache[nation.id] = nation
            nationByName[name.lowercase()] = nation.id
            nationByTag[tag.uppercase()] = nation.id

            Result.success(nation)
        }
    }

    /**
     * Récupère une nation par ID
     */
    fun getNation(id: Int): Nation? {
        // Vérifier le cache d'abord
        nationCache[id]?.let { return it }

        return db.transaction {
            Nations.select { Nations.id eq id }
                .singleOrNull()
                ?.toNation()
                ?.also { nationCache[it.id] = it }
        }
    }

    /**
     * Récupère une nation par nom
     */
    fun getNationByName(name: String): Nation? {
        nationByName[name.lowercase()]?.let { return getNation(it) }

        return db.transaction {
            Nations.select { Nations.name.lowerCase() eq name.lowercase() }
                .singleOrNull()
                ?.toNation()
                ?.also {
                    nationCache[it.id] = it
                    nationByName[it.name.lowercase()] = it.id
                }
        }
    }

    /**
     * Récupère une nation par tag
     */
    fun getNationByTag(tag: String): Nation? {
        nationByTag[tag.uppercase()]?.let { return getNation(it) }

        return db.transaction {
            Nations.select { Nations.tag eq tag.uppercase() }
                .singleOrNull()
                ?.toNation()
                ?.also {
                    nationCache[it.id] = it
                    nationByTag[it.tag] = it.id
                }
        }
    }

    /**
     * Récupère toutes les nations
     */
    fun getAllNations(): List<Nation> {
        return db.transaction {
            Nations.selectAll()
                .map { it.toNation() }
                .also { nations ->
                    nations.forEach { nation ->
                        nationCache[nation.id] = nation
                        nationByName[nation.name.lowercase()] = nation.id
                        nationByTag[nation.tag] = nation.id
                    }
                }
        }
    }

    /**
     * Met à jour une nation
     */
    fun updateNation(nation: Nation): Boolean {
        return db.transaction {
            Nations.update({ Nations.id eq nation.id }) {
                it[name] = nation.name
                it[description] = nation.description
                it[governmentType] = nation.governmentType
                it[leaderId] = nation.leaderId
                it[capitalRegion] = nation.capitalRegion
                it[balance] = nation.balance
                it[power] = nation.power
                it[color] = nation.color
                it[banner] = nation.banner
                it[motto] = nation.motto
                it[anthem] = nation.anthem
                it[isOpen] = nation.isOpen
                it[taxRate] = nation.taxRate
                it[warWeariness] = nation.warWeariness
                it[stability] = nation.stability
                it[reputation] = nation.reputation
            } > 0
        }.also { success ->
            if (success) {
                nationCache[nation.id] = nation
            }
        }
    }

    /**
     * Supprime une nation
     */
    fun deleteNation(nationId: Int): Boolean {
        val nation = getNation(nationId) ?: return false

        return db.transaction {
            // Supprimer les membres
            NationMembers.deleteWhere { NationMembers.nationId eq nationId }

            // Supprimer les relations
            NationRelations.deleteWhere {
                (NationRelations.nationId eq nationId) or
                        (targetNationId eq nationId)
            }

            // Supprimer la nation
            Nations.deleteWhere { Nations.id eq nationId } > 0
        }.also { success ->
            if (success) {
                nationCache.remove(nationId)
                nationByName.remove(nation.name.lowercase())
                nationByTag.remove(nation.tag)
            }
        }
    }

    /**
     * Modifie le solde de la nation
     */
    fun modifyBalance(nationId: Int, amount: Double): Boolean {
        return db.transaction {
            Nations.update({ Nations.id eq nationId }) {
                with(SqlExpressionBuilder) {
                    it[balance] = balance + amount
                }
            } > 0
        }.also { success ->
            if (success) {
                nationCache[nationId]?.let { nation ->
                    nationCache[nationId] = nation.copy(balance = nation.balance + amount)
                }
            }
        }
    }

    /**
     * Récupère les membres d'une nation
     */
    fun getMembers(nationId: Int): List<Pair<UUID, NationRole>> {
        return db.transaction {
            NationMembers.select { NationMembers.nationId eq nationId }
                .map { it[NationMembers.playerId] to it[NationMembers.role] }
        }
    }

    /**
     * Compte les membres d'une nation
     */
    fun getMemberCount(nationId: Int): Int {
        return db.transaction {
            NationMembers.select { NationMembers.nationId eq nationId }.count().toInt()
        }
    }

    /**
     * Définit une relation entre deux nations
     */
    fun setRelation(nationId: Int, targetNationId: Int, relationType: RelationType, expiresAt: Instant? = null) {
        db.transaction {
            NationRelations.upsert {
                it[NationRelations.nationId] = nationId
                it[NationRelations.targetNationId] = targetNationId
                it[NationRelations.relationType] = relationType
                it[since] = Instant.now()
                it[NationRelations.expiresAt] = expiresAt
            }
        }
    }

    /**
     * Récupère la relation entre deux nations
     */
    fun getRelation(nationId: Int, targetNationId: Int): RelationType {
        return db.transaction {
            NationRelations.select {
                (NationRelations.nationId eq nationId) and
                        (NationRelations.targetNationId eq targetNationId)
            }.singleOrNull()?.get(NationRelations.relationType) ?: RelationType.NEUTRAL
        }
    }

    /**
     * Vérifie si deux nations sont alliées
     */
    fun areAllies(nationId: Int, targetNationId: Int): Boolean {
        val relation = getRelation(nationId, targetNationId)
        return relation == RelationType.ALLY || relation == RelationType.TRADE_PARTNER
    }

    /**
     * Vérifie si deux nations sont en guerre
     */
    fun areAtWar(nationId: Int, targetNationId: Int): Boolean {
        return getRelation(nationId, targetNationId) == RelationType.WAR
    }

    /**
     * Invalide le cache d'une nation
     */
    fun invalidateCache(nationId: Int) {
        nationCache.remove(nationId)?.let { nation ->
            nationByName.remove(nation.name.lowercase())
            nationByTag.remove(nation.tag)
        }
    }

    /**
     * Alias pour getNation (utilisé par EmpireService)
     */
    fun getNationById(id: Int): Nation? = getNation(id)

    /**
     * Retire de l'argent du trésor national
     */
    fun withdrawFromTreasury(nationId: Int, amount: Double): Boolean {
        val nation = getNation(nationId) ?: return false
        if (nation.balance < amount) return false
        return modifyBalance(nationId, -amount)
    }

    /**
     * Dépose de l'argent dans le trésor national
     */
    fun depositToTreasury(nationId: Int, amount: Double): Boolean {
        return modifyBalance(nationId, amount)
    }

    /**
     * Modifie la puissance d'une nation
     */
    fun modifyPower(nationId: Int, amount: Int): Boolean {
        return db.transaction {
            Nations.update({ Nations.id eq nationId }) {
                with(SqlExpressionBuilder) {
                    it[power] = power + amount
                }
            } > 0
        }.also { success ->
            if (success) {
                nationCache[nationId]?.let { nation ->
                    nationCache[nationId] = nation.copy(power = nation.power + amount)
                }
            }
        }
    }

    /**
     * Récupère toutes les relations d'une nation
     */
    fun getRelations(nationId: Int): List<NationRelation> {
        return db.transaction {
            NationRelations.select { NationRelations.nationId eq nationId }
                .map { row ->
                    NationRelation(
                        nationId = row[NationRelations.nationId],
                        targetNationId = row[NationRelations.targetNationId],
                        relationType = row[NationRelations.relationType],
                        since = row[NationRelations.since],
                        expiresAt = row[NationRelations.expiresAt]
                    )
                }
        }
    }

    /**
     * Convertit un ResultRow en Nation
     */
    private fun ResultRow.toNation(): Nation {
        return Nation(
            id = this[Nations.id].value,
            uuid = this[Nations.uuid],
            name = this[Nations.name],
            tag = this[Nations.tag],
            description = this[Nations.description],
            governmentType = this[Nations.governmentType],
            leaderId = this[Nations.leaderId],
            capitalRegion = this[Nations.capitalRegion],
            createdAt = this[Nations.createdAt],
            balance = this[Nations.balance],
            power = this[Nations.power],
            color = this[Nations.color],
            banner = this[Nations.banner],
            motto = this[Nations.motto],
            anthem = this[Nations.anthem],
            isOpen = this[Nations.isOpen],
            taxRate = this[Nations.taxRate],
            warWeariness = this[Nations.warWeariness],
            stability = this[Nations.stability],
            reputation = this[Nations.reputation]
        )
    }
}
