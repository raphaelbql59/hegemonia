package com.hegemonia.economy.gui

import com.hegemonia.core.utils.*
import com.hegemonia.economy.HegemoniaEconomy
import com.hegemonia.economy.model.MarketCategory
import com.hegemonia.economy.model.MarketItem
import com.hegemonia.economy.model.PlayerAccount
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Gestionnaire des menus GUI pour l'économie
 */
class EconomyMenuManager(private val plugin: HegemoniaEconomy) : Listener {

    private val bankService = plugin.bankService
    private val marketService = plugin.marketService
    private val transactionService = plugin.transactionService

    // Sessions des joueurs
    private val playerSessions = ConcurrentHashMap<UUID, MenuSession>()

    // Types de menus
    enum class MenuType {
        MAIN,
        WALLET,
        BANK,
        MARKET_MAIN,
        MARKET_CATEGORY,
        MARKET_ITEM,
        MARKET_BUY,
        MARKET_SELL,
        TRANSACTIONS,
        TOP_PLAYERS
    }

    // Session d'un joueur
    data class MenuSession(
        var menuType: MenuType,
        var page: Int = 0,
        var category: MarketCategory? = null,
        var selectedItem: MarketItem? = null,
        var quantity: Int = 1,
        var customData: MutableMap<String, Any> = mutableMapOf()
    )

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    // ==================== MENUS PRINCIPAUX ====================

    /**
     * Ouvre le menu principal d'économie
     */
    fun openMainMenu(player: Player) {
        val inventory = createInventory(player, 27, "Économie")
        val session = getOrCreateSession(player)
        session.menuType = MenuType.MAIN

        val account = bankService.getOrCreateAccount(player.uniqueId)
        val currency = HegemoniaEconomy.CURRENCY_SYMBOL

        // Portefeuille
        inventory.setItem(11, createItem(
            Material.GOLD_INGOT,
            "<gold>Portefeuille",
            listOf(
                "",
                "<white>Solde: <green>${formatMoney(account.balance)} $currency",
                "",
                "<yellow>Cliquez pour voir les détails"
            )
        ))

        // Banque
        inventory.setItem(13, createItem(
            Material.CHEST,
            "<yellow>Banque",
            listOf(
                "",
                "<white>Épargne: <aqua>${formatMoney(account.bankBalance)} $currency",
                "<gray>Intérêts: ${(PlayerAccount.INTEREST_RATE * 100)}% / jour",
                "",
                "<yellow>Cliquez pour gérer votre épargne"
            )
        ))

        // Marché
        inventory.setItem(15, createItem(
            Material.EMERALD,
            "<green>Marché",
            listOf(
                "",
                "<white>Achetez et vendez des ressources",
                "<gray>Prix dynamiques selon l'offre/demande",
                "",
                "<yellow>Cliquez pour accéder au marché"
            )
        ))

        // Historique
        inventory.setItem(21, createItem(
            Material.BOOK,
            "<white>Historique",
            listOf(
                "",
                "<gray>Consultez vos transactions",
                "",
                "<yellow>Cliquez pour voir l'historique"
            )
        ))

        // Classement
        inventory.setItem(23, createItem(
            Material.DIAMOND,
            "<aqua>Classement",
            listOf(
                "",
                "<gray>Top des joueurs les plus riches",
                "",
                "<yellow>Cliquez pour voir le classement"
            )
        ))

        // Total
        inventory.setItem(4, createItem(
            Material.NETHER_STAR,
            "<light_purple>Fortune totale",
            listOf(
                "",
                "<white>Total: <gold>${formatMoney(account.totalBalance)} $currency"
            )
        ))

        fillBorders(inventory, Material.GRAY_STAINED_GLASS_PANE)
        player.openInventory(inventory)
    }

    /**
     * Ouvre le menu du portefeuille
     */
    fun openWalletMenu(player: Player) {
        val inventory = createInventory(player, 27, "Portefeuille")
        val session = getOrCreateSession(player)
        session.menuType = MenuType.WALLET

        val account = bankService.getOrCreateAccount(player.uniqueId)
        val currency = HegemoniaEconomy.CURRENCY_SYMBOL

        // Solde actuel
        inventory.setItem(13, createItem(
            Material.GOLD_INGOT,
            "<gold>Votre solde",
            listOf(
                "",
                "<white>${formatMoney(account.balance)} $currency",
                "",
                "<gray>Cet argent est dans votre poche",
                "<gray>et peut être utilisé directement."
            )
        ))

        // Déposer en banque
        inventory.setItem(11, createItem(
            Material.HOPPER,
            "<yellow>Déposer en banque",
            listOf(
                "",
                "<gray>Transférez votre argent en épargne",
                "<gray>pour gagner des intérêts.",
                "",
                "<yellow>Cliquez pour déposer"
            )
        ))

        // Payer un joueur
        inventory.setItem(15, createItem(
            Material.PLAYER_HEAD,
            "<green>Payer un joueur",
            listOf(
                "",
                "<gray>Envoyez de l'argent à un autre joueur.",
                "<red>Frais: ${(HegemoniaEconomy.TRANSACTION_FEE * 100).toInt()}%",
                "",
                "<yellow>Utilisez /money pay <joueur> <montant>"
            )
        ))

        // Retour
        inventory.setItem(22, createBackItem())

        fillBorders(inventory, Material.YELLOW_STAINED_GLASS_PANE)
        player.openInventory(inventory)
    }

    /**
     * Ouvre le menu de la banque
     */
    fun openBankMenu(player: Player) {
        val inventory = createInventory(player, 36, "Banque")
        val session = getOrCreateSession(player)
        session.menuType = MenuType.BANK

        val account = bankService.getOrCreateAccount(player.uniqueId)
        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val maxBank = com.hegemonia.economy.model.PlayerAccount.MAX_BANK_BALANCE

        // Info épargne
        inventory.setItem(4, createItem(
            Material.CHEST,
            "<yellow>Votre épargne",
            listOf(
                "",
                "<white>Solde: <aqua>${formatMoney(account.bankBalance)} $currency",
                "<gray>Limite: ${formatMoney(maxBank)} $currency",
                "",
                "<green>Intérêts: ${(PlayerAccount.INTEREST_RATE * 100)}% par jour"
            )
        ))

        // Déposer
        val depositAmounts = listOf(100.0, 500.0, 1000.0, 5000.0)
        depositAmounts.forEachIndexed { index, amount ->
            val canDeposit = account.balance >= amount && account.bankBalance + amount <= maxBank
            inventory.setItem(19 + index, createItem(
                if (canDeposit) Material.LIME_CONCRETE else Material.RED_CONCRETE,
                "<green>Déposer ${formatMoney(amount)} $currency",
                listOf(
                    "",
                    if (canDeposit) "<yellow>Cliquez pour déposer"
                    else "<red>Solde insuffisant ou limite atteinte"
                )
            ))
        }

        // Déposer tout
        inventory.setItem(23, createItem(
            if (account.balance > 0) Material.EMERALD_BLOCK else Material.BARRIER,
            "<green>Déposer tout",
            listOf(
                "",
                "<white>Montant: ${formatMoney(account.balance)} $currency",
                "",
                if (account.balance > 0) "<yellow>Cliquez pour tout déposer"
                else "<red>Rien à déposer"
            )
        ))

        // Retirer
        val withdrawAmounts = listOf(100.0, 500.0, 1000.0, 5000.0)
        withdrawAmounts.forEachIndexed { index, amount ->
            val canWithdraw = account.bankBalance >= amount
            inventory.setItem(28 + index, createItem(
                if (canWithdraw) Material.ORANGE_CONCRETE else Material.RED_CONCRETE,
                "<red>Retirer ${formatMoney(amount)} $currency",
                listOf(
                    "",
                    if (canWithdraw) "<yellow>Cliquez pour retirer"
                    else "<red>Épargne insuffisante"
                )
            ))
        }

        // Retirer tout
        inventory.setItem(32, createItem(
            if (account.bankBalance > 0) Material.REDSTONE_BLOCK else Material.BARRIER,
            "<red>Retirer tout",
            listOf(
                "",
                "<white>Montant: ${formatMoney(account.bankBalance)} $currency",
                "",
                if (account.bankBalance > 0) "<yellow>Cliquez pour tout retirer"
                else "<red>Rien à retirer"
            )
        ))

        // Retour
        inventory.setItem(31, createBackItem())

        fillBorders(inventory, Material.BLUE_STAINED_GLASS_PANE)
        player.openInventory(inventory)
    }

    // ==================== MENUS MARCHÉ ====================

    /**
     * Ouvre le menu principal du marché
     */
    fun openMarketMenu(player: Player) {
        val inventory = createInventory(player, 36, "Marché")
        val session = getOrCreateSession(player)
        session.menuType = MenuType.MARKET_MAIN

        // Catégories
        var slot = 10
        MarketCategory.entries.forEach { category ->
            val items = marketService.getItemsByCategory(category)
            inventory.setItem(slot, createItem(
                getCategoryMaterial(category),
                "<${getCategoryColor(category)}>${category.icon} ${category.displayName}",
                listOf(
                    "",
                    "<white>${items.size} articles",
                    "",
                    "<yellow>Cliquez pour voir les articles"
                )
            ))
            slot++
            if (slot == 17) slot = 19
            if (slot == 26) slot = 28
        }

        // Vendre item en main
        val itemInHand = player.inventory.itemInMainHand
        if (itemInHand.type != Material.AIR) {
            val marketItem = marketService.getItem(itemInHand.type.name)
            if (marketItem != null) {
                val sellPrice = marketItem.getSellPrice(itemInHand.amount)
                inventory.setItem(31, createItem(
                    Material.HOPPER,
                    "<green>Vendre item en main",
                    listOf(
                        "",
                        "<white>${marketItem.displayName} x${itemInHand.amount}",
                        "<green>Prix: ${formatMoney(sellPrice)} ${HegemoniaEconomy.CURRENCY_SYMBOL}",
                        "",
                        "<yellow>Cliquez pour vendre"
                    )
                ))
            }
        }

        // Retour
        inventory.setItem(35, createBackItem())

        fillBorders(inventory, Material.GREEN_STAINED_GLASS_PANE)
        player.openInventory(inventory)
    }

    /**
     * Ouvre le menu d'une catégorie du marché
     */
    fun openMarketCategory(player: Player, category: MarketCategory, page: Int = 0) {
        val items = marketService.getItemsByCategory(category)
        val totalPages = (items.size + 20) / 21
        val currentPage = page.coerceIn(0, (totalPages - 1).coerceAtLeast(0))

        val inventory = createInventory(player, 54, "${category.icon} ${category.displayName}")
        val session = getOrCreateSession(player)
        session.menuType = MenuType.MARKET_CATEGORY
        session.category = category
        session.page = currentPage

        val currency = HegemoniaEconomy.CURRENCY_SYMBOL

        // Items de la page
        val startIndex = currentPage * 21
        val pageItems = items.drop(startIndex).take(21)

        pageItems.forEachIndexed { index, item ->
            val slot = 10 + index + (index / 7) * 2
            if (slot < 44) {
                inventory.setItem(slot, createItem(
                    Material.getMaterial(item.material) ?: Material.BARRIER,
                    "<white>${item.displayName}",
                    listOf(
                        "",
                        "<red>Achat: ${formatMoney(item.getBuyPrice(1))} $currency",
                        "<green>Vente: ${formatMoney(item.getSellPrice(1))} $currency",
                        "",
                        "<gray>Offre: ${item.supply} | Demande: ${item.demand}",
                        "",
                        "<yellow>Clic gauche: Acheter",
                        "<yellow>Clic droit: Voir détails"
                    )
                ))
            }
        }

        // Navigation
        if (currentPage > 0) {
            inventory.setItem(48, createItem(
                Material.ARROW,
                "<white>Page précédente",
                listOf("<gray>Page ${currentPage}/${totalPages}")
            ))
        }

        inventory.setItem(49, createItem(
            Material.PAPER,
            "<white>Page ${currentPage + 1}/$totalPages",
            emptyList()
        ))

        if (currentPage < totalPages - 1) {
            inventory.setItem(50, createItem(
                Material.ARROW,
                "<white>Page suivante",
                listOf("<gray>Page ${currentPage + 2}/${totalPages}")
            ))
        }

        // Retour
        inventory.setItem(45, createBackItem())

        fillBorders(inventory, Material.LIME_STAINED_GLASS_PANE)
        player.openInventory(inventory)
    }

    /**
     * Ouvre le menu d'achat d'un item
     */
    fun openBuyMenu(player: Player, item: MarketItem) {
        val inventory = createInventory(player, 45, "Acheter: ${item.displayName}")
        val session = getOrCreateSession(player)
        session.menuType = MenuType.MARKET_BUY
        session.selectedItem = item
        session.quantity = 1

        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val balance = bankService.getBalance(player.uniqueId)

        // Item info
        inventory.setItem(4, createItem(
            Material.getMaterial(item.material) ?: Material.BARRIER,
            "<white>${item.displayName}",
            listOf(
                "",
                "<gray>Catégorie: ${item.category.displayName}",
                "<gray>Prix de base: ${formatMoney(item.basePrice)} $currency"
            )
        ))

        // Quantités
        val quantities = listOf(1, 8, 16, 32, 64, 128, 256, 576)
        quantities.forEachIndexed { index, qty ->
            val price = item.getBuyPrice(qty)
            val canAfford = balance >= price
            val slot = 19 + index

            inventory.setItem(slot, createItem(
                if (canAfford) Material.LIME_CONCRETE else Material.RED_CONCRETE,
                "<white>Acheter x$qty",
                listOf(
                    "",
                    "<red>Coût: ${formatMoney(price)} $currency",
                    "",
                    if (canAfford) "<yellow>Cliquez pour acheter"
                    else "<red>Solde insuffisant"
                )
            ))
        }

        // Solde
        inventory.setItem(40, createItem(
            Material.GOLD_INGOT,
            "<gold>Votre solde",
            listOf(
                "",
                "<white>${formatMoney(balance)} $currency"
            )
        ))

        // Retour
        inventory.setItem(36, createBackItem())

        fillBorders(inventory, Material.RED_STAINED_GLASS_PANE)
        player.openInventory(inventory)
    }

    /**
     * Ouvre le menu de vente d'un item
     */
    fun openSellMenu(player: Player) {
        val inventory = createInventory(player, 45, "Vendre des items")
        val session = getOrCreateSession(player)
        session.menuType = MenuType.MARKET_SELL

        val currency = HegemoniaEconomy.CURRENCY_SYMBOL

        // Scan inventory for sellable items
        val sellableItems = mutableListOf<Pair<ItemStack, MarketItem>>()
        player.inventory.contents.filterNotNull().forEach { stack ->
            val marketItem = marketService.getItem(stack.type.name)
            if (marketItem != null) {
                sellableItems.add(stack to marketItem)
            }
        }

        // Display sellable items
        sellableItems.take(28).forEachIndexed { index, (stack, marketItem) ->
            val slot = 10 + index + (index / 7) * 2
            val sellPrice = marketItem.getSellPrice(stack.amount)

            inventory.setItem(slot, createItem(
                stack.type,
                "<white>${marketItem.displayName} x${stack.amount}",
                listOf(
                    "",
                    "<green>Valeur: ${formatMoney(sellPrice)} $currency",
                    "",
                    "<yellow>Cliquez pour vendre"
                )
            ))
        }

        if (sellableItems.isEmpty()) {
            inventory.setItem(22, createItem(
                Material.BARRIER,
                "<red>Aucun item vendable",
                listOf(
                    "",
                    "<gray>Vous n'avez aucun item",
                    "<gray>qui peut être vendu sur le marché."
                )
            ))
        }

        // Retour
        inventory.setItem(40, createBackItem())

        fillBorders(inventory, Material.GREEN_STAINED_GLASS_PANE)
        player.openInventory(inventory)
    }

    // ==================== MENUS HISTORIQUE ====================

    /**
     * Ouvre le menu de l'historique des transactions
     */
    fun openTransactionsMenu(player: Player, page: Int = 0) {
        val transactions = transactionService.getPlayerTransactions(player.uniqueId, 100)
        val totalPages = (transactions.size + 20) / 21
        val currentPage = page.coerceIn(0, (totalPages - 1).coerceAtLeast(0))

        val inventory = createInventory(player, 54, "Historique des transactions")
        val session = getOrCreateSession(player)
        session.menuType = MenuType.TRANSACTIONS
        session.page = currentPage

        val currency = HegemoniaEconomy.CURRENCY_SYMBOL

        // Transactions de la page
        val startIndex = currentPage * 21
        val pageTransactions = transactions.drop(startIndex).take(21)

        pageTransactions.forEachIndexed { index, tx ->
            val slot = 10 + index + (index / 7) * 2
            if (slot < 44) {
                val isIncoming = tx.receiverId == player.uniqueId
                val material = if (isIncoming) Material.LIME_CONCRETE else Material.RED_CONCRETE
                val sign = if (isIncoming) "+" else "-"
                val color = if (isIncoming) "green" else "red"

                inventory.setItem(slot, createItem(
                    material,
                    "<$color>$sign${formatMoney(tx.amount)} $currency",
                    listOf(
                        "",
                        "<white>${tx.type.name}",
                        "<gray>${tx.description}",
                        "",
                        "<gray>${formatDate(tx.timestamp)}"
                    )
                ))
            }
        }

        if (transactions.isEmpty()) {
            inventory.setItem(22, createItem(
                Material.BARRIER,
                "<gray>Aucune transaction",
                listOf("<gray>Vous n'avez pas encore de transactions.")
            ))
        }

        // Navigation
        if (currentPage > 0) {
            inventory.setItem(48, createItem(Material.ARROW, "<white>Page précédente", emptyList()))
        }
        inventory.setItem(49, createItem(Material.PAPER, "<white>Page ${currentPage + 1}/$totalPages", emptyList()))
        if (currentPage < totalPages - 1) {
            inventory.setItem(50, createItem(Material.ARROW, "<white>Page suivante", emptyList()))
        }

        // Retour
        inventory.setItem(45, createBackItem())

        fillBorders(inventory, Material.PURPLE_STAINED_GLASS_PANE)
        player.openInventory(inventory)
    }

    /**
     * Ouvre le classement des joueurs
     */
    fun openTopPlayersMenu(player: Player) {
        val inventory = createInventory(player, 36, "Top Fortunes")
        val session = getOrCreateSession(player)
        session.menuType = MenuType.TOP_PLAYERS

        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val topPlayers = bankService.getTopPlayers(10)

        topPlayers.forEachIndexed { index, (uuid, balance) ->
            val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
            val medal = when (index) {
                0 -> "gold"
                1 -> "gray"
                2 -> "#CD7F32"
                else -> "white"
            }
            val medalIcon = when (index) {
                0 -> "1er"
                1 -> "2ème"
                2 -> "3ème"
                else -> "${index + 1}ème"
            }

            inventory.setItem(10 + index, createItem(
                when (index) {
                    0 -> Material.GOLD_BLOCK
                    1 -> Material.IRON_BLOCK
                    2 -> Material.COPPER_BLOCK
                    else -> Material.PLAYER_HEAD
                },
                "<$medal>$medalIcon - ${offlinePlayer.name ?: "Inconnu"}",
                listOf(
                    "",
                    "<gold>Fortune: ${formatMoney(balance)} $currency"
                )
            ))
        }

        if (topPlayers.isEmpty()) {
            inventory.setItem(13, createItem(
                Material.BARRIER,
                "<gray>Aucun joueur",
                emptyList()
            ))
        }

        // Retour
        inventory.setItem(31, createBackItem())

        fillBorders(inventory, Material.LIGHT_BLUE_STAINED_GLASS_PANE)
        player.openInventory(inventory)
    }

    // ==================== GESTION DES CLICS ====================

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val session = playerSessions[player.uniqueId] ?: return

        // Vérifier que c'est notre inventaire
        val title = event.view.title()
        if (!isOurInventory(title)) return

        event.isCancelled = true

        val slot = event.rawSlot
        if (slot < 0 || slot >= event.inventory.size) return

        when (session.menuType) {
            MenuType.MAIN -> handleMainMenuClick(player, slot)
            MenuType.WALLET -> handleWalletClick(player, slot)
            MenuType.BANK -> handleBankClick(player, slot)
            MenuType.MARKET_MAIN -> handleMarketMainClick(player, slot)
            MenuType.MARKET_CATEGORY -> handleMarketCategoryClick(player, slot, event)
            MenuType.MARKET_ITEM -> handleMarketCategoryClick(player, slot, event)
            MenuType.MARKET_BUY -> handleBuyClick(player, slot)
            MenuType.MARKET_SELL -> handleSellClick(player, slot, event)
            MenuType.TRANSACTIONS -> handleTransactionsClick(player, slot)
            MenuType.TOP_PLAYERS -> handleTopPlayersClick(player, slot)
        }
    }

    private fun handleMainMenuClick(player: Player, slot: Int) {
        when (slot) {
            11 -> openWalletMenu(player)
            13 -> openBankMenu(player)
            15 -> openMarketMenu(player)
            21 -> openTransactionsMenu(player)
            23 -> openTopPlayersMenu(player)
        }
    }

    private fun handleWalletClick(player: Player, slot: Int) {
        when (slot) {
            11 -> openBankMenu(player)
            22 -> openMainMenu(player)
        }
    }

    private fun handleBankClick(player: Player, slot: Int) {
        val account = bankService.getOrCreateAccount(player.uniqueId)
        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val depositAmounts = listOf(100.0, 500.0, 1000.0, 5000.0)
        val withdrawAmounts = listOf(100.0, 500.0, 1000.0, 5000.0)

        when (slot) {
            in 19..22 -> {
                val amount = depositAmounts[slot - 19]
                if (bankService.depositToBank(player.uniqueId, amount)) {
                    player.sendSuccess("Déposé ${formatMoney(amount)} $currency en épargne!")
                    openBankMenu(player)
                } else {
                    player.sendError("Impossible de déposer.")
                }
            }
            23 -> {
                if (account.balance > 0) {
                    val maxDeposit = (com.hegemonia.economy.model.PlayerAccount.MAX_BANK_BALANCE - account.bankBalance)
                        .coerceAtMost(account.balance)
                    if (maxDeposit > 0 && bankService.depositToBank(player.uniqueId, maxDeposit)) {
                        player.sendSuccess("Déposé ${formatMoney(maxDeposit)} $currency en épargne!")
                        openBankMenu(player)
                    }
                }
            }
            in 28..31 -> {
                val amount = withdrawAmounts[slot - 28]
                if (bankService.withdrawFromBank(player.uniqueId, amount)) {
                    player.sendSuccess("Retiré ${formatMoney(amount)} $currency de l'épargne!")
                    openBankMenu(player)
                } else {
                    player.sendError("Impossible de retirer.")
                }
            }
            32 -> {
                if (account.bankBalance > 0 && bankService.withdrawFromBank(player.uniqueId, account.bankBalance)) {
                    player.sendSuccess("Retiré ${formatMoney(account.bankBalance)} $currency de l'épargne!")
                    openBankMenu(player)
                }
            }
            31 -> openMainMenu(player)
        }
    }

    private fun handleMarketMainClick(player: Player, slot: Int) {
        val categories = MarketCategory.entries.toList()
        val categorySlots = listOf(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25)

        when {
            slot == 31 -> {
                // Vendre item en main
                val itemInHand = player.inventory.itemInMainHand
                if (itemInHand.type != Material.AIR) {
                    val marketItem = marketService.getItem(itemInHand.type.name)
                    if (marketItem != null) {
                        executeSell(player, marketItem, itemInHand.amount)
                    }
                }
            }
            slot == 35 -> openMainMenu(player)
            slot in categorySlots -> {
                val index = categorySlots.indexOf(slot)
                if (index < categories.size) {
                    openMarketCategory(player, categories[index])
                }
            }
        }
    }

    private fun handleMarketCategoryClick(player: Player, slot: Int, event: InventoryClickEvent) {
        val session = playerSessions[player.uniqueId] ?: return
        val category = session.category ?: return
        val items = marketService.getItemsByCategory(category)
        val page = session.page

        when (slot) {
            45 -> openMarketMenu(player)
            48 -> if (page > 0) openMarketCategory(player, category, page - 1)
            50 -> openMarketCategory(player, category, page + 1)
            else -> {
                // Calculate item index from slot
                val itemSlots = (10..16).toList() + (19..25).toList() + (28..34).toList()
                val slotIndex = itemSlots.indexOf(slot)
                if (slotIndex >= 0) {
                    val itemIndex = page * 21 + slotIndex
                    if (itemIndex < items.size) {
                        val item = items[itemIndex]
                        if (event.isLeftClick) {
                            openBuyMenu(player, item)
                        } else {
                            // Show item details
                            showItemDetails(player, item)
                        }
                    }
                }
            }
        }
    }

    private fun handleBuyClick(player: Player, slot: Int) {
        val session = playerSessions[player.uniqueId] ?: return
        val item = session.selectedItem ?: return

        val quantities = listOf(1, 8, 16, 32, 64, 128, 256, 576)
        val buySlots = (19..26).toList()

        when {
            slot == 36 -> {
                val category = session.category
                if (category != null) {
                    openMarketCategory(player, category)
                } else {
                    openMarketMenu(player)
                }
            }
            slot in buySlots -> {
                val index = slot - 19
                if (index < quantities.size) {
                    executeBuy(player, item, quantities[index])
                }
            }
        }
    }

    private fun handleSellClick(player: Player, slot: Int, event: InventoryClickEvent) {
        if (slot == 40) {
            openMarketMenu(player)
            return
        }

        // Find which item was clicked
        val sellableItems = mutableListOf<Pair<ItemStack, MarketItem>>()
        player.inventory.contents.filterNotNull().forEach { stack ->
            val marketItem = marketService.getItem(stack.type.name)
            if (marketItem != null) {
                sellableItems.add(stack to marketItem)
            }
        }

        val itemSlots = (10..16).toList() + (19..25).toList() + (28..34).toList() + (37..39).toList()
        val slotIndex = itemSlots.indexOf(slot)
        if (slotIndex >= 0 && slotIndex < sellableItems.size) {
            val (stack, marketItem) = sellableItems[slotIndex]
            executeSell(player, marketItem, stack.amount)
        }
    }

    private fun handleTransactionsClick(player: Player, slot: Int) {
        val session = playerSessions[player.uniqueId] ?: return
        when (slot) {
            45 -> openMainMenu(player)
            48 -> if (session.page > 0) openTransactionsMenu(player, session.page - 1)
            50 -> openTransactionsMenu(player, session.page + 1)
        }
    }

    private fun handleTopPlayersClick(player: Player, slot: Int) {
        if (slot == 31) {
            openMainMenu(player)
        }
    }

    // ==================== ACTIONS ====================

    private fun executeBuy(player: Player, item: MarketItem, quantity: Int) {
        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val totalPrice = item.getBuyPrice(quantity)
        val balance = bankService.getBalance(player.uniqueId)

        if (balance < totalPrice) {
            player.sendError("Solde insuffisant!")
            return
        }

        val material = Material.getMaterial(item.material)
        if (material == null) {
            player.sendError("Item invalide.")
            return
        }

        val itemStack = ItemStack(material, quantity)
        val leftover = player.inventory.addItem(itemStack)
        if (leftover.isNotEmpty()) {
            player.sendError("Inventaire plein!")
            return
        }

        if (bankService.withdraw(player.uniqueId, totalPrice, "Achat marché: ${item.displayName} x$quantity")) {
            marketService.recordPurchase(item.material, quantity)
            player.sendSuccess("Acheté: ${item.displayName} x$quantity pour ${formatMoney(totalPrice)} $currency")
            player.closeInventory()
        } else {
            player.inventory.removeItem(itemStack)
            player.sendError("Échec de l'achat.")
        }
    }

    private fun executeSell(player: Player, item: MarketItem, quantity: Int) {
        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val totalPrice = item.getSellPrice(quantity)
        val material = Material.getMaterial(item.material) ?: return

        // Remove items from inventory
        val toRemove = ItemStack(material, quantity)
        val removed = player.inventory.removeItem(toRemove)
        if (removed.isNotEmpty()) {
            player.sendError("Vous n'avez pas assez d'items.")
            return
        }

        if (bankService.deposit(player.uniqueId, totalPrice, "Vente marché: ${item.displayName} x$quantity")) {
            marketService.recordSale(item.material, quantity)
            player.sendSuccess("Vendu: ${item.displayName} x$quantity pour ${formatMoney(totalPrice)} $currency")
            openMarketMenu(player)
        } else {
            player.inventory.addItem(toRemove)
            player.sendError("Échec de la vente.")
        }
    }

    private fun showItemDetails(player: Player, item: MarketItem) {
        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        player.sendInfo("═══════ ${item.displayName} ═══════")
        player.sendInfo("Catégorie: ${item.category.icon} ${item.category.displayName}")
        player.sendInfo("Prix d'achat: <red>${formatMoney(item.getBuyPrice(1))} $currency")
        player.sendInfo("Prix de vente: <green>${formatMoney(item.getSellPrice(1))} $currency")
        player.sendInfo("Offre: ${item.supply} | Demande: ${item.demand}")
    }

    // ==================== UTILITAIRES ====================

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        // Keep session for a bit in case they reopen
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (player.openInventory.topInventory.holder == null) {
                playerSessions.remove(player.uniqueId)
            }
        }, 20L)
    }

    private fun getOrCreateSession(player: Player): MenuSession {
        return playerSessions.getOrPut(player.uniqueId) {
            MenuSession(MenuType.MAIN)
        }
    }

    private fun createInventory(player: Player, size: Int, title: String): Inventory {
        val component = Component.text("Économie | ").color(NamedTextColor.DARK_GREEN)
            .append(Component.text(title).color(NamedTextColor.GREEN))
        return Bukkit.createInventory(null, size, component)
    }

    private fun isOurInventory(title: Component): Boolean {
        val plainText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title)
        return plainText.startsWith("Économie |")
    }

    private fun createItem(material: Material, name: String, lore: List<String>): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item

        meta.displayName(parseComponent(name).decoration(TextDecoration.ITALIC, false))
        meta.lore(lore.map { parseComponent(it).decoration(TextDecoration.ITALIC, false) })

        item.itemMeta = meta
        return item
    }

    private fun createBackItem(): ItemStack {
        return createItem(
            Material.ARROW,
            "<gray>Retour",
            listOf("<yellow>Cliquez pour revenir")
        )
    }

    private fun parseComponent(text: String): Component {
        return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(text)
    }

    private fun fillBorders(inventory: Inventory, material: Material) {
        val size = inventory.size
        val rows = size / 9

        val filler = ItemStack(material)
        val meta = filler.itemMeta
        meta?.displayName(Component.empty())
        filler.itemMeta = meta

        for (i in 0 until 9) {
            inventory.setItem(i, filler)
            inventory.setItem(size - 9 + i, filler)
        }

        for (row in 1 until rows - 1) {
            inventory.setItem(row * 9, filler)
            inventory.setItem(row * 9 + 8, filler)
        }
    }

    private fun formatMoney(amount: Double): String {
        return String.format("%,.2f", amount)
    }

    private fun formatDate(instant: java.time.Instant): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")
            .withZone(java.time.ZoneId.systemDefault())
        return formatter.format(instant)
    }

    private fun getCategoryMaterial(category: MarketCategory): Material {
        return when (category) {
            MarketCategory.RESOURCES -> Material.IRON_INGOT
            MarketCategory.FOOD -> Material.BREAD
            MarketCategory.WEAPONS -> Material.DIAMOND_SWORD
            MarketCategory.ARMOR -> Material.DIAMOND_CHESTPLATE
            MarketCategory.TOOLS -> Material.DIAMOND_PICKAXE
            MarketCategory.BUILDING -> Material.STONE_BRICKS
            MarketCategory.REDSTONE -> Material.REDSTONE
            MarketCategory.MAGIC -> Material.ENDER_PEARL
            MarketCategory.LUXURY -> Material.NETHERITE_INGOT
            MarketCategory.MISC -> Material.CHEST
        }
    }

    private fun getCategoryColor(category: MarketCategory): String {
        return when (category) {
            MarketCategory.RESOURCES -> "gray"
            MarketCategory.FOOD -> "gold"
            MarketCategory.WEAPONS -> "red"
            MarketCategory.ARMOR -> "blue"
            MarketCategory.TOOLS -> "aqua"
            MarketCategory.BUILDING -> "white"
            MarketCategory.REDSTONE -> "dark_red"
            MarketCategory.MAGIC -> "light_purple"
            MarketCategory.LUXURY -> "dark_purple"
            MarketCategory.MISC -> "gray"
        }
    }
}
