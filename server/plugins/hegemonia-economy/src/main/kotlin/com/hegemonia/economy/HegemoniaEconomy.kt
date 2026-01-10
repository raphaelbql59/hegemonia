package com.hegemonia.economy

import com.hegemonia.core.HegemoniaCore
import com.hegemonia.economy.command.EconomyCommand
import com.hegemonia.economy.command.MarketCommand
import com.hegemonia.economy.command.BankCommand
import com.hegemonia.economy.dao.EconomyTables
import com.hegemonia.economy.gui.EconomyMenuManager
import com.hegemonia.economy.listener.EconomyListener
import com.hegemonia.economy.service.BankService
import com.hegemonia.economy.service.MarketService
import com.hegemonia.economy.service.TransactionService
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.SchemaUtils

/**
 * HegemoniaEconomy - Système économique pour Hegemonia
 *
 * Fonctionnalités:
 * - Monnaie: Hegemonia Dollar (H$)
 * - Banques personnelles et nationales
 * - Marché capital (prix fixes au lobby)
 * - Bourse internationale (offre/demande)
 * - Entreprises et production
 * - Système fiscal (national + international)
 * - Commerce inter-nations
 */
class HegemoniaEconomy : JavaPlugin() {

    lateinit var bankService: BankService
        private set

    lateinit var marketService: MarketService
        private set

    lateinit var transactionService: TransactionService
        private set

    lateinit var menuManager: EconomyMenuManager
        private set

    private val core: HegemoniaCore
        get() = HegemoniaCore.get()

    override fun onEnable() {
        instance = this

        logger.info("╔══════════════════════════════════════════╗")
        logger.info("║   HEGEMONIA ECONOMY - Initialisation     ║")
        logger.info("╚══════════════════════════════════════════╝")

        // Charger la configuration
        saveDefaultConfig()
        logger.info("✓ Configuration chargée")

        // Initialiser les tables
        initDatabase()
        logger.info("✓ Tables initialisées")

        // Initialiser les services
        bankService = BankService(core.database)
        marketService = MarketService(core.database)
        transactionService = TransactionService(core.database)
        logger.info("✓ Services initialisés")

        // Initialiser le gestionnaire de menus GUI
        menuManager = EconomyMenuManager(this)
        logger.info("✓ Menus GUI initialisés")

        // Enregistrer les commandes
        registerCommands()
        logger.info("✓ Commandes enregistrées")

        // Enregistrer les listeners
        registerListeners()
        logger.info("✓ Listeners enregistrés")

        // Démarrer les tâches planifiées
        startScheduledTasks()
        logger.info("✓ Tâches planifiées démarrées")

        logger.info("══════════════════════════════════════════")
        logger.info("  HegemoniaEconomy v${description.version} activé!")
        logger.info("══════════════════════════════════════════")
    }

    override fun onDisable() {
        logger.info("Arrêt de HegemoniaEconomy...")
        logger.info("HegemoniaEconomy désactivé.")
    }

    private fun initDatabase() {
        core.database.transaction {
            SchemaUtils.createMissingTablesAndColumns(
                EconomyTables.PlayerAccounts,
                EconomyTables.NationTreasuries,
                EconomyTables.Transactions,
                EconomyTables.MarketItems,
                EconomyTables.MarketOrders,
                EconomyTables.Enterprises,
                EconomyTables.EnterpriseEmployees,
                EconomyTables.TaxRecords
            )
        }
    }

    private fun registerCommands() {
        getCommand("money")?.setExecutor(EconomyCommand(this))
        getCommand("bank")?.setExecutor(BankCommand(this))
        getCommand("market")?.setExecutor(MarketCommand(this))
    }

    private fun registerListeners() {
        server.pluginManager.registerEvents(EconomyListener(this), this)
    }

    private fun startScheduledTasks() {
        // Mise à jour des prix du marché toutes les heures
        server.scheduler.runTaskTimer(this, Runnable {
            marketService.updateMarketPrices()
        }, 20L * 60 * 60, 20L * 60 * 60)

        // Collecte des taxes toutes les 24h (temps réel)
        server.scheduler.runTaskTimer(this, Runnable {
            collectTaxes()
        }, 20L * 60 * 60 * 24, 20L * 60 * 60 * 24)
    }

    private fun collectTaxes() {
        logger.info("Collecte des taxes en cours...")
        // Implémenté dans le service
    }

    companion object {
        lateinit var instance: HegemoniaEconomy
            private set

        fun get(): HegemoniaEconomy = instance

        // Constantes économiques
        const val CURRENCY_SYMBOL = "H$"
        const val CURRENCY_NAME = "Hegemonia Dollar"
        const val STARTING_BALANCE = 100.0
        const val MAX_PERSONAL_BALANCE = 10_000_000.0
        const val DEFAULT_TAX_RATE = 10.0
        const val TRANSACTION_FEE = 0.01 // 1%

        // Pénalité de mort
        const val DEATH_PENALTY_ENABLED = true
        const val DEATH_PENALTY_PERCENT = 0.05 // 5% du portefeuille
        const val DEATH_PENALTY_MAX = 500.0 // Maximum 500 H$
    }
}
