package com.hegemonia.war.service

import com.hegemonia.core.database.DatabaseManager
import com.hegemonia.war.dao.WarTables.WarEvents
import com.hegemonia.war.dao.WarTables.WarParticipants
import com.hegemonia.war.dao.WarTables.Wars
import com.hegemonia.war.dao.WarTables.Truces
import com.hegemonia.war.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Service de gestion des guerres
 */
class WarService(private val db: DatabaseManager) {

    // Cache des guerres actives
    private val activeWars = ConcurrentHashMap<Int, War>()

    /**
     * Déclare une guerre
     */
    fun declareWar(
        attackerId: Int,
        defenderId: Int,
        warGoal: WarGoal,
        reason: String
    ): Result<War> {
        // Vérifier s'il y a déjà une guerre entre ces nations
        if (areAtWar(attackerId, defenderId)) {
            return Result.failure(IllegalStateException("Ces nations sont déjà en guerre"))
        }

        // Vérifier s'il y a une trêve active
        if (hasTruce(attackerId, defenderId)) {
            return Result.failure(IllegalStateException("Une trêve est en cours entre ces nations"))
        }

        val now = Instant.now()
        val warUuid = UUID.randomUUID()
        val startTime = now.plus(War.WAR_START_DELAY_HOURS.toLong(), ChronoUnit.HOURS)

        return db.transaction {
            val warId = Wars.insertAndGetId {
                it[uuid] = warUuid
                it[Wars.attackerId] = attackerId
                it[Wars.defenderId] = defenderId
                it[status] = WarStatus.DECLARED
                it[Wars.warGoal] = warGoal
                it[declaredAt] = now
                it[startedAt] = startTime
                it[Wars.reason] = reason
            }

            // Ajouter les nations comme participants originaux
            WarParticipants.insert {
                it[WarParticipants.warId] = warId.value
                it[nationId] = attackerId
                it[side] = WarSide.ATTACKER
                it[joinedAt] = now
                it[isOriginal] = true
            }
            WarParticipants.insert {
                it[WarParticipants.warId] = warId.value
                it[nationId] = defenderId
                it[side] = WarSide.DEFENDER
                it[joinedAt] = now
                it[isOriginal] = true
            }

            // Enregistrer l'événement
            logWarEvent(warId.value, WarEventType.WAR_DECLARED, attackerId,
                "Guerre déclarée: $reason", 0)

            val war = War(
                id = warId.value,
                uuid = warUuid,
                attackerId = attackerId,
                defenderId = defenderId,
                status = WarStatus.DECLARED,
                warGoal = warGoal,
                declaredAt = now,
                startedAt = startTime,
                endedAt = null,
                attackerScore = 0,
                defenderScore = 0,
                attackerWarWeariness = 0,
                defenderWarWeariness = 0,
                reason = reason,
                peaceTerms = null
            )

            activeWars[war.id] = war
            Result.success(war)
        }
    }

    /**
     * Démarre une guerre (après le délai)
     */
    fun startWar(warId: Int): Boolean {
        val war = getWar(warId) ?: return false
        if (war.status != WarStatus.DECLARED) return false

        return db.transaction {
            Wars.update({ Wars.id eq warId }) {
                it[status] = WarStatus.ACTIVE
                it[startedAt] = Instant.now()
            }

            logWarEvent(warId, WarEventType.WAR_STARTED, null,
                "La guerre a officiellement commencé", 0)

            true
        }.also { success ->
            if (success) {
                activeWars[warId] = war.copy(status = WarStatus.ACTIVE)
            }
        }
    }

    /**
     * Ajoute des points au score
     */
    fun addScore(warId: Int, side: WarSide, points: Int, reason: String): Boolean {
        val war = getWar(warId) ?: return false

        return db.transaction {
            when (side) {
                WarSide.ATTACKER -> {
                    Wars.update({ Wars.id eq warId }) {
                        with(SqlExpressionBuilder) {
                            it[attackerScore] = attackerScore + points
                        }
                    }
                    logWarEvent(warId, WarEventType.BATTLE_WON, war.attackerId, reason, points)
                }
                WarSide.DEFENDER -> {
                    Wars.update({ Wars.id eq warId }) {
                        with(SqlExpressionBuilder) {
                            it[defenderScore] = defenderScore + points
                        }
                    }
                    logWarEvent(warId, WarEventType.BATTLE_WON, war.defenderId, reason, points)
                }
            }
            true
        }.also { success ->
            if (success) {
                invalidateCache(warId)
            }
        }
    }

    /**
     * Ajoute de la fatigue de guerre
     */
    fun addWarWeariness(warId: Int, side: WarSide, amount: Int): Boolean {
        return db.transaction {
            when (side) {
                WarSide.ATTACKER -> {
                    Wars.update({ Wars.id eq warId }) {
                        with(SqlExpressionBuilder) {
                            it[attackerWarWeariness] = attackerWarWeariness + amount
                        }
                    }
                }
                WarSide.DEFENDER -> {
                    Wars.update({ Wars.id eq warId }) {
                        with(SqlExpressionBuilder) {
                            it[defenderWarWeariness] = defenderWarWeariness + amount
                        }
                    }
                }
            }
            true
        }.also { invalidateCache(warId) }
    }

    /**
     * Propose la paix
     */
    fun proposePeace(warId: Int, proposerId: Int, terms: String): Boolean {
        val war = getWar(warId) ?: return false
        if (war.status != WarStatus.ACTIVE) return false

        return db.transaction {
            Wars.update({ Wars.id eq warId }) {
                it[status] = WarStatus.NEGOTIATING
                it[peaceTerms] = terms
            }

            logWarEvent(warId, WarEventType.PEACE_OFFERED, proposerId,
                "Proposition de paix: $terms", 0)

            true
        }.also { invalidateCache(warId) }
    }

    /**
     * Accepte la paix et termine la guerre
     */
    fun acceptPeace(warId: Int): Boolean {
        val war = getWar(warId) ?: return false
        if (war.status != WarStatus.NEGOTIATING) return false

        val now = Instant.now()
        val winnerId = war.getWinner()

        return db.transaction {
            Wars.update({ Wars.id eq warId }) {
                it[status] = WarStatus.ENDED
                it[endedAt] = now
                it[Wars.winnerId] = winnerId
            }

            logWarEvent(warId, WarEventType.PEACE_ACCEPTED, null,
                "Paix acceptée", 0)
            logWarEvent(warId, WarEventType.WAR_ENDED, winnerId,
                "La guerre est terminée", 0)

            // Créer une trêve
            createTruce(war.attackerId, war.defenderId, War.MIN_PEACE_DURATION_DAYS)

            true
        }.also { success ->
            if (success) {
                activeWars.remove(warId)
            }
        }
    }

    /**
     * Capitulation
     */
    fun surrender(warId: Int, surrenderingNationId: Int): Boolean {
        val war = getWar(warId) ?: return false
        if (war.status != WarStatus.ACTIVE) return false

        val winnerId = if (surrenderingNationId == war.attackerId) war.defenderId else war.attackerId
        val now = Instant.now()

        return db.transaction {
            Wars.update({ Wars.id eq warId }) {
                it[status] = WarStatus.SURRENDERED
                it[endedAt] = now
                it[Wars.winnerId] = winnerId
            }

            logWarEvent(warId, WarEventType.SURRENDER, surrenderingNationId,
                "Capitulation", 0)
            logWarEvent(warId, WarEventType.WAR_ENDED, winnerId,
                "Victoire par capitulation", 0)

            createTruce(war.attackerId, war.defenderId, War.MIN_PEACE_DURATION_DAYS)

            true
        }.also { activeWars.remove(warId) }
    }

    /**
     * Ajoute un allié à une guerre
     */
    fun joinWar(warId: Int, nationId: Int, side: WarSide): Boolean {
        return db.transaction {
            WarParticipants.insert {
                it[WarParticipants.warId] = warId
                it[WarParticipants.nationId] = nationId
                it[WarParticipants.side] = side
                it[joinedAt] = Instant.now()
                it[isOriginal] = false
            }

            logWarEvent(warId, WarEventType.ALLY_JOINED, nationId,
                "Allié rejoint le conflit", 0)

            true
        }
    }

    /**
     * Récupère une guerre
     */
    fun getWar(warId: Int): War? {
        activeWars[warId]?.let { return it }

        return db.transaction {
            Wars.select { Wars.id eq warId }
                .singleOrNull()
                ?.toWar()
                ?.also {
                    if (it.status == WarStatus.ACTIVE || it.status == WarStatus.DECLARED) {
                        activeWars[it.id] = it
                    }
                }
        }
    }

    /**
     * Récupère les guerres actives d'une nation
     */
    fun getActiveWars(nationId: Int): List<War> {
        return db.transaction {
            Wars.select {
                ((Wars.attackerId eq nationId) or (Wars.defenderId eq nationId)) and
                        (Wars.status inList listOf(WarStatus.DECLARED, WarStatus.ACTIVE, WarStatus.NEGOTIATING))
            }.map { it.toWar() }
        }
    }

    /**
     * Vérifie si deux nations sont en guerre
     */
    fun areAtWar(nationId1: Int, nationId2: Int): Boolean {
        return db.transaction {
            Wars.select {
                (((Wars.attackerId eq nationId1) and (Wars.defenderId eq nationId2)) or
                        ((Wars.attackerId eq nationId2) and (Wars.defenderId eq nationId1))) and
                        (Wars.status inList listOf(WarStatus.DECLARED, WarStatus.ACTIVE))
            }.count() > 0
        }
    }

    /**
     * Vérifie s'il y a une trêve entre deux nations
     */
    fun hasTruce(nationId1: Int, nationId2: Int): Boolean {
        val now = Instant.now()
        return db.transaction {
            Truces.select {
                (((Truces.nation1Id eq nationId1) and (Truces.nation2Id eq nationId2)) or
                        ((Truces.nation1Id eq nationId2) and (Truces.nation2Id eq nationId1))) and
                        (Truces.expiresAt greater now)
            }.count() > 0
        }
    }

    /**
     * Crée une trêve
     */
    private fun createTruce(nationId1: Int, nationId2: Int, durationDays: Int) {
        val now = Instant.now()
        val expiresAt = now.plus(durationDays.toLong(), ChronoUnit.DAYS)

        Truces.insert {
            it[nation1Id] = nationId1
            it[nation2Id] = nationId2
            it[startedAt] = now
            it[Truces.expiresAt] = expiresAt
            it[reason] = "Fin de guerre"
        }
    }

    /**
     * Enregistre un événement de guerre
     */
    private fun logWarEvent(
        warId: Int,
        type: WarEventType,
        nationId: Int?,
        description: String,
        scoreChange: Int
    ) {
        WarEvents.insert {
            it[WarEvents.warId] = warId
            it[WarEvents.type] = type
            it[WarEvents.nationId] = nationId
            it[WarEvents.description] = description
            it[WarEvents.scoreChange] = scoreChange
            it[timestamp] = Instant.now()
        }
    }

    /**
     * Récupère l'historique d'une guerre
     */
    fun getWarHistory(warId: Int): List<WarEvent> {
        return db.transaction {
            WarEvents.select { WarEvents.warId eq warId }
                .orderBy(WarEvents.timestamp)
                .map { row ->
                    WarEvent(
                        id = row[WarEvents.id].value,
                        warId = row[WarEvents.warId],
                        type = row[WarEvents.type],
                        nationId = row[WarEvents.nationId],
                        description = row[WarEvents.description],
                        scoreChange = row[WarEvents.scoreChange],
                        timestamp = row[WarEvents.timestamp]
                    )
                }
        }
    }

    /**
     * Invalide le cache
     */
    fun invalidateCache(warId: Int) {
        activeWars.remove(warId)
    }

    /**
     * Convertit un ResultRow en War
     */
    private fun ResultRow.toWar(): War {
        return War(
            id = this[Wars.id].value,
            uuid = this[Wars.uuid],
            attackerId = this[Wars.attackerId],
            defenderId = this[Wars.defenderId],
            status = this[Wars.status],
            warGoal = this[Wars.warGoal],
            declaredAt = this[Wars.declaredAt],
            startedAt = this[Wars.startedAt],
            endedAt = this[Wars.endedAt],
            attackerScore = this[Wars.attackerScore],
            defenderScore = this[Wars.defenderScore],
            attackerWarWeariness = this[Wars.attackerWarWeariness],
            defenderWarWeariness = this[Wars.defenderWarWeariness],
            reason = this[Wars.reason],
            peaceTerms = this[Wars.peaceTerms]
        )
    }
}
