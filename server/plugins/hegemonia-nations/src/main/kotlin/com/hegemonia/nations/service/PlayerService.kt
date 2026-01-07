package com.hegemonia.nations.service

import com.hegemonia.core.database.DatabaseManager
import com.hegemonia.nations.dao.NationTables.NationInvites
import com.hegemonia.nations.dao.NationTables.NationMembers
import com.hegemonia.nations.dao.NationTables.Players
import com.hegemonia.nations.model.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Service de gestion des joueurs
 */
class PlayerService(private val db: DatabaseManager) {

    // Cache des joueurs en ligne
    private val playerCache = ConcurrentHashMap<UUID, HegemoniaPlayer>()

    /**
     * Récupère ou crée un joueur
     */
    fun getOrCreatePlayer(uuid: UUID, username: String): HegemoniaPlayer {
        // Vérifier le cache
        playerCache[uuid]?.let { return it }

        return db.transaction {
            val existing = Players.select { Players.uuid eq uuid }.singleOrNull()

            if (existing != null) {
                existing.toHegemoniaPlayer()
            } else {
                // Créer un nouveau joueur
                val now = Instant.now()
                Players.insert {
                    it[Players.uuid] = uuid
                    it[Players.username] = username
                    it[firstJoin] = now
                    it[lastSeen] = now
                }

                HegemoniaPlayer(
                    uuid = uuid,
                    username = username,
                    nationId = null,
                    role = null,
                    joinedNationAt = null,
                    firstJoin = now,
                    lastSeen = now,
                    playTime = 0,
                    balance = 0.0,
                    reputation = 0,
                    kills = 0,
                    deaths = 0,
                    blocksPlaced = 0,
                    blocksDestroyed = 0,
                    settings = PlayerSettings()
                )
            }
        }.also { playerCache[uuid] = it }
    }

    /**
     * Récupère un joueur par UUID
     */
    fun getPlayer(uuid: UUID): HegemoniaPlayer? {
        playerCache[uuid]?.let { return it }

        return db.transaction {
            Players.select { Players.uuid eq uuid }
                .singleOrNull()
                ?.toHegemoniaPlayer()
                ?.also { playerCache[uuid] = it }
        }
    }

    /**
     * Met à jour les informations d'un joueur
     */
    fun updatePlayer(player: HegemoniaPlayer) {
        db.transaction {
            Players.update({ Players.uuid eq player.uuid }) {
                it[username] = player.username
                it[nationId] = player.nationId
                it[role] = player.role
                it[joinedNationAt] = player.joinedNationAt
                it[lastSeen] = player.lastSeen
                it[playTime] = player.playTime
                it[balance] = player.balance
                it[reputation] = player.reputation
                it[kills] = player.kills
                it[deaths] = player.deaths
                it[blocksPlaced] = player.blocksPlaced
                it[blocksDestroyed] = player.blocksDestroyed
            }
        }
        playerCache[player.uuid] = player
    }

    /**
     * Met à jour la dernière connexion
     */
    fun updateLastSeen(uuid: UUID) {
        val now = Instant.now()
        db.transaction {
            Players.update({ Players.uuid eq uuid }) {
                it[lastSeen] = now
            }
        }
        playerCache[uuid]?.let {
            playerCache[uuid] = it.copy(lastSeen = now)
        }
    }

    /**
     * Ajoute du temps de jeu
     */
    fun addPlayTime(uuid: UUID, seconds: Long) {
        db.transaction {
            Players.update({ Players.uuid eq uuid }) {
                with(SqlExpressionBuilder) {
                    it[playTime] = playTime + seconds
                }
            }
        }
        playerCache[uuid]?.let {
            playerCache[uuid] = it.copy(playTime = it.playTime + seconds)
        }
    }

    /**
     * Fait rejoindre une nation à un joueur
     */
    fun joinNation(uuid: UUID, nationId: Int, role: NationRole = NationRole.RECRUIT): Boolean {
        val now = Instant.now()

        return db.transaction {
            // Mettre à jour la table Players
            Players.update({ Players.uuid eq uuid }) {
                it[Players.nationId] = nationId
                it[Players.role] = role
                it[joinedNationAt] = now
            }

            // Ajouter dans NationMembers
            NationMembers.upsert {
                it[NationMembers.nationId] = nationId
                it[playerId] = uuid
                it[NationMembers.role] = role
                it[joinedAt] = now
            }

            // Supprimer les invitations
            NationInvites.deleteWhere {
                (NationInvites.playerId eq uuid) and (NationInvites.nationId eq nationId)
            }

            true
        }.also { success ->
            if (success) {
                playerCache[uuid]?.let {
                    playerCache[uuid] = it.copy(
                        nationId = nationId,
                        role = role,
                        joinedNationAt = now
                    )
                }
            }
        }
    }

    /**
     * Fait quitter la nation à un joueur
     */
    fun leaveNation(uuid: UUID): Boolean {
        val player = getPlayer(uuid) ?: return false
        val nationId = player.nationId ?: return false

        return db.transaction {
            // Mettre à jour la table Players
            Players.update({ Players.uuid eq uuid }) {
                it[Players.nationId] = null
                it[role] = null
                it[joinedNationAt] = null
            }

            // Supprimer de NationMembers
            NationMembers.deleteWhere {
                (NationMembers.playerId eq uuid) and (NationMembers.nationId eq nationId)
            }

            true
        }.also { success ->
            if (success) {
                playerCache[uuid]?.let {
                    playerCache[uuid] = it.copy(
                        nationId = null,
                        role = null,
                        joinedNationAt = null
                    )
                }
            }
        }
    }

    /**
     * Change le rôle d'un joueur dans sa nation
     */
    fun setRole(uuid: UUID, newRole: NationRole): Boolean {
        val player = getPlayer(uuid) ?: return false
        val nationId = player.nationId ?: return false

        return db.transaction {
            Players.update({ Players.uuid eq uuid }) {
                it[role] = newRole
            }
            NationMembers.update({
                (NationMembers.playerId eq uuid) and (NationMembers.nationId eq nationId)
            }) {
                it[role] = newRole
            }
            true
        }.also { success ->
            if (success) {
                playerCache[uuid]?.let {
                    playerCache[uuid] = it.copy(role = newRole)
                }
            }
        }
    }

    /**
     * Envoie une invitation à rejoindre une nation
     */
    fun sendInvite(playerId: UUID, nationId: Int, invitedBy: UUID): Boolean {
        val now = Instant.now()
        val expiresAt = now.plus(24, ChronoUnit.HOURS)

        return db.transaction {
            NationInvites.upsert {
                it[NationInvites.playerId] = playerId
                it[NationInvites.nationId] = nationId
                it[NationInvites.invitedBy] = invitedBy
                it[invitedAt] = now
                it[NationInvites.expiresAt] = expiresAt
            }
            true
        }
    }

    /**
     * Récupère les invitations d'un joueur
     */
    fun getInvites(playerId: UUID): List<NationInvite> {
        return db.transaction {
            NationInvites.select { NationInvites.playerId eq playerId }
                .map {
                    NationInvite(
                        playerId = it[NationInvites.playerId],
                        nationId = it[NationInvites.nationId],
                        invitedBy = it[NationInvites.invitedBy],
                        invitedAt = it[NationInvites.invitedAt],
                        expiresAt = it[NationInvites.expiresAt]
                    )
                }
                .filter { !it.isExpired() }
        }
    }

    /**
     * Modifie le solde d'un joueur
     */
    fun modifyBalance(uuid: UUID, amount: Double): Boolean {
        return db.transaction {
            Players.update({ Players.uuid eq uuid }) {
                with(SqlExpressionBuilder) {
                    it[balance] = balance + amount
                }
            } > 0
        }.also { success ->
            if (success) {
                playerCache[uuid]?.let {
                    playerCache[uuid] = it.copy(balance = it.balance + amount)
                }
            }
        }
    }

    /**
     * Enregistre un kill
     */
    fun registerKill(killerId: UUID, victimId: UUID) {
        db.transaction {
            Players.update({ Players.uuid eq killerId }) {
                with(SqlExpressionBuilder) {
                    it[kills] = kills + 1
                }
            }
            Players.update({ Players.uuid eq victimId }) {
                with(SqlExpressionBuilder) {
                    it[deaths] = deaths + 1
                }
            }
        }
    }

    /**
     * Retire un joueur du cache
     */
    fun uncache(uuid: UUID) {
        playerCache.remove(uuid)
    }

    /**
     * Convertit un ResultRow en HegemoniaPlayer
     */
    private fun ResultRow.toHegemoniaPlayer(): HegemoniaPlayer {
        return HegemoniaPlayer(
            uuid = this[Players.uuid],
            username = this[Players.username],
            nationId = this[Players.nationId],
            role = this[Players.role],
            joinedNationAt = this[Players.joinedNationAt],
            firstJoin = this[Players.firstJoin],
            lastSeen = this[Players.lastSeen],
            playTime = this[Players.playTime],
            balance = this[Players.balance],
            reputation = this[Players.reputation],
            kills = this[Players.kills],
            deaths = this[Players.deaths],
            blocksPlaced = this[Players.blocksPlaced],
            blocksDestroyed = this[Players.blocksDestroyed],
            settings = PlayerSettings() // TODO: Parse JSON
        )
    }
}
