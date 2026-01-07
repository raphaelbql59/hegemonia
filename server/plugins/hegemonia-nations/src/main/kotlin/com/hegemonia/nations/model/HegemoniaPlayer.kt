package com.hegemonia.nations.model

import java.time.Instant
import java.util.UUID

/**
 * Représente un joueur Hegemonia avec ses données nationales
 */
data class HegemoniaPlayer(
    val uuid: UUID,
    val username: String,
    val nationId: Int?,
    val role: NationRole?,
    val joinedNationAt: Instant?,
    val firstJoin: Instant,
    val lastSeen: Instant,
    val playTime: Long,  // En secondes
    val balance: Double,
    val reputation: Int,
    val kills: Int,
    val deaths: Int,
    val blocksPlaced: Long,
    val blocksDestroyed: Long,
    val settings: PlayerSettings
) {
    /**
     * Vérifie si le joueur est dans une nation
     */
    val hasNation: Boolean
        get() = nationId != null

    /**
     * Vérifie si le joueur est leader de sa nation
     */
    val isLeader: Boolean
        get() = role == NationRole.LEADER

    /**
     * Calcule le K/D ratio
     */
    val kdRatio: Double
        get() = if (deaths == 0) kills.toDouble() else kills.toDouble() / deaths

    /**
     * Vérifie si le joueur a une permission dans sa nation
     */
    fun hasNationPermission(permission: NationPermission): Boolean {
        return role?.hasPermission(permission) ?: false
    }
}

/**
 * Paramètres personnels du joueur
 */
data class PlayerSettings(
    val showScoreboard: Boolean = true,
    val showActionBar: Boolean = true,
    val allowTeleportRequests: Boolean = true,
    val language: String = "fr",
    val chatChannel: ChatChannel = ChatChannel.GLOBAL,
    val mapVisible: Boolean = true,
    val notificationsEnabled: Boolean = true
)

/**
 * Canaux de chat disponibles
 */
enum class ChatChannel(
    val prefix: String,
    val color: String
) {
    GLOBAL("G", "<white>"),
    NATION("N", "<green>"),
    ALLY("A", "<aqua>"),
    LOCAL("L", "<gray>"),
    TRADE("T", "<gold>")
}

/**
 * Invitation à rejoindre une nation
 */
data class NationInvite(
    val playerId: UUID,
    val nationId: Int,
    val invitedBy: UUID,
    val invitedAt: Instant,
    val expiresAt: Instant
) {
    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)
}
