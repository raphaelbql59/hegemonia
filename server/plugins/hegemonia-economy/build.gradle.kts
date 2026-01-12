/*
 * HEGEMONIA Economy - Build Configuration
 * Système économique et marché
 */

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    // Dépendances sur les autres modules (compileOnly car ce sont des plugins séparés)
    compileOnly(project(":hegemonia-core"))
    compileOnly(project(":hegemonia-nations"))

    // Vault pour compatibilité avec autres plugins économiques
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("HegemoniaEconomy")

    // Exclure Kotlin et les dépendances fournies par le Core
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*"))
        exclude(dependency("org.jetbrains.kotlinx:.*"))
        exclude(dependency("org.jetbrains.exposed:.*"))
        exclude(dependency("org.postgresql:.*"))
        exclude(dependency("com.zaxxer:.*"))
        exclude(dependency("redis.clients:.*"))
        exclude(dependency("org.spongepowered:.*"))
        exclude(dependency("org.slf4j:.*"))
    }
}
