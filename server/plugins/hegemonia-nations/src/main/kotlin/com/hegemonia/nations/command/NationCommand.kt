package com.hegemonia.nations.command

import com.hegemonia.core.utils.*
import com.hegemonia.nations.HegemoniaNations
import com.hegemonia.nations.model.GovernmentType
import com.hegemonia.nations.model.Nation
import com.hegemonia.nations.model.NationPermission
import com.hegemonia.nations.model.NationRole
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * Commande principale /nation
 */
class NationCommand(private val plugin: HegemoniaNations) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendError("Cette commande est réservée aux joueurs.")
            return true
        }

        if (args.isEmpty()) {
            showHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "help", "?" -> showHelp(sender)
            "create", "créer" -> createNation(sender, args)
            "info", "i" -> showInfo(sender, args)
            "list", "liste" -> listNations(sender, args)
            "join", "rejoindre" -> joinNation(sender, args)
            "leave", "quitter" -> leaveNation(sender)
            "invite", "inviter" -> invitePlayer(sender, args)
            "kick", "expulser" -> kickPlayer(sender, args)
            "promote", "promouvoir" -> promotePlayer(sender, args)
            "demote", "rétrograder" -> demotePlayer(sender, args)
            "deposit", "déposer" -> deposit(sender, args)
            "withdraw", "retirer" -> withdraw(sender, args)
            "balance", "solde" -> showBalance(sender)
            "members", "membres" -> showMembers(sender)
            "relations" -> showRelations(sender)
            "ally", "allier" -> setAlly(sender, args)
            "enemy", "ennemi" -> setEnemy(sender, args)
            "neutral", "neutre" -> setNeutral(sender, args)
            "open", "ouvrir" -> toggleOpen(sender, true)
            "close", "fermer" -> toggleOpen(sender, false)
            "set" -> handleSet(sender, args)
            "disband", "dissoudre" -> disbandNation(sender)
            else -> sender.sendError("Commande inconnue. Utilisez /nation help")
        }

        return true
    }

    private fun showHelp(player: Player) {
        player.sendMini("""
            <gold><bold>═══ HEGEMONIA NATIONS ═══</bold></gold>

            <yellow>/nation create <nom> <tag></yellow> <gray>- Créer une nation</gray>
            <yellow>/nation info [nation]</yellow> <gray>- Informations sur une nation</gray>
            <yellow>/nation list</yellow> <gray>- Liste des nations</gray>
            <yellow>/nation join <nation></yellow> <gray>- Rejoindre une nation ouverte</gray>
            <yellow>/nation leave</yellow> <gray>- Quitter votre nation</gray>
            <yellow>/nation invite <joueur></yellow> <gray>- Inviter un joueur</gray>
            <yellow>/nation kick <joueur></yellow> <gray>- Expulser un membre</gray>
            <yellow>/nation promote <joueur></yellow> <gray>- Promouvoir un membre</gray>
            <yellow>/nation demote <joueur></yellow> <gray>- Rétrograder un membre</gray>
            <yellow>/nation deposit <montant></yellow> <gray>- Déposer au trésor</gray>
            <yellow>/nation withdraw <montant></yellow> <gray>- Retirer du trésor</gray>
            <yellow>/nation members</yellow> <gray>- Liste des membres</gray>
            <yellow>/nation relations</yellow> <gray>- Relations diplomatiques</gray>
            <yellow>/nation ally <nation></yellow> <gray>- Proposer une alliance</gray>
            <yellow>/nation open/close</yellow> <gray>- Ouvrir/fermer le recrutement</gray>
            <yellow>/nation set <option> <valeur></yellow> <gray>- Modifier les paramètres</gray>
        """.trimIndent())
    }

    private fun createNation(player: Player, args: Array<out String>) {
        if (args.size < 3) {
            player.sendError("Usage: /nation create <nom> <tag>")
            return
        }

        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (hegemoniaPlayer?.hasNation == true) {
            player.sendError("Vous êtes déjà membre d'une nation.")
            return
        }

        val name = args[1]
        val tag = args[2]

        // Demander le type de gouvernement
        val govType = if (args.size > 3) {
            GovernmentType.fromString(args[3]) ?: GovernmentType.DEMOCRACY
        } else {
            GovernmentType.DEMOCRACY
        }

        val result = plugin.nationService.createNation(
            name = name,
            tag = tag,
            leaderId = player.uniqueId,
            governmentType = govType
        )

        result.onSuccess { nation ->
            // Mettre à jour le joueur
            plugin.playerService.joinNation(player.uniqueId, nation.id, NationRole.LEADER)

            player.sendSuccess("La nation <gold>${nation.name}</gold> [${nation.tag}] a été créée!")
            player.sendMini("<gray>Type de gouvernement: ${govType.color}${govType.displayName}")
            player.sendMini("<gray>Vous êtes maintenant ${govType.leaderTitle}!")

            // Annonce globale
            Bukkit.broadcast(plugin.parse(
                "<gold><bold>NOUVELLE NATION!</bold></gold> <yellow>${player.name}</yellow> a fondé <gold>${nation.name}</gold>!"
            ))
        }.onFailure { error ->
            player.sendError(error.message ?: "Erreur lors de la création")
        }
    }

    private fun showInfo(player: Player, args: Array<out String>) {
        val nation = if (args.size > 1) {
            plugin.nationService.getNationByName(args[1])
                ?: plugin.nationService.getNationByTag(args[1])
        } else {
            val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
            hegemoniaPlayer?.nationId?.let { plugin.nationService.getNation(it) }
        }

        if (nation == null) {
            player.sendError("Nation non trouvée.")
            return
        }

        val memberCount = plugin.nationService.getMemberCount(nation.id)
        val leader = Bukkit.getOfflinePlayer(nation.leaderId).name ?: "Inconnu"

        player.sendMini("""
            <gold><bold>═══ ${nation.name} [${nation.tag}] ═══</bold></gold>

            <gray>Description:</gray> <white>${nation.description.ifEmpty { "Aucune" }}</white>
            <gray>Gouvernement:</gray> ${nation.governmentType.color}${nation.governmentType.displayName}
            <gray>${nation.governmentType.leaderTitle}:</gray> <yellow>$leader</yellow>
            <gray>Membres:</gray> <white>$memberCount</white>
            <gray>Puissance:</gray> <white>${nation.power}</white>
            <gray>Stabilité:</gray> <white>${nation.stability}%</white>
            <gray>Réputation:</gray> <white>${nation.reputation}</white>
            <gray>Trésor:</gray> <gold>${nation.balance.toCurrency()}</gold>
            <gray>Taxes:</gray> <white>${nation.taxRate}%</white>
            <gray>Recrutement:</gray> ${if (nation.isOpen) "<green>Ouvert" else "<red>Fermé"}
            ${nation.motto?.let { "<gray>Devise:</gray> <italic>\"$it\"</italic>" } ?: ""}
            <gray>Créée le:</gray> <white>${nation.createdAt}</white>
        """.trimIndent())
    }

    private fun listNations(player: Player, args: Array<out String>) {
        val nations = plugin.nationService.getAllNations()
            .sortedByDescending { it.power }

        val page = args.getOrNull(1)?.toIntOrNull() ?: 1
        val pageSize = 10
        val totalPages = nations.totalPages(pageSize)
        val pageNations = nations.page(page - 1, pageSize)

        if (pageNations.isEmpty()) {
            player.sendInfo("Aucune nation trouvée.")
            return
        }

        player.sendMini("<gold><bold>═══ NATIONS (Page $page/$totalPages) ═══</bold></gold>")

        pageNations.forEachIndexed { index, nation ->
            val rank = (page - 1) * pageSize + index + 1
            val memberCount = plugin.nationService.getMemberCount(nation.id)
            player.sendMini(
                "<gray>$rank.</gray> <gold>${nation.name}</gold> <dark_gray>[${nation.tag}]</dark_gray> " +
                        "<gray>- ${nation.governmentType.color}${nation.governmentType.displayName}</gray> " +
                        "<gray>| <white>$memberCount</white> membres | Puissance: <white>${nation.power}</white></gray>"
            )
        }

        if (totalPages > 1) {
            player.sendMini("<gray>Utilisez /nation list <page> pour voir plus</gray>")
        }
    }

    private fun joinNation(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendError("Usage: /nation join <nation>")
            return
        }

        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (hegemoniaPlayer?.hasNation == true) {
            player.sendError("Vous êtes déjà membre d'une nation.")
            return
        }

        val nation = plugin.nationService.getNationByName(args[1])
            ?: plugin.nationService.getNationByTag(args[1])

        if (nation == null) {
            player.sendError("Nation non trouvée.")
            return
        }

        // Vérifier si le joueur a une invitation ou si la nation est ouverte
        val invites = plugin.playerService.getInvites(player.uniqueId)
        val hasInvite = invites.any { it.nationId == nation.id }

        if (!nation.isOpen && !hasInvite) {
            player.sendError("Cette nation est fermée au recrutement. Demandez une invitation.")
            return
        }

        if (plugin.playerService.joinNation(player.uniqueId, nation.id)) {
            player.sendSuccess("Vous avez rejoint <gold>${nation.name}</gold>!")

            // Notifier les membres en ligne
            plugin.nationService.getMembers(nation.id).forEach { (uuid, _) ->
                uuid.player?.sendMini("<green>${player.name}</green> a rejoint la nation!")
            }
        } else {
            player.sendError("Erreur lors de l'adhésion.")
        }
    }

    private fun leaveNation(player: Player) {
        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)

        if (hegemoniaPlayer?.hasNation != true) {
            player.sendError("Vous n'êtes pas membre d'une nation.")
            return
        }

        if (hegemoniaPlayer.isLeader) {
            player.sendError("Le chef ne peut pas quitter. Transférez le leadership ou dissolvez la nation.")
            return
        }

        val nation = plugin.nationService.getNation(hegemoniaPlayer.nationId!!)

        if (plugin.playerService.leaveNation(player.uniqueId)) {
            player.sendSuccess("Vous avez quitté <gold>${nation?.name}</gold>.")
        } else {
            player.sendError("Erreur lors du départ.")
        }
    }

    private fun invitePlayer(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendError("Usage: /nation invite <joueur>")
            return
        }

        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (!hegemoniaPlayer.hasNationPermission(NationPermission.INVITE)) {
            player.sendError("Vous n'avez pas la permission d'inviter.")
            return
        }

        val target = Bukkit.getPlayer(args[1])
        if (target == null) {
            player.sendError("Joueur non trouvé ou hors ligne.")
            return
        }

        val targetPlayer = plugin.playerService.getPlayer(target.uniqueId)
        if (targetPlayer?.hasNation == true) {
            player.sendError("Ce joueur est déjà membre d'une nation.")
            return
        }

        val nationId = hegemoniaPlayer?.nationId ?: return
        val nation = plugin.nationService.getNation(nationId) ?: return

        if (plugin.playerService.sendInvite(target.uniqueId, nationId, player.uniqueId)) {
            player.sendSuccess("Invitation envoyée à <yellow>${target.name}</yellow>.")
            target.sendMini(
                "<gold>${player.name}</gold> vous invite à rejoindre <gold>${nation.name}</gold>! " +
                        "<click:run_command:'/nation join ${nation.name}'><green>[Accepter]</green></click>"
            )
        }
    }

    private fun kickPlayer(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendError("Usage: /nation kick <joueur>")
            return
        }

        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (!hegemoniaPlayer.hasNationPermission(NationPermission.KICK)) {
            player.sendError("Vous n'avez pas la permission d'expulser.")
            return
        }

        val targetName = args[1]
        val target = Bukkit.getOfflinePlayer(targetName)
        val targetPlayer = plugin.playerService.getPlayer(target.uniqueId)

        if (targetPlayer?.nationId != hegemoniaPlayer?.nationId) {
            player.sendError("Ce joueur n'est pas dans votre nation.")
            return
        }

        if (targetPlayer?.role?.priority ?: 0 >= hegemoniaPlayer?.role?.priority ?: 0) {
            player.sendError("Vous ne pouvez pas expulser ce membre.")
            return
        }

        if (plugin.playerService.leaveNation(target.uniqueId)) {
            player.sendSuccess("<yellow>$targetName</yellow> a été expulsé de la nation.")
            target.player?.sendError("Vous avez été expulsé de la nation par ${player.name}.")
        }
    }

    private fun promotePlayer(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendError("Usage: /nation promote <joueur>")
            return
        }

        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (!hegemoniaPlayer.hasNationPermission(NationPermission.PROMOTE)) {
            player.sendError("Vous n'avez pas la permission de promouvoir.")
            return
        }

        val target = Bukkit.getOfflinePlayer(args[1])
        val targetPlayer = plugin.playerService.getPlayer(target.uniqueId)

        if (targetPlayer?.nationId != hegemoniaPlayer?.nationId) {
            player.sendError("Ce joueur n'est pas dans votre nation.")
            return
        }

        val currentRole = targetPlayer?.role ?: return
        val newRole = NationRole.entries
            .filter { it.priority > currentRole.priority && it.priority < (hegemoniaPlayer?.role?.priority ?: 0) }
            .minByOrNull { it.priority }

        if (newRole == null) {
            player.sendError("Ce joueur ne peut pas être promu davantage.")
            return
        }

        if (plugin.playerService.setRole(target.uniqueId, newRole)) {
            player.sendSuccess("<yellow>${target.name}</yellow> a été promu ${newRole.displayName}.")
            target.player?.sendSuccess("Vous avez été promu ${newRole.displayName}!")
        }
    }

    private fun demotePlayer(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendError("Usage: /nation demote <joueur>")
            return
        }

        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (!hegemoniaPlayer.hasNationPermission(NationPermission.DEMOTE)) {
            player.sendError("Vous n'avez pas la permission de rétrograder.")
            return
        }

        val target = Bukkit.getOfflinePlayer(args[1])
        val targetPlayer = plugin.playerService.getPlayer(target.uniqueId)

        if (targetPlayer?.nationId != hegemoniaPlayer?.nationId) {
            player.sendError("Ce joueur n'est pas dans votre nation.")
            return
        }

        val currentRole = targetPlayer?.role ?: return
        val newRole = NationRole.entries
            .filter { it.priority < currentRole.priority }
            .maxByOrNull { it.priority }

        if (newRole == null) {
            player.sendError("Ce joueur ne peut pas être rétrogradé davantage.")
            return
        }

        if (plugin.playerService.setRole(target.uniqueId, newRole)) {
            player.sendSuccess("<yellow>${target.name}</yellow> a été rétrogradé ${newRole.displayName}.")
            target.player?.sendWarning("Vous avez été rétrogradé ${newRole.displayName}.")
        }
    }

    private fun deposit(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendError("Usage: /nation deposit <montant>")
            return
        }

        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (hegemoniaPlayer?.hasNation != true) {
            player.sendError("Vous n'êtes pas membre d'une nation.")
            return
        }

        val amount = args[1].toDoubleOrNull()
        if (amount == null || amount <= 0) {
            player.sendError("Montant invalide.")
            return
        }

        if (hegemoniaPlayer.balance < amount) {
            player.sendError("Fonds insuffisants.")
            return
        }

        if (plugin.playerService.modifyBalance(player.uniqueId, -amount) &&
            plugin.nationService.modifyBalance(hegemoniaPlayer.nationId!!, amount)
        ) {
            player.sendSuccess("Vous avez déposé <gold>${amount.toCurrency()}</gold> dans le trésor national.")
        }
    }

    private fun withdraw(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendError("Usage: /nation withdraw <montant>")
            return
        }

        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (!hegemoniaPlayer.hasNationPermission(NationPermission.WITHDRAW_TREASURY)) {
            player.sendError("Vous n'avez pas accès au trésor.")
            return
        }

        val amount = args[1].toDoubleOrNull()
        if (amount == null || amount <= 0) {
            player.sendError("Montant invalide.")
            return
        }

        val nation = plugin.nationService.getNation(hegemoniaPlayer?.nationId!!)
        if (nation == null || nation.balance < amount) {
            player.sendError("Fonds insuffisants dans le trésor.")
            return
        }

        if (plugin.nationService.modifyBalance(nation.id, -amount) &&
            plugin.playerService.modifyBalance(player.uniqueId, amount)
        ) {
            player.sendSuccess("Vous avez retiré <gold>${amount.toCurrency()}</gold> du trésor national.")
        }
    }

    private fun showBalance(player: Player) {
        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (hegemoniaPlayer?.hasNation != true) {
            player.sendError("Vous n'êtes pas membre d'une nation.")
            return
        }

        val nation = plugin.nationService.getNation(hegemoniaPlayer.nationId!!)
        player.sendMini("<gold>Trésor national:</gold> <white>${nation?.balance?.toCurrency()}</white>")
    }

    private fun showMembers(player: Player) {
        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (hegemoniaPlayer?.hasNation != true) {
            player.sendError("Vous n'êtes pas membre d'une nation.")
            return
        }

        val members = plugin.nationService.getMembers(hegemoniaPlayer.nationId!!)
            .sortedByDescending { it.second.priority }

        player.sendMini("<gold><bold>═══ MEMBRES (${members.size}) ═══</bold></gold>")

        members.forEach { (uuid, role) ->
            val name = Bukkit.getOfflinePlayer(uuid).name ?: "Inconnu"
            val online = uuid.isOnline
            val status = if (online) "<green>●</green>" else "<red>●</red>"
            player.sendMini("$status <yellow>$name</yellow> <gray>- ${role.displayName}</gray>")
        }
    }

    private fun showRelations(player: Player) {
        player.sendInfo("Fonctionnalité en développement.")
    }

    private fun setAlly(player: Player, args: Array<out String>) {
        player.sendInfo("Fonctionnalité en développement.")
    }

    private fun setEnemy(player: Player, args: Array<out String>) {
        player.sendInfo("Fonctionnalité en développement.")
    }

    private fun setNeutral(player: Player, args: Array<out String>) {
        player.sendInfo("Fonctionnalité en développement.")
    }

    private fun toggleOpen(player: Player, open: Boolean) {
        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (!hegemoniaPlayer.hasNationPermission(NationPermission.EDIT_INFO)) {
            player.sendError("Vous n'avez pas la permission de modifier ce paramètre.")
            return
        }

        val nation = plugin.nationService.getNation(hegemoniaPlayer?.nationId!!) ?: return
        val updatedNation = nation.copy(isOpen = open)

        if (plugin.nationService.updateNation(updatedNation)) {
            if (open) {
                player.sendSuccess("Le recrutement est maintenant <green>ouvert</green>.")
            } else {
                player.sendSuccess("Le recrutement est maintenant <red>fermé</red>.")
            }
        }
    }

    private fun handleSet(player: Player, args: Array<out String>) {
        if (args.size < 3) {
            player.sendMini("""
                <gold>Options disponibles:</gold>
                <yellow>/nation set description <texte></yellow>
                <yellow>/nation set motto <devise></yellow>
                <yellow>/nation set tax <taux></yellow>
                <yellow>/nation set color <#hexcode></yellow>
            """.trimIndent())
            return
        }

        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (!hegemoniaPlayer.hasNationPermission(NationPermission.EDIT_INFO)) {
            player.sendError("Vous n'avez pas la permission de modifier les paramètres.")
            return
        }

        val nation = plugin.nationService.getNation(hegemoniaPlayer?.nationId!!) ?: return

        when (args[1].lowercase()) {
            "description", "desc" -> {
                val desc = args.drop(2).joinToString(" ")
                plugin.nationService.updateNation(nation.copy(description = desc))
                player.sendSuccess("Description mise à jour.")
            }

            "motto", "devise" -> {
                val motto = args.drop(2).joinToString(" ")
                plugin.nationService.updateNation(nation.copy(motto = motto))
                player.sendSuccess("Devise mise à jour: <italic>\"$motto\"</italic>")
            }

            "tax", "taxe" -> {
                if (!hegemoniaPlayer.hasNationPermission(NationPermission.SET_TAXES)) {
                    player.sendError("Permission refusée.")
                    return
                }
                val rate = args[2].toDoubleOrNull()
                if (rate == null || rate < 0 || rate > 100) {
                    player.sendError("Le taux doit être entre 0 et 100.")
                    return
                }
                plugin.nationService.updateNation(nation.copy(taxRate = rate))
                player.sendSuccess("Taux d'imposition: <yellow>$rate%</yellow>")
            }

            "color", "couleur" -> {
                val color = args[2]
                if (!color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                    player.sendError("Format de couleur invalide. Utilisez #RRGGBB")
                    return
                }
                plugin.nationService.updateNation(nation.copy(color = color))
                player.sendSuccess("Couleur mise à jour.")
            }

            else -> player.sendError("Option inconnue.")
        }
    }

    private fun disbandNation(player: Player) {
        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (!hegemoniaPlayer.hasNationPermission(NationPermission.DISSOLVE)) {
            player.sendError("Seul le chef peut dissoudre la nation.")
            return
        }

        val nation = plugin.nationService.getNation(hegemoniaPlayer?.nationId!!) ?: return

        // TODO: Ajouter une confirmation
        player.sendWarning("Fonctionnalité en développement. Tapez /nadmin disband pour confirmer.")
    }

    // Extension pour vérifier les permissions sans null
    private fun com.hegemonia.nations.model.HegemoniaPlayer?.hasNationPermission(permission: NationPermission): Boolean {
        return this?.hasNationPermission(permission) ?: false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (sender !is Player) return emptyList()

        return when (args.size) {
            1 -> listOf(
                "help", "create", "info", "list", "join", "leave",
                "invite", "kick", "promote", "demote", "deposit",
                "withdraw", "balance", "members", "relations",
                "ally", "enemy", "neutral", "open", "close", "set"
            ).filter { it.startsWith(args[0].lowercase()) }

            2 -> when (args[0].lowercase()) {
                "info", "join", "ally", "enemy", "neutral" -> {
                    plugin.nationService.getAllNations().map { it.name }
                        .filter { it.lowercase().startsWith(args[1].lowercase()) }
                }

                "invite", "kick", "promote", "demote" -> {
                    Bukkit.getOnlinePlayers().map { it.name }
                        .filter { it.lowercase().startsWith(args[1].lowercase()) }
                }

                "set" -> listOf("description", "motto", "tax", "color")
                    .filter { it.startsWith(args[1].lowercase()) }

                else -> emptyList()
            }

            else -> emptyList()
        }
    }
}
