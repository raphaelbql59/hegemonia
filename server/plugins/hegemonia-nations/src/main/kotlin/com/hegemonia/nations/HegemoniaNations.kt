package com.hegemonia.nations

import com.hegemonia.core.HegemoniaCore
import com.hegemonia.nations.command.NationCommand
import com.hegemonia.nations.command.NationAdminCommand
import com.hegemonia.nations.dao.NationTables
import com.hegemonia.nations.gui.NationMenuManager
import com.hegemonia.nations.listener.PlayerListener
import com.hegemonia.nations.listener.ProtectionListener
import com.hegemonia.nations.service.ElectionService
import com.hegemonia.nations.service.EmpireService
import com.hegemonia.nations.service.NationService
import com.hegemonia.nations.service.PlayerService
import com.hegemonia.nations.service.TerritoryService
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * HegemoniaNations - Système de nations pour Hegemonia
 *
 * Fonctionnalités:
 * - Création et gestion de nations
 * - Système de gouvernement (démocratie, monarchie, dictature, etc.)
 * - Gestion des territoires et régions
 * - Système de citoyens et rôles
 * - Relations diplomatiques
 */
class HegemoniaNations : JavaPlugin() {

    lateinit var nationService: NationService
        private set

    lateinit var playerService: PlayerService
        private set

    lateinit var territoryService: TerritoryService
        private set

    lateinit var empireService: EmpireService
        private set

    lateinit var electionService: ElectionService
        private set

    lateinit var menuManager: NationMenuManager
        private set

    private val core: HegemoniaCore
        get() = HegemoniaCore.get()

    override fun onEnable() {
        instance = this

        logger.info("╔══════════════════════════════════════════╗")
        logger.info("║   HEGEMONIA NATIONS - Initialisation     ║")
        logger.info("╚══════════════════════════════════════════╝")

        // Charger la configuration
        saveDefaultConfig()
        logger.info("✓ Configuration chargée")

        // Initialiser les tables
        initDatabase()
        logger.info("✓ Tables initialisées")

        // Initialiser les services
        nationService = NationService(core.database)
        playerService = PlayerService(core.database)
        territoryService = TerritoryService(core.database)
        empireService = EmpireService(this)
        empireService.initialize()
        electionService = ElectionService(this)
        electionService.initialize()
        logger.info("✓ Services initialisés")

        // Initialiser le gestionnaire de menus GUI
        menuManager = NationMenuManager(this)
        logger.info("✓ Menus GUI initialisés")

        // Enregistrer les commandes
        registerCommands()
        logger.info("✓ Commandes enregistrées")

        // Enregistrer les listeners
        registerListeners()
        logger.info("✓ Listeners enregistrés")

        // Charger les données en cache
        loadCache()
        logger.info("✓ Cache chargé")

        logger.info("══════════════════════════════════════════")
        logger.info("  HegemoniaNations v${description.version} activé!")
        logger.info("══════════════════════════════════════════")
    }

    override fun onDisable() {
        logger.info("Arrêt de HegemoniaNations...")

        // Sauvegarder les données en cache
        saveCache()

        logger.info("HegemoniaNations désactivé.")
    }

    private fun initDatabase() {
        core.database.transaction {
            SchemaUtils.createMissingTablesAndColumns(
                NationTables.Nations,
                NationTables.NationMembers,
                NationTables.NationRoles,
                NationTables.NationRelations,
                NationTables.Territories,
                NationTables.Players,
                NationTables.NationInvites,
                NationTables.Elections,
                NationTables.ElectionCandidates,
                NationTables.ElectionVotes,
                NationTables.NationAuditLog
            )
        }
    }

    private fun registerCommands() {
        getCommand("nation")?.setExecutor(NationCommand(this))
        getCommand("nadmin")?.setExecutor(NationAdminCommand(this))
    }

    private fun registerListeners() {
        server.pluginManager.registerEvents(PlayerListener(this), this)
        server.pluginManager.registerEvents(ProtectionListener(this), this)
    }

    private fun loadCache() {
        // Charger les nations actives en cache Redis
        val nations = nationService.getAllNations()
        logger.info("  → ${nations.size} nations chargées")
    }

    private fun saveCache() {
        // Sauvegarder le cache si nécessaire
    }

    companion object {
        lateinit var instance: HegemoniaNations
            private set

        fun get(): HegemoniaNations = instance
    }
}
