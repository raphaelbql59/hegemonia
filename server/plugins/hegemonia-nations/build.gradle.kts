/*
 * HEGEMONIA Nations - Build Configuration
 * Système de nations et gouvernements
 */

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    // Dépendance sur le core (compileOnly car c'est un plugin séparé)
    compileOnly(project(":hegemonia-core"))
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("HegemoniaNations")

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
