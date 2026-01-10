package com.hegemonia.economy.command

import com.hegemonia.core.utils.*
import com.hegemonia.economy.HegemoniaEconomy
import com.hegemonia.economy.model.MarketCategory
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Commande /market pour le marché
 */
class MarketCommand(private val plugin: HegemoniaEconomy) : CommandExecutor, TabCompleter {

    private val marketService = plugin.marketService
    private val bankService = plugin.bankService
    private val menuManager by lazy { plugin.menuManager }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendError("Cette commande est réservée aux joueurs.")
            return true
        }

        if (args.isEmpty()) {
            menuManager.openMarketMenu(sender)
            return true
        }

        when (args[0].lowercase()) {
            "menu", "gui" -> menuManager.openMarketMenu(sender)
            "buy", "acheter" -> handleBuy(sender, args)
            "sell", "vendre" -> handleSell(sender, args)
            "price", "prix" -> handlePrice(sender, args)
            "list", "liste" -> handleList(sender, args)
            "hand", "main" -> handleHand(sender)
            "help", "?" -> showHelp(sender)
            else -> sender.sendError("Commande inconnue. Utilisez /market help")
        }

        return true
    }

    private fun handleBuy(player: Player, args: Array<out String>) {
        if (args.size < 3) {
            player.sendError("Usage: /market buy <item> <quantité>")
            return
        }

        val materialName = args[1].uppercase()
        val quantity = args[2].toIntOrNull()

        if (quantity == null || quantity <= 0 || quantity > 64 * 9) {
            player.sendError("Quantité invalide (1-576).")
            return
        }

        val item = marketService.getItem(materialName)
        if (item == null) {
            player.sendError("Item non disponible sur le marché: $materialName")
            return
        }

        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val totalPrice = item.getBuyPrice(quantity)
        val balance = bankService.getBalance(player.uniqueId)

        if (balance < totalPrice) {
            player.sendError("Solde insuffisant. Coût: ${String.format("%,.2f", totalPrice)} $currency")
            player.sendInfo("Votre solde: ${String.format("%,.2f", balance)} $currency")
            return
        }

        // Vérifier l'espace inventaire
        val material = Material.getMaterial(materialName)
        if (material == null) {
            player.sendError("Matériau invalide.")
            return
        }

        val itemStack = ItemStack(material, quantity)
        val leftover = player.inventory.addItem(itemStack)
        if (leftover.isNotEmpty()) {
            player.sendError("Inventaire plein! Libérez de l'espace.")
            return
        }

        // Effectuer l'achat
        val success = bankService.withdraw(player.uniqueId, totalPrice, "Achat marché: ${item.displayName} x$quantity")
        if (success) {
            marketService.recordPurchase(materialName, quantity)
            player.sendSuccess("Acheté: ${item.displayName} x$quantity pour ${String.format("%,.2f", totalPrice)} $currency")
        } else {
            // Rembourser les items si échec
            player.inventory.removeItem(itemStack)
            player.sendError("Échec de l'achat.")
        }
    }

    private fun handleSell(player: Player, args: Array<out String>) {
        // Vendre ce qu'on a en main
        val itemInHand = player.inventory.itemInMainHand

        if (itemInHand.type == Material.AIR) {
            player.sendError("Tenez un item en main pour le vendre.")
            return
        }

        val quantity = if (args.size >= 2) {
            if (args[1].equals("all", ignoreCase = true)) {
                itemInHand.amount
            } else {
                args[1].toIntOrNull() ?: itemInHand.amount
            }
        } else {
            itemInHand.amount
        }

        if (quantity <= 0 || quantity > itemInHand.amount) {
            player.sendError("Quantité invalide. Vous avez ${itemInHand.amount} en main.")
            return
        }

        val materialName = itemInHand.type.name
        val item = marketService.getItem(materialName)

        if (item == null) {
            player.sendError("Cet item n'est pas achetable sur le marché.")
            return
        }

        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val totalPrice = item.getSellPrice(quantity)

        // Retirer les items
        if (quantity >= itemInHand.amount) {
            player.inventory.setItemInMainHand(null)
        } else {
            itemInHand.amount -= quantity
        }

        // Créditer le joueur
        val success = bankService.deposit(player.uniqueId, totalPrice, "Vente marché: ${item.displayName} x$quantity")
        if (success) {
            marketService.recordSale(materialName, quantity)
            player.sendSuccess("Vendu: ${item.displayName} x$quantity pour ${String.format("%,.2f", totalPrice)} $currency")
        } else {
            // Rembourser les items si échec
            player.inventory.addItem(ItemStack(itemInHand.type, quantity))
            player.sendError("Échec de la vente.")
        }
    }

    private fun handlePrice(player: Player, args: Array<out String>) {
        val materialName = if (args.size >= 2) {
            args[1].uppercase()
        } else {
            // Utiliser l'item en main
            val itemInHand = player.inventory.itemInMainHand
            if (itemInHand.type == Material.AIR) {
                player.sendError("Usage: /market price <item> ou tenez un item en main")
                return
            }
            itemInHand.type.name
        }

        val item = marketService.getItem(materialName)
        if (item == null) {
            player.sendError("Item non disponible sur le marché: $materialName")
            return
        }

        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val buyPrice = item.getBuyPrice(1)
        val sellPrice = item.getSellPrice(1)

        player.sendInfo("═══════ ${item.displayName} ═══════")
        player.sendInfo("Catégorie: ${item.category.icon} ${item.category.displayName}")
        player.sendInfo("Prix d'achat: <red>${String.format("%,.2f", buyPrice)} $currency")
        player.sendInfo("Prix de vente: <green>${String.format("%,.2f", sellPrice)} $currency")
        player.sendInfo("Prix de base: <gray>${String.format("%,.2f", item.basePrice)} $currency")
        player.sendInfo("Offre/Demande: ${item.supply}/${item.demand}")
    }

    private fun handleHand(player: Player) {
        val itemInHand = player.inventory.itemInMainHand
        if (itemInHand.type == Material.AIR) {
            player.sendError("Tenez un item en main.")
            return
        }

        val materialName = itemInHand.type.name
        val item = marketService.getItem(materialName)

        if (item == null) {
            player.sendWarning("${itemInHand.type.name} n'est pas sur le marché.")
            return
        }

        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val quantity = itemInHand.amount
        val sellPrice = item.getSellPrice(quantity)

        player.sendInfo("Vous avez: ${item.displayName} x$quantity")
        player.sendInfo("Valeur de vente: <green>${String.format("%,.2f", sellPrice)} $currency")
        player.sendInfo("Utilisez /market sell pour vendre")
    }

    private fun handleList(player: Player, args: Array<out String>) {
        val category = if (args.size >= 2) {
            try {
                MarketCategory.valueOf(args[1].uppercase())
            } catch (e: Exception) {
                player.sendError("Catégorie invalide.")
                player.sendInfo("Catégories: ${MarketCategory.entries.joinToString(", ") { it.name.lowercase() }}")
                return
            }
        } else {
            null
        }

        val items = if (category != null) {
            marketService.getItemsByCategory(category)
        } else {
            marketService.getAllItems().take(20)
        }

        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val title = category?.let { "${it.icon} ${it.displayName}" } ?: "Marché"

        player.sendInfo("═══════ $title ═══════")
        items.forEach { item ->
            val buyPrice = item.getBuyPrice(1)
            player.sendInfo("${item.displayName}: <yellow>${String.format("%,.2f", buyPrice)} $currency")
        }

        if (category == null) {
            player.sendInfo("")
            player.sendInfo("<gray>Utilisez /market list <catégorie> pour filtrer")
        }
    }

    private fun showHelp(player: Player) {
        player.sendInfo("═══════ Commandes Marché ═══════")
        player.sendInfo("/market - Ouvre le menu du marché")
        player.sendInfo("/market buy <item> <quantité> - Acheter")
        player.sendInfo("/market sell [quantité|all] - Vendre (item en main)")
        player.sendInfo("/market price [item] - Voir les prix")
        player.sendInfo("/market hand - Info sur l'item en main")
        player.sendInfo("/market list [catégorie] - Liste des items")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf("menu", "buy", "sell", "price", "hand", "list", "help")
                .filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> when (args[0].lowercase()) {
                "buy", "price" -> marketService.getAllItems()
                    .map { it.material.lowercase() }
                    .filter { it.startsWith(args[1], ignoreCase = true) }
                    .take(20)
                "sell" -> listOf("1", "16", "32", "64", "all")
                    .filter { it.startsWith(args[1], ignoreCase = true) }
                "list" -> MarketCategory.entries
                    .map { it.name.lowercase() }
                    .filter { it.startsWith(args[1], ignoreCase = true) }
                else -> emptyList()
            }
            3 -> when (args[0].lowercase()) {
                "buy" -> listOf("1", "16", "32", "64")
                    .filter { it.startsWith(args[2], ignoreCase = true) }
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
