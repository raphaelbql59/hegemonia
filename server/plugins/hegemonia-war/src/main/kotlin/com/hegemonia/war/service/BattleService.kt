package com.hegemonia.war.service

import com.hegemonia.core.database.DatabaseManager
import com.hegemonia.war.dao.WarTables.BattleParticipants
import com.hegemonia.war.dao.WarTables.Battles
import com.hegemonia.war.model.*
import org.bukkit.Location
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Service de gestion des batailles
 */
class BattleService(
    private val db: DatabaseManager,
    private val warService: WarService
) {

    // Cache des batailles actives
    private val activeBattles = ConcurrentHashMap<Int, Battle>()

    // Participants aux batailles actives
    private val battleParticipants = ConcurrentHashMap<Int, MutableSet<UUID>>()

    /**
     * Crée une nouvelle bataille
     */
    fun createBattle(
        warId: Int,
        type: BattleType,
        regionId: String,
        attackerNationId: Int,
        defenderNationId: Int,
        center: Location,
        radius: Int = Battle.DEFAULT_RADIUS,
        scheduledAt: Instant? = null
    ): Result<Battle> {
        val war = warService.getWar(warId)
            ?: return Result.failure(IllegalStateException("Guerre non trouvée"))

        if (war.status != WarStatus.ACTIVE) {
            return Result.failure(IllegalStateException("La guerre n'est pas active"))
        }

        val battleUuid = UUID.randomUUID()
        val now = Instant.now()
        val status = if (scheduledAt != null) BattleStatus.SCHEDULED else BattleStatus.PREPARATION

        return db.transaction {
            val battleId = Battles.insertAndGetId {
                it[uuid] = battleUuid
                it[Battles.warId] = warId
                it[Battles.type] = type
                it[Battles.status] = status
                it[Battles.regionId] = regionId
                it[Battles.attackerNationId] = attackerNationId
                it[Battles.defenderNationId] = defenderNationId
                it[Battles.scheduledAt] = scheduledAt
                it[startedAt] = if (scheduledAt == null) now else null
                it[centerX] = center.x
                it[centerY] = center.y
                it[centerZ] = center.z
                it[Battles.radius] = radius
            }

            val battle = Battle(
                id = battleId.value,
                uuid = battleUuid,
                warId = warId,
                type = type,
                status = status,
                regionId = regionId,
                attackerNationId = attackerNationId,
                defenderNationId = defenderNationId,
                scheduledAt = scheduledAt,
                startedAt = if (scheduledAt == null) now else null,
                endedAt = null,
                attackerKills = 0,
                defenderKills = 0,
                attackerDeaths = 0,
                defenderDeaths = 0,
                winnerId = null,
                scoreAwarded = 0,
                centerX = center.x,
                centerY = center.y,
                centerZ = center.z,
                radius = radius
            )

            activeBattles[battle.id] = battle
            battleParticipants[battle.id] = mutableSetOf()

            Result.success(battle)
        }
    }

    /**
     * Démarre une bataille
     */
    fun startBattle(battleId: Int): Boolean {
        val battle = getBattle(battleId) ?: return false
        if (battle.status != BattleStatus.SCHEDULED && battle.status != BattleStatus.PREPARATION) {
            return false
        }

        return db.transaction {
            Battles.update({ Battles.id eq battleId }) {
                it[status] = BattleStatus.IN_PROGRESS
                it[startedAt] = Instant.now()
            }
            true
        }.also { success ->
            if (success) {
                activeBattles[battleId] = battle.copy(
                    status = BattleStatus.IN_PROGRESS,
                    startedAt = Instant.now()
                )
            }
        }
    }

    /**
     * Rejoint une bataille
     */
    fun joinBattle(battleId: Int, playerId: UUID, nationId: Int, side: WarSide): Boolean {
        val battle = getBattle(battleId) ?: return false
        if (battle.status != BattleStatus.IN_PROGRESS && battle.status != BattleStatus.PREPARATION) {
            return false
        }

        return db.transaction {
            BattleParticipants.insert {
                it[BattleParticipants.battleId] = battleId
                it[BattleParticipants.playerId] = playerId
                it[BattleParticipants.nationId] = nationId
                it[BattleParticipants.side] = side
                it[joinedAt] = Instant.now()
            }
            true
        }.also { success ->
            if (success) {
                battleParticipants.getOrPut(battleId) { mutableSetOf() }.add(playerId)
            }
        }
    }

    /**
     * Quitte une bataille
     */
    fun leaveBattle(battleId: Int, playerId: UUID): Boolean {
        return db.transaction {
            BattleParticipants.update({
                (BattleParticipants.battleId eq battleId) and
                        (BattleParticipants.playerId eq playerId)
            }) {
                it[leftAt] = Instant.now()
            } > 0
        }.also { success ->
            if (success) {
                battleParticipants[battleId]?.remove(playerId)
            }
        }
    }

    /**
     * Enregistre un kill dans une bataille
     */
    fun registerKill(battleId: Int, killerId: UUID, victimId: UUID): Boolean {
        val battle = getBattle(battleId) ?: return false
        if (battle.status != BattleStatus.IN_PROGRESS) return false

        return db.transaction {
            // Récupérer les côtés des joueurs
            val killerSide = BattleParticipants
                .select { (BattleParticipants.battleId eq battleId) and (BattleParticipants.playerId eq killerId) }
                .singleOrNull()?.get(BattleParticipants.side) ?: return@transaction false

            // Mettre à jour les stats du killer
            BattleParticipants.update({
                (BattleParticipants.battleId eq battleId) and (BattleParticipants.playerId eq killerId)
            }) {
                with(SqlExpressionBuilder) {
                    it[kills] = kills + 1
                }
            }

            // Mettre à jour les stats de la victime
            BattleParticipants.update({
                (BattleParticipants.battleId eq battleId) and (BattleParticipants.playerId eq victimId)
            }) {
                with(SqlExpressionBuilder) {
                    it[deaths] = deaths + 1
                }
                it[isAlive] = false
            }

            // Mettre à jour les stats de la bataille
            when (killerSide) {
                WarSide.ATTACKER -> {
                    Battles.update({ Battles.id eq battleId }) {
                        with(SqlExpressionBuilder) {
                            it[attackerKills] = attackerKills + 1
                            it[defenderDeaths] = defenderDeaths + 1
                        }
                    }
                }
                WarSide.DEFENDER -> {
                    Battles.update({ Battles.id eq battleId }) {
                        with(SqlExpressionBuilder) {
                            it[defenderKills] = defenderKills + 1
                            it[attackerDeaths] = attackerDeaths + 1
                        }
                    }
                }
            }

            true
        }.also { invalidateCache(battleId) }
    }

    /**
     * Respawn un joueur dans la bataille
     */
    fun respawnPlayer(battleId: Int, playerId: UUID): Boolean {
        return db.transaction {
            BattleParticipants.update({
                (BattleParticipants.battleId eq battleId) and (BattleParticipants.playerId eq playerId)
            }) {
                it[isAlive] = true
            } > 0
        }
    }

    /**
     * Termine une bataille
     */
    fun endBattle(battleId: Int, winnerId: Int? = null): Boolean {
        val battle = getBattle(battleId) ?: return false
        if (battle.status != BattleStatus.IN_PROGRESS) return false

        // Déterminer le vainqueur si non spécifié
        val finalWinnerId = winnerId ?: determineWinner(battle)

        // Calculer le score
        val score = calculateScore(battle, finalWinnerId)

        return db.transaction {
            Battles.update({ Battles.id eq battleId }) {
                it[status] = BattleStatus.COMPLETED
                it[endedAt] = Instant.now()
                it[Battles.winnerId] = finalWinnerId
                it[scoreAwarded] = score
            }

            // Ajouter le score à la guerre
            if (finalWinnerId != null) {
                val side = if (finalWinnerId == battle.attackerNationId) WarSide.ATTACKER else WarSide.DEFENDER
                warService.addScore(battle.warId, side, score, "Victoire: ${battle.type.displayName}")
            }

            true
        }.also { success ->
            if (success) {
                activeBattles.remove(battleId)
                battleParticipants.remove(battleId)
            }
        }
    }

    /**
     * Détermine le vainqueur d'une bataille
     */
    private fun determineWinner(battle: Battle): Int? {
        return when {
            battle.attackerKills > battle.defenderKills -> battle.attackerNationId
            battle.defenderKills > battle.attackerKills -> battle.defenderNationId
            else -> null  // Égalité
        }
    }

    /**
     * Calcule le score d'une bataille
     */
    private fun calculateScore(battle: Battle, winnerId: Int?): Int {
        if (winnerId == null) return battle.type.baseScore / 2  // Égalité

        val baseScore = battle.type.baseScore
        val killBonus = if (winnerId == battle.attackerNationId) {
            (battle.attackerKills - battle.defenderKills) * Battle.SCORE_PER_KILL
        } else {
            (battle.defenderKills - battle.attackerKills) * Battle.SCORE_PER_KILL
        }

        return baseScore + killBonus + Battle.VICTORY_SCORE
    }

    /**
     * Récupère une bataille
     */
    fun getBattle(battleId: Int): Battle? {
        activeBattles[battleId]?.let { return it }

        return db.transaction {
            Battles.select { Battles.id eq battleId }
                .singleOrNull()
                ?.toBattle()
                ?.also {
                    if (it.status == BattleStatus.IN_PROGRESS) {
                        activeBattles[it.id] = it
                    }
                }
        }
    }

    /**
     * Récupère les batailles actives d'une guerre
     */
    fun getActiveBattles(warId: Int): List<Battle> {
        return db.transaction {
            Battles.select {
                (Battles.warId eq warId) and
                        (Battles.status inList listOf(BattleStatus.IN_PROGRESS, BattleStatus.PREPARATION))
            }.map { it.toBattle() }
        }
    }

    /**
     * Vérifie si un joueur est dans une bataille
     */
    fun isInBattle(playerId: UUID): Int? {
        return battleParticipants.entries
            .find { it.value.contains(playerId) }
            ?.key
    }

    /**
     * Récupère la bataille à une position
     */
    fun getBattleAt(location: Location): Battle? {
        return activeBattles.values.find { battle ->
            battle.isInBattleZone(location)
        }
    }

    /**
     * Récupère les participants d'une bataille
     */
    fun getParticipants(battleId: Int): List<BattleParticipant> {
        return db.transaction {
            BattleParticipants.select { BattleParticipants.battleId eq battleId }
                .map { row ->
                    BattleParticipant(
                        battleId = row[BattleParticipants.battleId],
                        playerId = row[BattleParticipants.playerId],
                        nationId = row[BattleParticipants.nationId],
                        side = row[BattleParticipants.side],
                        joinedAt = row[BattleParticipants.joinedAt],
                        leftAt = row[BattleParticipants.leftAt],
                        kills = row[BattleParticipants.kills],
                        deaths = row[BattleParticipants.deaths],
                        damageDealt = row[BattleParticipants.damageDealt],
                        damageTaken = row[BattleParticipants.damageTaken],
                        isAlive = row[BattleParticipants.isAlive]
                    )
                }
        }
    }

    /**
     * Compte les participants actifs par côté
     */
    fun countActiveParticipants(battleId: Int): Pair<Int, Int> {
        return db.transaction {
            val attackers = BattleParticipants.select {
                (BattleParticipants.battleId eq battleId) and
                        (BattleParticipants.side eq WarSide.ATTACKER) and
                        (BattleParticipants.isAlive eq true)
            }.count().toInt()

            val defenders = BattleParticipants.select {
                (BattleParticipants.battleId eq battleId) and
                        (BattleParticipants.side eq WarSide.DEFENDER) and
                        (BattleParticipants.isAlive eq true)
            }.count().toInt()

            attackers to defenders
        }
    }

    /**
     * Invalide le cache
     */
    fun invalidateCache(battleId: Int) {
        activeBattles.remove(battleId)
    }

    /**
     * Convertit un ResultRow en Battle
     */
    private fun ResultRow.toBattle(): Battle {
        return Battle(
            id = this[Battles.id].value,
            uuid = this[Battles.uuid],
            warId = this[Battles.warId],
            type = this[Battles.type],
            status = this[Battles.status],
            regionId = this[Battles.regionId],
            attackerNationId = this[Battles.attackerNationId],
            defenderNationId = this[Battles.defenderNationId],
            scheduledAt = this[Battles.scheduledAt],
            startedAt = this[Battles.startedAt],
            endedAt = this[Battles.endedAt],
            attackerKills = this[Battles.attackerKills],
            defenderKills = this[Battles.defenderKills],
            attackerDeaths = this[Battles.attackerDeaths],
            defenderDeaths = this[Battles.defenderDeaths],
            winnerId = this[Battles.winnerId],
            scoreAwarded = this[Battles.scoreAwarded],
            centerX = this[Battles.centerX],
            centerY = this[Battles.centerY],
            centerZ = this[Battles.centerZ],
            radius = this[Battles.radius]
        )
    }
}
