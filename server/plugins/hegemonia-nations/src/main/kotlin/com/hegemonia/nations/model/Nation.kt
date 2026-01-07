package com.hegemonia.nations.model

import java.time.Instant
import java.util.UUID

/**
 * Représente une nation dans Hegemonia
 */
data class Nation(
    val id: Int,
    val uuid: UUID,
    val name: String,
    val tag: String,  // Tag court (3-4 caractères)
    val description: String,
    val governmentType: GovernmentType,
    val leaderId: UUID,
    val capitalRegion: String?,  // Identifiant de la région capitale
    val createdAt: Instant,
    val balance: Double,
    val power: Int,  // Puissance militaire/politique
    val color: String,  // Couleur pour la map
    val banner: String?,  // Pattern du drapeau
    val motto: String?,  // Devise nationale
    val anthem: String?,  // URL de l'hymne (optionnel)
    val isOpen: Boolean,  // Recrutement ouvert
    val taxRate: Double,  // Taux d'imposition (0-100)
    val warWeariness: Int,  // Fatigue de guerre
    val stability: Int,  // Stabilité (0-100)
    val reputation: Int,  // Réputation internationale
) {
    /**
     * Vérifie si la nation peut déclarer la guerre
     */
    fun canDeclareWar(): Boolean {
        return stability >= 30 && warWeariness < 80
    }

    /**
     * Calcule le coût d'entretien quotidien
     */
    fun dailyUpkeep(): Double {
        // Base + membres + territoires
        return 100.0 + (power * 0.5)
    }

    companion object {
        const val MIN_NAME_LENGTH = 3
        const val MAX_NAME_LENGTH = 32
        const val TAG_LENGTH = 4
        const val MIN_MEMBERS_FOR_WAR = 3
    }
}

/**
 * Rôles au sein d'une nation
 */
enum class NationRole(
    val displayName: String,
    val priority: Int,  // Plus haut = plus de pouvoir
    val permissions: Set<NationPermission>
) {
    LEADER(
        displayName = "Chef d'État",
        priority = 100,
        permissions = NationPermission.entries.toSet()  // Toutes les permissions
    ),

    MINISTER(
        displayName = "Ministre",
        priority = 80,
        permissions = setOf(
            NationPermission.INVITE,
            NationPermission.KICK,
            NationPermission.PROMOTE,
            NationPermission.CLAIM,
            NationPermission.UNCLAIM,
            NationPermission.BUILD,
            NationPermission.DESTROY,
            NationPermission.ACCESS_TREASURY,
            NationPermission.MANAGE_RELATIONS,
            NationPermission.BROADCAST
        )
    ),

    GENERAL(
        displayName = "Général",
        priority = 60,
        permissions = setOf(
            NationPermission.INVITE,
            NationPermission.KICK,
            NationPermission.CLAIM,
            NationPermission.BUILD,
            NationPermission.DESTROY,
            NationPermission.WAR_ACTIONS,
            NationPermission.BROADCAST
        )
    ),

    OFFICER(
        displayName = "Officier",
        priority = 40,
        permissions = setOf(
            NationPermission.INVITE,
            NationPermission.CLAIM,
            NationPermission.BUILD,
            NationPermission.DESTROY,
            NationPermission.WAR_ACTIONS
        )
    ),

    CITIZEN(
        displayName = "Citoyen",
        priority = 20,
        permissions = setOf(
            NationPermission.BUILD,
            NationPermission.DESTROY,
            NationPermission.VOTE
        )
    ),

    RECRUIT(
        displayName = "Recrue",
        priority = 10,
        permissions = setOf(
            NationPermission.BUILD
        )
    );

    fun hasPermission(permission: NationPermission): Boolean {
        return permissions.contains(permission)
    }

    fun canPromoteTo(role: NationRole): Boolean {
        return this.priority > role.priority
    }
}

/**
 * Permissions au sein d'une nation
 */
enum class NationPermission {
    INVITE,           // Inviter des joueurs
    KICK,             // Expulser des membres
    PROMOTE,          // Promouvoir des membres
    DEMOTE,           // Rétrograder des membres
    CLAIM,            // Revendiquer des territoires
    UNCLAIM,          // Abandonner des territoires
    BUILD,            // Construire sur le territoire
    DESTROY,          // Détruire sur le territoire
    ACCESS_TREASURY,  // Accéder au trésor national
    WITHDRAW_TREASURY,// Retirer du trésor
    SET_TAXES,        // Définir les taxes
    DECLARE_WAR,      // Déclarer la guerre
    MAKE_PEACE,       // Signer la paix
    MANAGE_RELATIONS, // Gérer alliances/ennemis
    WAR_ACTIONS,      // Actions en guerre (batailles)
    VOTE,             // Voter (démocratie)
    BROADCAST,        // Envoyer des annonces
    EDIT_INFO,        // Modifier les infos nation
    DISSOLVE          // Dissoudre la nation
}

/**
 * Relations entre nations
 */
enum class RelationType(
    val displayName: String,
    val color: String
) {
    NEUTRAL("Neutre", "<gray>"),
    ALLY("Allié", "<green>"),
    ENEMY("Ennemi", "<red>"),
    WAR("En guerre", "<dark_red>"),
    TRUCE("Trêve", "<yellow>"),
    VASSAL("Vassal", "<gold>"),
    OVERLORD("Suzerain", "<dark_purple>"),
    TRADE_PARTNER("Partenaire commercial", "<aqua>"),
    NON_AGGRESSION("Pacte de non-agression", "<blue>")
}

/**
 * Relation entre deux nations
 */
data class NationRelation(
    val nationId: Int,
    val targetNationId: Int,
    val relationType: RelationType,
    val since: Instant,
    val expiresAt: Instant?  // Pour les trêves temporaires
)
