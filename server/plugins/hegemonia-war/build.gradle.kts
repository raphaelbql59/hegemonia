/*
 * HEGEMONIA War - Build Configuration
 * Système de guerre et batailles
 */

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    // Dépendances sur les autres modules (compileOnly car ce sont des plugins séparés)
    compileOnly(project(":hegemonia-core"))
    compileOnly(project(":hegemonia-nations"))
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("HegemoniaWar")

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
