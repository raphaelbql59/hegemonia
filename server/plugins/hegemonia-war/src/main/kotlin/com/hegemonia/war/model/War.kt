package com.hegemonia.war.model

import java.time.Instant
import java.util.UUID

/**
 * Représente une guerre entre nations
 */
data class War(
    val id: Int,
    val uuid: UUID,
    val attackerId: Int,        // Nation attaquante
    val defenderId: Int,        // Nation défenseure
    val status: WarStatus,
    val warGoal: WarGoal,
    val declaredAt: Instant,
    val startedAt: Instant?,    // Début effectif (après délai)
    val endedAt: Instant?,
    val attackerScore: Int,
    val defenderScore: Int,
    val attackerWarWeariness: Int,
    val defenderWarWeariness: Int,
    val reason: String,         // Casus Belli
    val peaceTerms: String?,    // Termes de paix proposés
) {
    /**
     * Calcule le score total
     */
    val totalScore: Int get() = attackerScore - defenderScore

    /**
     * Vérifie si l'attaquant gagne
     */
    val isAttackerWinning: Boolean get() = attackerScore > defenderScore

    /**
     * Vérifie si la guerre peut se terminer (score suffisant)
     */
    fun canEndWar(): Boolean {
        return kotlin.math.abs(totalScore) >= WIN_SCORE_THRESHOLD ||
                attackerWarWeariness >= MAX_WAR_WEARINESS ||
                defenderWarWeariness >= MAX_WAR_WEARINESS
    }

    /**
     * Détermine le vainqueur
     */
    fun getWinner(): Int? {
        if (!canEndWar()) return null
        return when {
            attackerScore >= WIN_SCORE_THRESHOLD -> attackerId
            defenderScore >= WIN_SCORE_THRESHOLD -> defenderId
            attackerWarWeariness >= MAX_WAR_WEARINESS -> defenderId
            defenderWarWeariness >= MAX_WAR_WEARINESS -> attackerId
            else -> null
        }
    }

    companion object {
        const val WIN_SCORE_THRESHOLD = 100
        const val MAX_WAR_WEARINESS = 100
        const val WAR_START_DELAY_HOURS = 24  // Délai avant début de la guerre
        const val MIN_PEACE_DURATION_DAYS = 7  // Durée minimale de paix après guerre
    }
}

/**
 * Statut d'une guerre
 */
enum class WarStatus(val displayName: String, val color: String) {
    DECLARED("Déclarée", "<yellow>"),       // Guerre déclarée, pas encore commencée
    ACTIVE("Active", "<red>"),              // Guerre en cours
    CEASEFIRE("Cessez-le-feu", "<gold>"),   // Pause temporaire
    NEGOTIATING("Négociation", "<aqua>"),   // Négociation de paix
    ENDED("Terminée", "<gray>"),            // Guerre finie
    SURRENDERED("Capitulation", "<dark_red>") // Une nation a capitulé
}

/**
 * Objectifs de guerre (War Goals)
 */
enum class WarGoal(
    val displayName: String,
    val description: String,
    val scoreMultiplier: Double,
    val requiredScore: Int
) {
    CONQUEST(
        displayName = "Conquête",
        description = "Conquérir des territoires ennemis",
        scoreMultiplier = 1.0,
        requiredScore = 100
    ),

    SUBJUGATION(
        displayName = "Assujettissement",
        description = "Transformer l'ennemi en vassal",
        scoreMultiplier = 1.5,
        requiredScore = 150
    ),

    LIBERATION(
        displayName = "Libération",
        description = "Libérer des territoires occupés",
        scoreMultiplier = 0.8,
        requiredScore = 80
    ),

    HUMILIATION(
        displayName = "Humiliation",
        description = "Humilier l'ennemi (pas de gain territorial)",
        scoreMultiplier = 0.6,
        requiredScore = 60
    ),

    INDEPENDENCE(
        displayName = "Indépendance",
        description = "Obtenir l'indépendance d'un suzerain",
        scoreMultiplier = 1.2,
        requiredScore = 120
    ),

    TOTAL_WAR(
        displayName = "Guerre Totale",
        description = "Destruction complète de l'ennemi",
        scoreMultiplier = 2.0,
        requiredScore = 200
    ),

    DEFENSIVE(
        displayName = "Défensive",
        description = "Repousser l'envahisseur",
        scoreMultiplier = 0.7,
        requiredScore = 70
    ),

    REVENGE(
        displayName = "Vengeance",
        description = "Venger une défaite passée",
        scoreMultiplier = 1.1,
        requiredScore = 110
    );

    companion object {
        fun fromString(value: String): WarGoal? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

/**
 * Participant à une guerre (nation + alliés)
 */
data class WarParticipant(
    val warId: Int,
    val nationId: Int,
    val side: WarSide,
    val joinedAt: Instant,
    val contribution: Int,      // Contribution au score
    val casualties: Int,        // Pertes
    val isOriginal: Boolean     // Nation originale ou allié
)

/**
 * Côté dans une guerre
 */
enum class WarSide {
    ATTACKER,
    DEFENDER
}

/**
 * Événement de guerre (pour l'historique)
 */
data class WarEvent(
    val id: Int,
    val warId: Int,
    val type: WarEventType,
    val nationId: Int?,
    val description: String,
    val scoreChange: Int,
    val timestamp: Instant
)

/**
 * Types d'événements de guerre
 */
enum class WarEventType(val displayName: String, val icon: String) {
    WAR_DECLARED("Déclaration de guerre", "⚔"),
    WAR_STARTED("Début de la guerre", "🏁"),
    BATTLE_WON("Bataille gagnée", "⚔"),
    BATTLE_LOST("Bataille perdue", "💀"),
    SIEGE_STARTED("Siège commencé", "🏰"),
    SIEGE_WON("Siège réussi", "🏆"),
    SIEGE_FAILED("Siège échoué", "❌"),
    TERRITORY_CAPTURED("Territoire conquis", "🚩"),
    TERRITORY_LOST("Territoire perdu", "📍"),
    ALLY_JOINED("Allié rejoint", "🤝"),
    CEASEFIRE("Cessez-le-feu", "🕊"),
    PEACE_OFFERED("Paix proposée", "📜"),
    PEACE_ACCEPTED("Paix acceptée", "✅"),
    PEACE_REJECTED("Paix refusée", "❌"),
    SURRENDER("Capitulation", "🏳"),
    WAR_ENDED("Fin de la guerre", "🏁")
}
