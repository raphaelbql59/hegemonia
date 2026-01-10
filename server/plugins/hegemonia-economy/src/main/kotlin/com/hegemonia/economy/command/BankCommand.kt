package com.hegemonia.economy.command

import com.hegemonia.core.utils.*
import com.hegemonia.economy.HegemoniaEconomy
import com.hegemonia.economy.model.PlayerAccount
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * Commande /bank pour la gestion bancaire
 */
class BankCommand(private val plugin: HegemoniaEconomy) : CommandExecutor, TabCompleter {

    private val bankService = plugin.bankService

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendError("Cette commande est réservée aux joueurs.")
            return true
        }

        if (args.isEmpty()) {
            showBankInfo(sender)
            return true
        }

        when (args[0].lowercase()) {
            "deposit", "déposer" -> handleDeposit(sender, args)
            "withdraw", "retirer" -> handleWithdraw(sender, args)
            "info", "solde" -> showBankInfo(sender)
            "help", "?" -> showHelp(sender)
            else -> sender.sendError("Commande inconnue. Utilisez /bank help")
        }

        return true
    }

    private fun showBankInfo(player: Player) {
        val account = bankService.getOrCreateAccount(player.uniqueId)
        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val maxBank = PlayerAccount.MAX_BANK_BALANCE
        val interestRate = PlayerAccount.INTEREST_RATE * 100

        player.sendInfo("═══════ 🏦 Votre Banque ═══════")
        player.sendInfo("Portefeuille: <green>${String.format("%,.2f", account.balance)} $currency")
        player.sendInfo("Épargne: <yellow>${String.format("%,.2f", account.bankBalance)} $currency")
        player.sendInfo("Limite épargne: <gray>${String.format("%,.0f", maxBank)} $currency")
        player.sendInfo("")
        player.sendInfo("Taux d'intérêt: <aqua>${interestRate}% par jour")
        player.sendInfo("")
        player.sendInfo("<gray>/bank deposit <montant> - Épargner")
        player.sendInfo("<gray>/bank withdraw <montant> - Retirer")
    }

    private fun handleDeposit(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendError("Usage: /bank deposit <montant>")
            return
        }

        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val account = bankService.getOrCreateAccount(player.uniqueId)

        val amount = if (args[1].equals("all", ignoreCase = true)) {
            account.balance
        } else {
            args[1].toDoubleOrNull()
        }

        if (amount == null || amount <= 0) {
            player.sendError("Montant invalide.")
            return
        }

        if (amount > account.balance) {
            player.sendError("Solde insuffisant. Vous avez ${String.format("%,.2f", account.balance)} $currency")
            return
        }

        val maxDeposit = PlayerAccount.MAX_BANK_BALANCE - account.bankBalance
        if (amount > maxDeposit) {
            player.sendError("Vous ne pouvez déposer que ${String.format("%,.2f", maxDeposit)} $currency (limite atteinte)")
            return
        }

        val success = bankService.depositToBank(player.uniqueId, amount)
        if (success) {
            player.sendSuccess("Vous avez déposé ${String.format("%,.2f", amount)} $currency en épargne!")
            player.sendInfo("Nouveau solde épargne: ${String.format("%,.2f", account.bankBalance + amount)} $currency")
        } else {
            player.sendError("Échec du dépôt.")
        }
    }

    private fun handleWithdraw(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendError("Usage: /bank withdraw <montant>")
            return
        }

        val currency = HegemoniaEconomy.CURRENCY_SYMBOL
        val account = bankService.getOrCreateAccount(player.uniqueId)

        val amount = if (args[1].equals("all", ignoreCase = true)) {
            account.bankBalance
        } else {
            args[1].toDoubleOrNull()
        }

        if (amount == null || amount <= 0) {
            player.sendError("Montant invalide.")
            return
        }

        if (amount > account.bankBalance) {
            player.sendError("Épargne insuffisante. Vous avez ${String.format("%,.2f", account.bankBalance)} $currency en banque")
            return
        }

        val success = bankService.withdrawFromBank(player.uniqueId, amount)
        if (success) {
            player.sendSuccess("Vous avez retiré ${String.format("%,.2f", amount)} $currency de l'épargne!")
            player.sendInfo("Nouveau solde: ${String.format("%,.2f", account.balance + amount)} $currency")
        } else {
            player.sendError("Échec du retrait.")
        }
    }

    private fun showHelp(player: Player) {
        player.sendInfo("═══════ Commandes Banque ═══════")
        player.sendInfo("/bank - Voir votre compte bancaire")
        player.sendInfo("/bank deposit <montant|all> - Déposer en épargne")
        player.sendInfo("/bank withdraw <montant|all> - Retirer de l'épargne")
        player.sendInfo("")
        player.sendInfo("<aqua>L'épargne génère des intérêts quotidiens!")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf("deposit", "withdraw", "info", "help")
                .filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> when (args[0].lowercase()) {
                "deposit", "withdraw" -> listOf("100", "500", "1000", "all")
                    .filter { it.startsWith(args[1], ignoreCase = true) }
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
