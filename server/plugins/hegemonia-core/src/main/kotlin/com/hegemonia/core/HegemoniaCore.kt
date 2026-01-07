package com.hegemonia.core

import com.hegemonia.core.config.CoreConfig
import com.hegemonia.core.database.DatabaseManager
import com.hegemonia.core.database.RedisManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

/**
 * HegemoniaCore - Plugin principal fournissant l'API commune
 *
 * Responsabilités:
 * - Connexion à la base de données PostgreSQL
 * - Connexion à Redis pour le cache/messaging
 * - Configuration partagée
 * - Utilitaires communs
 */
class HegemoniaCore : JavaPlugin() {

    lateinit var config: CoreConfig
        private set

    lateinit var database: DatabaseManager
        private set

    lateinit var redis: RedisManager
        private set

    val miniMessage: MiniMessage = MiniMessage.miniMessage()

    override fun onEnable() {
        instance = this

        logger.info("╔══════════════════════════════════════════╗")
        logger.info("║     HEGEMONIA CORE - Initialisation      ║")
        logger.info("╚══════════════════════════════════════════╝")

        // Charger la configuration
        saveDefaultConfig()
        config = CoreConfig.load(this)
        logger.info("✓ Configuration chargée")

        // Connexion à PostgreSQL
        try {
            database = DatabaseManager(config.database)
            database.connect()
            logger.info("✓ PostgreSQL connecté")
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "✗ Échec connexion PostgreSQL", e)
            server.pluginManager.disablePlugin(this)
            return
        }

        // Connexion à Redis
        try {
            redis = RedisManager(config.redis)
            redis.connect()
            logger.info("✓ Redis connecté")
        } catch (e: Exception) {
            logger.log(Level.WARNING, "✗ Échec connexion Redis (mode dégradé)", e)
            // Redis est optionnel, on continue
        }

        logger.info("══════════════════════════════════════════")
        logger.info("  HegemoniaCore v${description.version} activé!")
        logger.info("══════════════════════════════════════════")
    }

    override fun onDisable() {
        logger.info("Arrêt de HegemoniaCore...")

        // Fermer les connexions
        if (::redis.isInitialized) {
            redis.disconnect()
        }
        if (::database.isInitialized) {
            database.disconnect()
        }

        logger.info("HegemoniaCore désactivé.")
    }

    /**
     * Parse un message MiniMessage en Component
     */
    fun parse(message: String): Component {
        return miniMessage.deserialize(message)
    }

    /**
     * Parse un message avec des placeholders
     */
    fun parse(message: String, vararg placeholders: Pair<String, String>): Component {
        var result = message
        placeholders.forEach { (key, value) ->
            result = result.replace("<$key>", value)
        }
        return miniMessage.deserialize(result)
    }

    companion object {
        lateinit var instance: HegemoniaCore
            private set

        fun get(): HegemoniaCore = instance
    }
}
