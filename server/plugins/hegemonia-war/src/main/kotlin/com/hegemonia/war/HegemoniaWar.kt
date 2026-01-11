package com.hegemonia.war

import com.hegemonia.core.HegemoniaCore
import com.hegemonia.war.bridge.NationBridge
import com.hegemonia.war.command.BattleCommand
import com.hegemonia.war.command.WarCommand
import com.hegemonia.war.dao.WarTables
import com.hegemonia.war.gui.WarMenuManager
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
    lateinit var nationBridge: NationBridge
        private set
    lateinit var menuManager: WarMenuManager
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

        // Initialiser le bridge vers HegemoniaNations
        nationBridge = NationBridge.getInstance()
        nationBridge.initialize()

        // Initialiser le gestionnaire de menus GUI
        menuManager = WarMenuManager(this)
        logger.info("Système de menus GUI initialisé")

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

        warService.getActiveWars().forEach { war ->
            if (war.status == com.hegemonia.war.model.WarStatus.DECLARED) {
                val startTime = war.startedAt ?: return@forEach
                if (now.isAfter(startTime) || now == startTime) {
                    if (warService.startWar(war.id)) {
                        logger.info("Guerre #${war.id} démarrée automatiquement")

                        // Notifier les nations
                        val attackerName = nationBridge.getNationName(war.attackerId) ?: "Nation #${war.attackerId}"
                        val defenderName = nationBridge.getNationName(war.defenderId) ?: "Nation #${war.defenderId}"

                        server.broadcast(
                            HegemoniaCore.get().parse(
                                "<dark_red>⚔ GUERRE!</dark_red> La guerre entre <gold>$attackerName</gold> et <gold>$defenderName</gold> a officiellement commencé!"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun checkScheduledBattles() {
        // Démarrer les batailles programmées
        val now = java.time.Instant.now()

        warService.getActiveWars().forEach { war ->
            if (war.status == com.hegemonia.war.model.WarStatus.ACTIVE) {
                battleService.getActiveBattles(war.id).forEach { battle ->
                    if (battle.status == com.hegemonia.war.model.BattleStatus.SCHEDULED) {
                        val scheduledAt = battle.scheduledAt ?: return@forEach
                        if (now.isAfter(scheduledAt) || now == scheduledAt) {
                            if (battleService.startBattle(battle.id)) {
                                logger.info("Bataille #${battle.id} démarrée automatiquement")

                                // Notifier les joueurs des nations concernées
                                val attackerName = nationBridge.getNationName(battle.attackerNationId) ?: "Attaquant"
                                val defenderName = nationBridge.getNationName(battle.defenderNationId) ?: "Défenseur"

                                server.broadcast(
                                    HegemoniaCore.get().parse(
                                        "<red>⚔ BATAILLE!</red> Une bataille commence entre <gold>$attackerName</gold> et <gold>$defenderName</gold> à ${battle.regionId}!"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateSieges() {
        // Mettre à jour la progression des sièges pour les batailles actives
        warService.getActiveWars().forEach { war ->
            if (war.status == com.hegemonia.war.model.WarStatus.ACTIVE) {
                battleService.getActiveBattles(war.id).forEach { battle ->
                    if (battle.status == com.hegemonia.war.model.BattleStatus.IN_PROGRESS &&
                        battle.type == com.hegemonia.war.model.BattleType.SIEGE) {

                        val siege = siegeService.getSiege(battle.id) ?: return@forEach

                        // Compter les joueurs actifs de chaque côté
                        val (attackers, defenders) = battleService.countActiveParticipants(battle.id)

                        // Calculer la progression basée sur la supériorité numérique
                        val progressChange = when {
                            attackers > defenders * 2 -> 3  // Supériorité écrasante
                            attackers > defenders -> 2      // Supériorité
                            attackers == defenders -> 0     // Égalité
                            defenders > attackers -> -1     // Défenseurs repoussent
                            else -> 0
                        }

                        if (progressChange != 0) {
                            siegeService.updateProgress(battle.id, progressChange)

                            // Vérifier si le siège est terminé
                            if (siegeService.isComplete(battle.id)) {
                                battleService.endBattle(battle.id, battle.attackerNationId)
                                logger.info("Siège #${battle.id} terminé - Victoire de l'attaquant")
                            }
                        }

                        // Vérifier si une brèche est ouverte (bonus pour l'attaquant)
                        if (siegeService.isBreached(battle.id) && siege.siegeProgress < 100) {
                            // Accélérer la progression si brèche
                            siegeService.updateProgress(battle.id, 1)
                        }
                    }
                }
            }
        }
    }

    private fun checkWarWeariness() {
        // Augmenter la fatigue de guerre pour les guerres actives
        warService.getActiveWars().forEach { war ->
            if (war.status == com.hegemonia.war.model.WarStatus.ACTIVE) {
                // Fatigue de base par heure
                val baseWeariness = 1

                // Les deux côtés accumulent de la fatigue
                warService.addWarWeariness(war.id, com.hegemonia.war.model.WarSide.ATTACKER, baseWeariness)
                warService.addWarWeariness(war.id, com.hegemonia.war.model.WarSide.DEFENDER, baseWeariness)

                // Vérifier si la guerre peut se terminer automatiquement (fatigue max)
                val updatedWar = warService.getWar(war.id) ?: return@forEach
                if (updatedWar.canEndWar()) {
                    val winner = updatedWar.getWinner()
                    if (winner != null) {
                        // La nation avec trop de fatigue capitule automatiquement
                        val loser = if (winner == war.attackerId) war.defenderId else war.attackerId
                        warService.surrender(war.id, loser)

                        val winnerName = nationBridge.getNationName(winner) ?: "Vainqueur"
                        val loserName = nationBridge.getNationName(loser) ?: "Perdant"

                        server.broadcast(
                            HegemoniaCore.get().parse(
                                "<gold>⚔ FIN DE GUERRE!</gold> <yellow>$loserName</yellow> a capitulé face à <gold>$winnerName</gold> (fatigue de guerre)!"
                            )
                        )
                        logger.info("Guerre #${war.id} terminée automatiquement - Capitulation par fatigue")
                    }
                }
            }
        }
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
