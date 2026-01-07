package com.hegemonia.war.model

import org.bukkit.Location
import java.time.Instant
import java.util.UUID

/**
 * Représente une bataille dans une guerre
 */
data class Battle(
    val id: Int,
    val uuid: UUID,
    val warId: Int,
    val type: BattleType,
    val status: BattleStatus,
    val regionId: String,           // Région où se déroule la bataille
    val attackerNationId: Int,
    val defenderNationId: Int,
    val scheduledAt: Instant?,      // Heure programmée (si planifiée)
    val startedAt: Instant?,
    val endedAt: Instant?,
    val attackerKills: Int,
    val defenderKills: Int,
    val attackerDeaths: Int,
    val defenderDeaths: Int,
    val winnerId: Int?,             // Nation gagnante
    val scoreAwarded: Int,
    // Zone de combat
    val centerX: Double,
    val centerY: Double,
    val centerZ: Double,
    val radius: Int
) {
    /**
     * Vérifie si la bataille est en cours
     */
    val isActive: Boolean get() = status == BattleStatus.IN_PROGRESS

    /**
     * Calcule le K/D de l'attaquant
     */
    val attackerKD: Double
        get() = if (attackerDeaths == 0) attackerKills.toDouble()
        else attackerKills.toDouble() / attackerDeaths

    /**
     * Calcule le K/D du défenseur
     */
    val defenderKD: Double
        get() = if (defenderDeaths == 0) defenderKills.toDouble()
        else defenderKills.toDouble() / defenderDeaths

    /**
     * Vérifie si une position est dans la zone de bataille
     */
    fun isInBattleZone(x: Double, y: Double, z: Double): Boolean {
        val dx = x - centerX
        val dy = y - centerY
        val dz = z - centerZ
        return (dx * dx + dy * dy + dz * dz) <= (radius * radius)
    }

    /**
     * Vérifie si une location Bukkit est dans la zone de bataille
     */
    fun isInBattleZone(location: Location): Boolean {
        return isInBattleZone(location.x, location.y, location.z)
    }

    companion object {
        const val DEFAULT_RADIUS = 100           // Rayon par défaut
        const val MIN_DURATION_MINUTES = 15      // Durée minimale
        const val MAX_DURATION_MINUTES = 60      // Durée maximale
        const val RESPAWN_DELAY_SECONDS = 10     // Délai de respawn
        const val SCORE_PER_KILL = 5             // Points par kill
        const val VICTORY_SCORE = 50             // Points pour victoire
    }
}

/**
 * Types de batailles
 */
enum class BattleType(
    val displayName: String,
    val description: String,
    val baseScore: Int,
    val maxParticipants: Int
) {
    SKIRMISH(
        displayName = "Escarmouche",
        description = "Petit affrontement rapide",
        baseScore = 10,
        maxParticipants = 10
    ),

    BATTLE(
        displayName = "Bataille",
        description = "Affrontement standard",
        baseScore = 25,
        maxParticipants = 30
    ),

    SIEGE(
        displayName = "Siège",
        description = "Attaque d'une fortification",
        baseScore = 50,
        maxParticipants = 50
    ),

    NAVAL_BATTLE(
        displayName = "Bataille Navale",
        description = "Combat sur l'eau",
        baseScore = 30,
        maxParticipants = 20
    ),

    AERIAL_BATTLE(
        displayName = "Bataille Aérienne",
        description = "Combat aérien",
        baseScore = 35,
        maxParticipants = 15
    ),

    GRAND_BATTLE(
        displayName = "Grande Bataille",
        description = "Affrontement majeur décisif",
        baseScore = 75,
        maxParticipants = 100
    )
}

/**
 * Statut d'une bataille
 */
enum class BattleStatus(val displayName: String, val color: String) {
    SCHEDULED("Programmée", "<yellow>"),
    PREPARATION("Préparation", "<gold>"),
    IN_PROGRESS("En cours", "<red>"),
    ENDING("Fin imminente", "<dark_red>"),
    COMPLETED("Terminée", "<gray>"),
    CANCELLED("Annulée", "<dark_gray>")
}

/**
 * Participant à une bataille
 */
data class BattleParticipant(
    val battleId: Int,
    val playerId: UUID,
    val nationId: Int,
    val side: WarSide,
    val joinedAt: Instant,
    val leftAt: Instant?,
    val kills: Int,
    val deaths: Int,
    val damageDealt: Double,
    val damageTaken: Double,
    val isAlive: Boolean
) {
    val kd: Double
        get() = if (deaths == 0) kills.toDouble() else kills.toDouble() / deaths
}

/**
 * Siège (extension de bataille)
 */
data class Siege(
    val battleId: Int,
    val fortificationLevel: Int,
    val wallsHealth: Int,
    val maxWallsHealth: Int,
    val gatesHealth: Int,
    val maxGatesHealth: Int,
    val siegeProgress: Int,         // 0-100
    val siegeEquipmentUsed: List<SiegeEquipment>
) {
    /**
     * Vérifie si les murs sont détruits
     */
    val wallsDestroyed: Boolean get() = wallsHealth <= 0

    /**
     * Vérifie si les portes sont ouvertes
     */
    val gatesOpen: Boolean get() = gatesHealth <= 0

    /**
     * Vérifie si le siège est réussi
     */
    val isBreached: Boolean get() = wallsDestroyed || gatesOpen
}

/**
 * Équipement de siège
 */
enum class SiegeEquipment(
    val displayName: String,
    val damageToWalls: Int,
    val damageToGates: Int,
    val cost: Double
) {
    BATTERING_RAM("Bélier", 0, 50, 500.0),
    CATAPULT("Catapulte", 30, 10, 1000.0),
    TREBUCHET("Trébuchet", 50, 20, 2000.0),
    SIEGE_TOWER("Tour de siège", 0, 0, 1500.0),  // Permet d'escalader
    CANNON("Canon", 100, 80, 5000.0),
    EXPLOSIVES("Explosifs", 200, 150, 3000.0)
}

/**
 * Créneau horaire pour les batailles
 */
data class BattleTimeSlot(
    val dayOfWeek: Int,     // 1-7 (Lundi-Dimanche)
    val startHour: Int,     // 0-23
    val endHour: Int,       // 0-23
    val enabled: Boolean
) {
    /**
     * Vérifie si l'heure actuelle est dans ce créneau
     */
    fun isNow(): Boolean {
        val now = java.time.LocalDateTime.now()
        return now.dayOfWeek.value == dayOfWeek &&
                now.hour in startHour until endHour &&
                enabled
    }
}
