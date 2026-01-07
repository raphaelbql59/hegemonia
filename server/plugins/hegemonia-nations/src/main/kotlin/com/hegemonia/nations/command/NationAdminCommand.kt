package com.hegemonia.nations.command

import com.hegemonia.core.utils.*
import com.hegemonia.nations.HegemoniaNations
import com.hegemonia.nations.model.GovernmentType
import com.hegemonia.nations.model.NationRole
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * Commandes administratives /nadmin
 */
class NationAdminCommand(private val plugin: HegemoniaNations) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("hegemonia.nations.admin")) {
            sender.sendError("Permission refusée.")
            return true
        }

        if (args.isEmpty()) {
            showHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "help" -> showHelp(sender)
            "reload" -> reloadConfig(sender)
            "create" -> adminCreateNation(sender, args)
            "delete", "disband" -> adminDeleteNation(sender, args)
            "setleader" -> setLeader(sender, args)
            "setbalance" -> setBalance(sender, args)
            "addbalance" -> addBalance(sender, args)
            "setpower" -> setPower(sender, args)
            "setstability" -> setStability(sender, args)
            "forcejoin" -> forceJoin(sender, args)
            "forceleave" -> forceLeave(sender, args)
            "setgovernment" -> setGovernment(sender, args)
            "tp" -> teleportToNation(sender, args)
            "debug" -> toggleDebug(sender)
            else -> sender.sendError("Commande inconnue. Utilisez /nadmin help")
        }

        return true
    }

    private fun showHelp(sender: CommandSender) {
        sender.sendMini("""
            <red><bold>═══ NATION ADMIN ═══</bold></red>

            <yellow>/nadmin reload</yellow> <gray>- Recharger la configuration</gray>
            <yellow>/nadmin create <nom> <tag> <leader></yellow> <gray>- Créer une nation</gray>
            <yellow>/nadmin delete <nation></yellow> <gray>- Supprimer une nation</gray>
            <yellow>/nadmin setleader <nation> <joueur></yellow> <gray>- Définir le leader</gray>
            <yellow>/nadmin setbalance <nation> <montant></yellow> <gray>- Définir le solde</gray>
            <yellow>/nadmin addbalance <nation> <montant></yellow> <gray>- Ajouter au solde</gray>
            <yellow>/nadmin setpower <nation> <puissance></yellow> <gray>- Définir la puissance</gray>
            <yellow>/nadmin setstability <nation> <stabilité></yellow> <gray>- Définir la stabilité</gray>
            <yellow>/nadmin forcejoin <joueur> <nation></yellow> <gray>- Forcer un joueur à rejoindre</gray>
            <yellow>/nadmin forceleave <joueur></yellow> <gray>- Forcer un joueur à quitter</gray>
            <yellow>/nadmin setgovernment <nation> <type></yellow> <gray>- Changer le gouvernement</gray>
            <yellow>/nadmin debug</yellow> <gray>- Activer le mode debug</gray>
        """.trimIndent())
    }

    private fun reloadConfig(sender: CommandSender) {
        plugin.reloadConfig()
        sender.sendSuccess("Configuration rechargée.")
    }

    private fun adminCreateNation(sender: CommandSender, args: Array<out String>) {
        if (args.size < 4) {
            sender.sendError("Usage: /nadmin create <nom> <tag> <leader>")
            return
        }

        val name = args[1]
        val tag = args[2]
        val leaderName = args[3]
        val leader = Bukkit.getOfflinePlayer(leaderName)

        if (!leader.hasPlayedBefore() && !leader.isOnline) {
            sender.sendError("Joueur non trouvé: $leaderName")
            return
        }

        val govType = if (args.size > 4) {
            GovernmentType.fromString(args[4]) ?: GovernmentType.DEMOCRACY
        } else {
            GovernmentType.DEMOCRACY
        }

        val result = plugin.nationService.createNation(
            name = name,
            tag = tag,
            leaderId = leader.uniqueId,
            governmentType = govType
        )

        result.onSuccess { nation ->
            plugin.playerService.joinNation(leader.uniqueId, nation.id, NationRole.LEADER)
            sender.sendSuccess("Nation <gold>${nation.name}</gold> créée avec ${leader.name} comme leader.")
        }.onFailure { error ->
            sender.sendError(error.message ?: "Erreur lors de la création")
        }
    }

    private fun adminDeleteNation(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendError("Usage: /nadmin delete <nation>")
            return
        }

        val nation = plugin.nationService.getNationByName(args[1])
            ?: plugin.nationService.getNationByTag(args[1])

        if (nation == null) {
            sender.sendError("Nation non trouvée.")
            return
        }

        // Retirer tous les membres
        plugin.nationService.getMembers(nation.id).forEach { (uuid, _) ->
            plugin.playerService.leaveNation(uuid)
        }

        if (plugin.nationService.deleteNation(nation.id)) {
            sender.sendSuccess("Nation <gold>${nation.name}</gold> supprimée.")
            Bukkit.broadcast(plugin.parse(
                "<red>La nation <gold>${nation.name}</gold> a été dissoute par un administrateur.</red>"
            ))
        } else {
            sender.sendError("Erreur lors de la suppression.")
        }
    }

    private fun setLeader(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendError("Usage: /nadmin setleader <nation> <joueur>")
            return
        }

        val nation = plugin.nationService.getNationByName(args[1])
        if (nation == null) {
            sender.sendError("Nation non trouvée.")
            return
        }

        val newLeader = Bukkit.getOfflinePlayer(args[2])
        val newLeaderData = plugin.playerService.getPlayer(newLeader.uniqueId)

        if (newLeaderData?.nationId != nation.id) {
            sender.sendError("Ce joueur n'est pas membre de cette nation.")
            return
        }

        // Changer les rôles
        plugin.playerService.setRole(nation.leaderId, NationRole.CITIZEN)
        plugin.playerService.setRole(newLeader.uniqueId, NationRole.LEADER)

        // Mettre à jour la nation
        plugin.nationService.updateNation(nation.copy(leaderId = newLeader.uniqueId))

        sender.sendSuccess("${newLeader.name} est maintenant le leader de ${nation.name}.")
    }

    private fun setBalance(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendError("Usage: /nadmin setbalance <nation> <montant>")
            return
        }

        val nation = plugin.nationService.getNationByName(args[1])
        if (nation == null) {
            sender.sendError("Nation non trouvée.")
            return
        }

        val amount = args[2].toDoubleOrNull()
        if (amount == null) {
            sender.sendError("Montant invalide.")
            return
        }

        val difference = amount - nation.balance
        if (plugin.nationService.modifyBalance(nation.id, difference)) {
            sender.sendSuccess("Solde de ${nation.name} défini à ${amount.toCurrency()}.")
        }
    }

    private fun addBalance(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendError("Usage: /nadmin addbalance <nation> <montant>")
            return
        }

        val nation = plugin.nationService.getNationByName(args[1])
        if (nation == null) {
            sender.sendError("Nation non trouvée.")
            return
        }

        val amount = args[2].toDoubleOrNull()
        if (amount == null) {
            sender.sendError("Montant invalide.")
            return
        }

        if (plugin.nationService.modifyBalance(nation.id, amount)) {
            sender.sendSuccess("${amount.toCurrency()} ajouté au trésor de ${nation.name}.")
        }
    }

    private fun setPower(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendError("Usage: /nadmin setpower <nation> <puissance>")
            return
        }

        val nation = plugin.nationService.getNationByName(args[1])
        if (nation == null) {
            sender.sendError("Nation non trouvée.")
            return
        }

        val power = args[2].toIntOrNull()
        if (power == null || power < 0) {
            sender.sendError("Puissance invalide.")
            return
        }

        if (plugin.nationService.updateNation(nation.copy(power = power))) {
            sender.sendSuccess("Puissance de ${nation.name} définie à $power.")
        }
    }

    private fun setStability(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendError("Usage: /nadmin setstability <nation> <stabilité>")
            return
        }

        val nation = plugin.nationService.getNationByName(args[1])
        if (nation == null) {
            sender.sendError("Nation non trouvée.")
            return
        }

        val stability = args[2].toIntOrNull()
        if (stability == null || stability < 0 || stability > 100) {
            sender.sendError("Stabilité invalide (0-100).")
            return
        }

        if (plugin.nationService.updateNation(nation.copy(stability = stability))) {
            sender.sendSuccess("Stabilité de ${nation.name} définie à $stability%.")
        }
    }

    private fun forceJoin(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendError("Usage: /nadmin forcejoin <joueur> <nation>")
            return
        }

        val player = Bukkit.getOfflinePlayer(args[1])
        val nation = plugin.nationService.getNationByName(args[2])

        if (nation == null) {
            sender.sendError("Nation non trouvée.")
            return
        }

        // Quitter l'ancienne nation si nécessaire
        val playerData = plugin.playerService.getPlayer(player.uniqueId)
        if (playerData?.hasNation == true) {
            plugin.playerService.leaveNation(player.uniqueId)
        }

        if (plugin.playerService.joinNation(player.uniqueId, nation.id)) {
            sender.sendSuccess("${player.name} a été ajouté à ${nation.name}.")
            player.player?.sendInfo("Vous avez été ajouté à ${nation.name} par un administrateur.")
        }
    }

    private fun forceLeave(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendError("Usage: /nadmin forceleave <joueur>")
            return
        }

        val player = Bukkit.getOfflinePlayer(args[1])

        if (plugin.playerService.leaveNation(player.uniqueId)) {
            sender.sendSuccess("${player.name} a quitté sa nation.")
            player.player?.sendWarning("Vous avez été retiré de votre nation par un administrateur.")
        } else {
            sender.sendError("Ce joueur n'est pas dans une nation.")
        }
    }

    private fun setGovernment(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendError("Usage: /nadmin setgovernment <nation> <type>")
            sender.sendMini("<gray>Types: ${GovernmentType.entries.joinToString(", ") { it.name }}</gray>")
            return
        }

        val nation = plugin.nationService.getNationByName(args[1])
        if (nation == null) {
            sender.sendError("Nation non trouvée.")
            return
        }

        val govType = GovernmentType.fromString(args[2])
        if (govType == null) {
            sender.sendError("Type de gouvernement invalide.")
            sender.sendMini("<gray>Types: ${GovernmentType.entries.joinToString(", ") { it.name }}</gray>")
            return
        }

        if (plugin.nationService.updateNation(nation.copy(governmentType = govType))) {
            sender.sendSuccess("${nation.name} est maintenant une ${govType.displayName}.")
        }
    }

    private fun teleportToNation(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendError("Cette commande est réservée aux joueurs.")
            return
        }

        sender.sendInfo("Fonctionnalité en développement.")
    }

    private fun toggleDebug(sender: CommandSender) {
        sender.sendInfo("Mode debug activé/désactivé.")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (!sender.hasPermission("hegemonia.nations.admin")) return emptyList()

        return when (args.size) {
            1 -> listOf(
                "help", "reload", "create", "delete", "setleader",
                "setbalance", "addbalance", "setpower", "setstability",
                "forcejoin", "forceleave", "setgovernment", "debug"
            ).filter { it.startsWith(args[0].lowercase()) }

            2 -> when (args[0].lowercase()) {
                "delete", "setleader", "setbalance", "addbalance",
                "setpower", "setstability", "setgovernment" -> {
                    plugin.nationService.getAllNations().map { it.name }
                        .filter { it.lowercase().startsWith(args[1].lowercase()) }
                }

                "forcejoin", "forceleave" -> {
                    Bukkit.getOnlinePlayers().map { it.name }
                        .filter { it.lowercase().startsWith(args[1].lowercase()) }
                }

                else -> emptyList()
            }

            3 -> when (args[0].lowercase()) {
                "setleader", "create" -> {
                    Bukkit.getOnlinePlayers().map { it.name }
                        .filter { it.lowercase().startsWith(args[2].lowercase()) }
                }

                "forcejoin" -> {
                    plugin.nationService.getAllNations().map { it.name }
                        .filter { it.lowercase().startsWith(args[2].lowercase()) }
                }

                "setgovernment" -> {
                    GovernmentType.entries.map { it.name }
                        .filter { it.lowercase().startsWith(args[2].lowercase()) }
                }

                else -> emptyList()
            }

            else -> emptyList()
        }
    }

    private fun CommandSender.parse(message: String) = plugin.parse(message)

    private fun plugin.parse(message: String) =
        com.hegemonia.core.HegemoniaCore.get().parse(message)
}
