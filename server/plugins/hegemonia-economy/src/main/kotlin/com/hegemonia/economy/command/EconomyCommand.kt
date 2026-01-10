package com.hegemonia.economy.command

import com.hegemonia.core.utils.*
import com.hegemonia.economy.HegemoniaEconomy
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * Commande principale /money
 */
class EconomyCommand(private val plugin: HegemoniaEconomy) : CommandExecutor, TabCompleter {

    private val bankService = plugin.bankService
    private val menuManager by lazy { plugin.menuManager }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Sans argument -> ouvrir le menu ou afficher le solde
        if (args.isEmpty()) {
            if (sender is Player) {
                menuManager.openMainMenu(sender)
            } else {
                sender.sendError("Cette commande est réservée aux joueurs.")
            }
            return true
        }

        when (args[0].lowercase()) {
            "menu", "gui" -> handleMenu(sender)
            "balance", "bal", "solde" -> handleBalance(sender, args)
            "pay", "payer" -> handlePay(sender, args)
            "top", "classement" -> handleTop(sender)
            "help", "?" -> showHelp(sender)
            else -> sender.sendError("Commande inconnue. Utilisez /money help")
        }

        return true
    }

    private fun handleMenu(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendError("Cette commande est réservée aux joueurs.")
            return
        }
        menuManager.openMainMenu(sender)
    }

    private fun handleBalance(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player && args.size < 2) {
            sender.sendError("Usage: /money balance <joueur>")
            return
        }

        val target = if (args.size >= 2) {
            Bukkit.getPlayer(args[1])
        } else {
            sender as? Player
        }

        if (target == null) {
            sender.sendError("Joueur introuvable.")
            return
        }

        val account = bankService.getOrCreateAccount(target.uniqueId)
        val currency = HegemoniaEconomy.CURRENCY_SYMBOL

        if (target == sender) {
            sender.sendInfo("═══════ Votre Portefeuille ═══════")
            sender.sendInfo("Solde: <green>${String.format("%,.2f", account.balance)} $currency")
            sender.sendInfo("En banque: <yellow>${String.format("%,.2f", account.bankBalance)} $currency")
            sender.sendInfo("Total: <gold>${String.format("%,.2f", account.totalBalance)} $currency")
        } else {
            sender.sendInfo("═══════ Portefeuille de ${target.name} ═══════")
            sender.sendInfo("Solde: <green>${String.format("%,.2f", account.balance)} $currency")
        }
    }

    private fun handlePay(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendError("Cette commande est réservée aux joueurs.")
            return
        }

        if (args.size < 3) {
            sender.sendError("Usage: /money pay <joueur> <montant>")
            return
        }

        val target = Bukkit.getPlayer(args[1])
        if (target == null) {
            sender.sendError("Joueur introuvable ou hors ligne.")
            return
        }

        if (target == sender) {
            sender.sendError("Vous ne pouvez pas vous envoyer de l'argent.")
            return
        }

        val amount = args[2].toDoubleOrNull()
        if (amount == null || amount <= 0) {
            sender.sendError("Montant invalide.")
            return
        }

        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val senderBalance = bankService.getBalance(sender.uniqueId)

        if (senderBalance < amount) {
            sender.sendError("Solde insuffisant. Vous avez ${String.format("%,.2f", senderBalance)} $currency")
            return
        }

        val success = bankService.transfer(sender.uniqueId, target.uniqueId, amount, "Paiement de ${sender.name}")
        if (success) {
            val fee = amount * HegemoniaEconomy.TRANSACTION_FEE
            val netAmount = amount - fee

            sender.sendSuccess("Vous avez envoyé ${String.format("%,.2f", amount)} $currency à ${target.name}")
            sender.sendInfo("Frais de transaction: ${String.format("%,.2f", fee)} $currency")

            target.sendSuccess("${sender.name} vous a envoyé ${String.format("%,.2f", netAmount)} $currency!")
        } else {
            sender.sendError("Échec du transfert.")
        }
    }

    private fun handleTop(sender: CommandSender) {
        val topPlayers = bankService.getTopPlayers(10)
        val currency = HegemoniaEconomy.CURRENCY_SYMBOL

        sender.sendInfo("═══════ Top 10 Fortunes ═══════")

        topPlayers.forEachIndexed { index, (uuid, balance) ->
            val player = Bukkit.getOfflinePlayer(uuid)
            val medal = when (index) {
                0 -> "🥇"
                1 -> "🥈"
                2 -> "🥉"
                else -> "${index + 1}."
            }
            sender.sendInfo("$medal ${player.name ?: "Inconnu"}: <gold>${String.format("%,.2f", balance)} $currency")
        }
    }

    private fun showHelp(sender: CommandSender) {
        sender.sendInfo("═══════ Commandes Économie ═══════")
        sender.sendInfo("/money - Ouvre le menu économique")
        sender.sendInfo("/money balance [joueur] - Voir le solde")
        sender.sendInfo("/money pay <joueur> <montant> - Envoyer de l'argent")
        sender.sendInfo("/money top - Classement des fortunes")
        sender.sendInfo("")
        sender.sendInfo("/bank - Gestion bancaire")
        sender.sendInfo("/market - Marché et commerce")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf("menu", "balance", "pay", "top", "help")
                .filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> when (args[0].lowercase()) {
                "pay", "balance" -> Bukkit.getOnlinePlayers()
                    .map { it.name }
                    .filter { it.startsWith(args[1], ignoreCase = true) }
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
