/*
 * HEGEMONIA Economy - Build Configuration
 * Système économique et marché
 */

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    // Dépendances sur les autres modules
    implementation(project(":hegemonia-core"))
    implementation(project(":hegemonia-nations"))

    // Vault pour compatibilité avec autres plugins économiques
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("HegemoniaEconomy")
}
