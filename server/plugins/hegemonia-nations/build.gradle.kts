/*
 * HEGEMONIA Nations - Build Configuration
 * Système de nations et gouvernements
 */

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    // Dépendance sur le core
    implementation(project(":hegemonia-core"))
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("HegemoniaNations")
}
