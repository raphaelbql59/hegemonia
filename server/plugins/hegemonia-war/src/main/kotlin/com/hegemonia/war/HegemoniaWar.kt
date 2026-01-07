package com.hegemonia.war

import com.hegemonia.core.HegemoniaCore
import com.hegemonia.war.command.BattleCommand
import com.hegemonia.war.command.WarCommand
import com.hegemonia.war.dao.WarTables
import com.hegemonia.war.listener.BattleListener
import com.hegemonia.war.listener.WarListener
import com.hegemonia.war.service.BattleService
import com.hegemonia.war.service.SiegeService
import com.hegemonia.war.service.WarService
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.SchemaUtils

/**
 * Plugin principal de gestion des guerres
 */
class HegemoniaWar : JavaPlugin() {

    lateinit var warService: WarService
        private set
    lateinit var battleService: BattleService
        private set
    lateinit var siegeService: SiegeService
        private set

    override fun onEnable() {
        instance = this
        saveDefaultConfig()

        // Récupérer le core
        val core = server.pluginManager.getPlugin("HegemoniaCore") as? HegemoniaCore
        if (core == null) {
            logger.severe("HegemoniaCore non trouvé! Désactivation du plugin.")
            server.pluginManager.disablePlugin(this)
            return
        }

        // Initialiser les tables
        initializeTables(core)

        // Initialiser les services
        warService = WarService(core.database)
        battleService = BattleService(core.database, warService)
        siegeService = SiegeService(core.database, battleService)

        // Enregistrer les commandes
        registerCommands()

        // Enregistrer les listeners
        registerListeners()

        // Démarrer les tâches planifiées
        startScheduledTasks()

        logger.info("HegemoniaWar activé - Système de guerre opérationnel!")
    }

    override fun onDisable() {
        // Sauvegarder les batailles en cours
        savePendingData()
        logger.info("HegemoniaWar désactivé")
    }

    private fun initializeTables(core: HegemoniaCore) {
        core.database.transaction {
            SchemaUtils.createMissingTablesAndColumns(
                WarTables.Wars,
                WarTables.WarParticipants,
                WarTables.Battles,
                WarTables.BattleParticipants,
                WarTables.Sieges,
                WarTables.WarEvents,
                WarTables.BattleTimeSlots,
                WarTables.PeaceTreaties,
                WarTables.Truces
            )
        }
        logger.info("Tables de guerre initialisées")
    }

    private fun registerCommands() {
        getCommand("war")?.setExecutor(WarCommand(this))
        getCommand("battle")?.setExecutor(BattleCommand(this))
        logger.info("Commandes de guerre enregistrées")
    }

    private fun registerListeners() {
        val pluginManager = server.pluginManager
        pluginManager.registerEvents(BattleListener(this), this)
        pluginManager.registerEvents(WarListener(this), this)
        logger.info("Listeners de guerre enregistrés")
    }

    private fun startScheduledTasks() {
        // Vérifier les guerres à démarrer
        server.scheduler.runTaskTimer(this, Runnable {
            checkWarStarts()
        }, 20L * 60, 20L * 60) // Toutes les minutes

        // Vérifier les batailles programmées
        server.scheduler.runTaskTimer(this, Runnable {
            checkScheduledBattles()
        }, 20L * 30, 20L * 30) // Toutes les 30 secondes

        // Mettre à jour les sièges
        server.scheduler.runTaskTimer(this, Runnable {
            updateSieges()
        }, 20L * 5, 20L * 5) // Toutes les 5 secondes

        // Vérifier la fatigue de guerre
        server.scheduler.runTaskTimer(this, Runnable {
            checkWarWeariness()
        }, 20L * 60 * 60, 20L * 60 * 60) // Toutes les heures

        logger.info("Tâches planifiées de guerre démarrées")
    }

    private fun checkWarStarts() {
        // Démarrer les guerres dont le délai est écoulé
        val now = java.time.Instant.now()
        // TODO: Implémenter la logique de démarrage automatique
    }

    private fun checkScheduledBattles() {
        // Démarrer les batailles programmées
        // TODO: Implémenter la logique de démarrage des batailles
    }

    private fun updateSieges() {
        // Mettre à jour la progression des sièges
        // TODO: Implémenter la mise à jour des sièges
    }

    private fun checkWarWeariness() {
        // Augmenter la fatigue de guerre
        // TODO: Implémenter la fatigue de guerre progressive
    }

    private fun savePendingData() {
        // Sauvegarder l'état des batailles en cours
        logger.info("Sauvegarde des données de guerre...")
    }

    companion object {
        lateinit var instance: HegemoniaWar
            private set
    }
}
