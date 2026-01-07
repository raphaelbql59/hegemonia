/*
 * HEGEMONIA - Root Build Configuration
 * Configuration Gradle pour tous les plugins
 */

plugins {
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.serialization") version "1.9.22" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("io.papermc.paperweight.userdev") version "1.5.11" apply false
}

allprojects {
    group = "com.hegemonia"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "com.github.johnrengelman.shadow")

    dependencies {
        // Kotlin
        "implementation"(kotlin("stdlib"))
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        "implementation"("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

        // Paper API
        "compileOnly"("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

        // Database - Exposed ORM
        "implementation"("org.jetbrains.exposed:exposed-core:0.46.0")
        "implementation"("org.jetbrains.exposed:exposed-dao:0.46.0")
        "implementation"("org.jetbrains.exposed:exposed-jdbc:0.46.0")
        "implementation"("org.jetbrains.exposed:exposed-java-time:0.46.0")

        // PostgreSQL Driver
        "implementation"("org.postgresql:postgresql:42.7.1")

        // HikariCP - Connection Pool
        "implementation"("com.zaxxer:HikariCP:5.1.0")

        // Redis - Jedis
        "implementation"("redis.clients:jedis:5.1.0")

        // Configuration - Configurate
        "implementation"("org.spongepowered:configurate-yaml:4.1.2")
        "implementation"("org.spongepowered:configurate-extra-kotlin:4.1.2")

        // Adventure - Text API (inclus dans Paper mais utile pour compilation)
        "compileOnly"("net.kyori:adventure-api:4.15.0")
        "compileOnly"("net.kyori:adventure-text-minimessage:4.15.0")

        // Testing
        "testImplementation"(kotlin("test"))
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.10.1")
        "testImplementation"("io.mockk:mockk:1.13.9")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    tasks.withType<JavaCompile> {
        options.release.set(21)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("")

        // Désactiver minimize et relocate pour éviter les problèmes avec Java 21
        // minimize()

        // Note: Relocations désactivées temporairement à cause de Java 21
        // relocate("org.jetbrains.exposed", "com.hegemonia.libs.exposed")
        // relocate("com.zaxxer.hikari", "com.hegemonia.libs.hikari")
        // relocate("redis.clients", "com.hegemonia.libs.jedis")
    }

    // Copy JAR to test server plugins folder
    tasks.register<Copy>("deployToServer") {
        dependsOn("shadowJar")
        from(tasks.named("shadowJar"))
        into("../../paper/earth/plugins")
    }
}
