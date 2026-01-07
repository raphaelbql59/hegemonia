/*
 * HEGEMONIA Core - Build Configuration
 * API commune et utilitaires partagés
 */

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    // Ce module est l'API, pas de dépendances supplémentaires
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("HegemoniaCcore")
}
