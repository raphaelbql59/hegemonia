/*
 * HEGEMONIA - Plugins Gradle Settings
 * Configuration multi-projets pour tous les plugins
 */

rootProject.name = "hegemonia-plugins"

// Plugins principaux
include("hegemonia-core")      // API commune et utilitaires
include("hegemonia-nations")   // Système de nations
include("hegemonia-war")       // Système de guerre
include("hegemonia-economy")   // Système économique

// Plugins secondaires (à ajouter plus tard)
// include("hegemonia-jobs")
// include("hegemonia-diplomacy")
// include("hegemonia-events")
// include("hegemonia-intel")
// include("hegemonia-faith")
// include("hegemonia-quests")
// include("hegemonia-tech")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
