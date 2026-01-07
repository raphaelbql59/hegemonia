package com.hegemonia.nations.model

/**
 * Types de gouvernement disponibles pour les nations
 */
enum class GovernmentType(
    val displayName: String,
    val description: String,
    val color: String,
    val leaderTitle: String,
    val features: Set<GovernmentFeature>
) {
    DEMOCRACY(
        displayName = "Démocratie",
        description = "Le peuple élit ses dirigeants",
        color = "<blue>",
        leaderTitle = "Président",
        features = setOf(
            GovernmentFeature.ELECTIONS,
            GovernmentFeature.TERM_LIMITS,
            GovernmentFeature.CITIZEN_VOTING
        )
    ),

    MONARCHY(
        displayName = "Monarchie",
        description = "Le pouvoir est héréditaire",
        color = "<gold>",
        leaderTitle = "Roi",
        features = setOf(
            GovernmentFeature.HEREDITARY_SUCCESSION,
            GovernmentFeature.NOBLE_RANKS,
            GovernmentFeature.ROYAL_DECREES
        )
    ),

    DICTATORSHIP(
        displayName = "Dictature",
        description = "Un leader absolu contrôle tout",
        color = "<red>",
        leaderTitle = "Dictateur",
        features = setOf(
            GovernmentFeature.ABSOLUTE_POWER,
            GovernmentFeature.MARTIAL_LAW,
            GovernmentFeature.PROPAGANDA
        )
    ),

    THEOCRACY(
        displayName = "Théocratie",
        description = "La religion guide le gouvernement",
        color = "<yellow>",
        leaderTitle = "Grand Prêtre",
        features = setOf(
            GovernmentFeature.RELIGIOUS_LAWS,
            GovernmentFeature.DIVINE_RIGHT,
            GovernmentFeature.HOLY_WARS
        )
    ),

    OLIGARCHY(
        displayName = "Oligarchie",
        description = "Gouverné par une élite",
        color = "<dark_purple>",
        leaderTitle = "Archonte",
        features = setOf(
            GovernmentFeature.COUNCIL_RULE,
            GovernmentFeature.WEALTH_VOTING,
            GovernmentFeature.TRADE_BONUSES
        )
    ),

    COMMUNISM(
        displayName = "Communisme",
        description = "Propriété collective des moyens de production",
        color = "<dark_red>",
        leaderTitle = "Secrétaire Général",
        features = setOf(
            GovernmentFeature.SHARED_RESOURCES,
            GovernmentFeature.PLANNED_ECONOMY,
            GovernmentFeature.WORKERS_COUNCILS
        )
    ),

    REPUBLIC(
        displayName = "République",
        description = "Représentants élus gouvernent",
        color = "<aqua>",
        leaderTitle = "Consul",
        features = setOf(
            GovernmentFeature.ELECTIONS,
            GovernmentFeature.SENATE,
            GovernmentFeature.CITIZEN_RIGHTS
        )
    ),

    FEDERATION(
        displayName = "Fédération",
        description = "Union de régions autonomes",
        color = "<green>",
        leaderTitle = "Chancelier",
        features = setOf(
            GovernmentFeature.REGIONAL_AUTONOMY,
            GovernmentFeature.FEDERAL_COUNCIL,
            GovernmentFeature.SHARED_DEFENSE
        )
    ),

    ANARCHY(
        displayName = "Anarchie",
        description = "Pas de gouvernement centralisé",
        color = "<dark_gray>",
        leaderTitle = "Porte-parole",
        features = setOf(
            GovernmentFeature.NO_TAXES,
            GovernmentFeature.DIRECT_DEMOCRACY,
            GovernmentFeature.COMMUNES
        )
    ),

    EMPIRE(
        displayName = "Empire",
        description = "Domination sur plusieurs territoires",
        color = "<dark_purple>",
        leaderTitle = "Empereur",
        features = setOf(
            GovernmentFeature.VASSAL_SYSTEM,
            GovernmentFeature.IMPERIAL_ARMY,
            GovernmentFeature.TRIBUTE_COLLECTION
        )
    );

    companion object {
        fun fromString(value: String): GovernmentType? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

/**
 * Fonctionnalités spéciales des gouvernements
 */
enum class GovernmentFeature {
    // Démocratie
    ELECTIONS,
    TERM_LIMITS,
    CITIZEN_VOTING,

    // Monarchie
    HEREDITARY_SUCCESSION,
    NOBLE_RANKS,
    ROYAL_DECREES,

    // Dictature
    ABSOLUTE_POWER,
    MARTIAL_LAW,
    PROPAGANDA,

    // Théocratie
    RELIGIOUS_LAWS,
    DIVINE_RIGHT,
    HOLY_WARS,

    // Oligarchie
    COUNCIL_RULE,
    WEALTH_VOTING,
    TRADE_BONUSES,

    // Communisme
    SHARED_RESOURCES,
    PLANNED_ECONOMY,
    WORKERS_COUNCILS,

    // République
    SENATE,
    CITIZEN_RIGHTS,

    // Fédération
    REGIONAL_AUTONOMY,
    FEDERAL_COUNCIL,
    SHARED_DEFENSE,

    // Anarchie
    NO_TAXES,
    DIRECT_DEMOCRACY,
    COMMUNES,

    // Empire
    VASSAL_SYSTEM,
    IMPERIAL_ARMY,
    TRIBUTE_COLLECTION
}
