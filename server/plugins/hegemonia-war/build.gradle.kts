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
}
