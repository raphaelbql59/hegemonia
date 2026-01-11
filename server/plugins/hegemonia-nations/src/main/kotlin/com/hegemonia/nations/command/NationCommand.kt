package com.hegemonia.nations.command

import com.hegemonia.core.utils.*
import com.hegemonia.nations.HegemoniaNations
import com.hegemonia.nations.model.GovernmentType
import com.hegemonia.nations.model.Nation
import com.hegemonia.nations.model.NationPermission
import com.hegemonia.nations.model.NationRole
import com.hegemonia.nations.model.ElectionActionResult
import com.hegemonia.nations.model.ElectionStatus
import com.hegemonia.nations.model.GovernmentFeature
import com.hegemonia.nations.model.RelationType
import com.hegemonia.nations.service.ElectionService
import com.hegemonia.nations.service.VassalizationResult
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

    private val menuManager by lazy { plugin.menuManager }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendError("Cette commande est réservée aux joueurs.")
            return true
        }

        // Sans argument -> ouvrir le menu GUI
        if (args.isEmpty()) {
            menuManager.openMainMenu(sender)
            return true
        }

        when (args[0].lowercase()) {
            "menu", "gui" -> menuManager.openMainMenu(sender)
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
            // Commandes Empire/Vassal
            "empire" -> showEmpire(sender)
            "vassalize", "vassaliser" -> vassalizeNation(sender, args)
            "liberate", "libérer" -> liberateNation(sender)
            "tribute", "tribut" -> collectTribute(sender)
            // Commandes Élections
            "election", "élection" -> showElection(sender)
            "candidate", "candidat" -> registerAsCandidate(sender, args)
            "vote", "voter" -> voteForCandidate(sender, args)
            "startelection", "démarrerélection" -> startElection(sender)
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

            <gold><bold>═══ EMPIRE ═══</bold></gold>
            <yellow>/nation empire</yellow> <gray>- Afficher votre empire</gray>
            <yellow>/nation vassalize <nation></yellow> <gray>- Vassaliser une nation</gray>
            <yellow>/nation liberate</yellow> <gray>- Se libérer de la vassalité</gray>
            <yellow>/nation tribute</yellow> <gray>- Collecter les tributs (suzerain)</gray>

            <blue><bold>═══ ÉLECTIONS ═══</bold></blue>
            <yellow>/nation election</yellow> <gray>- Voir l'élection en cours</gray>
            <yellow>/nation candidate [slogan]</yellow> <gray>- Se présenter comme candidat</gray>
            <yellow>/nation vote <candidat></yellow> <gray>- Voter pour un candidat</gray>
            <yellow>/nation startelection</yellow> <gray>- Démarrer une élection (chef)</gray>
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
            Bukkit.broadcast(com.hegemonia.core.HegemoniaCore.get().parse(
                "<gold><bold>NOUVELLE NATION!</bold></gold> <yellow>${player.name}</yellow> a fondé <gold>${nation.name}</gold>!"
            ) as net.kyori.adventure.text.Component)
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

        // Vérifier que la nation existe
        plugin.nationService.getNation(hegemoniaPlayer?.nationId!!) ?: return

        // TODO: Ajouter une confirmation
        player.sendWarning("Fonctionnalité en développement. Tapez /nadmin disband pour confirmer.")
    }

    // ========================================================================
    // Commandes Empire/Vassal
    // ========================================================================

    private fun showEmpire(player: Player) {
        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (hegemoniaPlayer?.hasNation != true) {
            player.sendError("Vous n'êtes pas membre d'une nation.")
            return
        }

        val nationId = hegemoniaPlayer.nationId!!
        val nation = plugin.nationService.getNation(nationId) ?: return
        val empireService = plugin.empireService

        // Récupérer les infos empire
        val isVassal = empireService.isVassal(nationId)
        val isOverlord = empireService.isOverlord(nationId)
        val vassals = empireService.getVassals(nationId)
        val overlordId = empireService.getOverlord(nationId)
        val empirePower = empireService.getEmpirePower(nationId)

        player.sendMini("<gold><bold>═══ EMPIRE DE ${nation.name.uppercase()} ═══</bold></gold>")
        player.sendMini("")

        if (isVassal && overlordId != null) {
            val overlord = plugin.nationService.getNation(overlordId)
            player.sendMini("<dark_purple>⚜ Suzerain:</dark_purple> <gold>${overlord?.name}</gold> [${overlord?.tag}]")
            player.sendMini("<gray>Votre nation paie ${(com.hegemonia.nations.service.EmpireService.VASSAL_TAX_RATE * 100).toInt()}% de tribut")
            player.sendMini("")
        }

        if (isOverlord) {
            player.sendMini("<dark_purple>⚔ Vassaux (${vassals.size}/${com.hegemonia.nations.service.EmpireService.MAX_VASSALS}):</dark_purple>")
            vassals.forEach { vassalId ->
                val vassal = plugin.nationService.getNation(vassalId)
                val tribute = empireService.calculateTribute(vassalId)
                player.sendMini("  <gold>• ${vassal?.name}</gold> <gray>- Tribut: ${tribute.toCurrency()}</gray>")
            }
            player.sendMini("")
        }

        if (!isVassal && !isOverlord) {
            player.sendMini("<gray>Votre nation n'a ni suzerain, ni vassaux.</gray>")
            player.sendMini("<gray>Utilisez <yellow>/nation vassalize <nation></yellow> pour vassaliser une nation.</gray>")
            player.sendMini("")
        }

        player.sendMini("<white>Puissance totale de l'empire:</white> <gold>$empirePower</gold>")

        // Afficher les nations de l'empire
        val empireNations = empireService.getEmpireNations(nationId)
        if (empireNations.size > 1) {
            player.sendMini("")
            player.sendMini("<dark_purple>Nations de l'empire:</dark_purple>")
            empireNations.forEach { empNationId ->
                val empNation = plugin.nationService.getNation(empNationId)
                val role = when {
                    empireService.isOverlord(empNationId) -> "<dark_purple>[Suzerain]</dark_purple>"
                    empireService.isVassal(empNationId) -> "<gold>[Vassal]</gold>"
                    else -> ""
                }
                player.sendMini("  <gray>•</gray> <white>${empNation?.name}</white> $role")
            }
        }
    }

    private fun vassalizeNation(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendError("Usage: /nation vassalize <nation>")
            return
        }

        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (!hegemoniaPlayer.hasNationPermission(NationPermission.MANAGE_RELATIONS)) {
            player.sendError("Vous n'avez pas la permission de gérer les relations diplomatiques.")
            return
        }

        val nationId = hegemoniaPlayer?.nationId ?: return
        val nation = plugin.nationService.getNation(nationId) ?: return

        val targetNation = plugin.nationService.getNationByName(args[1])
            ?: plugin.nationService.getNationByTag(args[1])

        if (targetNation == null) {
            player.sendError("Nation non trouvée.")
            return
        }

        if (targetNation.id == nationId) {
            player.sendError("Vous ne pouvez pas vous vassaliser vous-même.")
            return
        }

        val result = plugin.empireService.canVassalize(nation, targetNation)
        if (result != VassalizationResult.SUCCESS) {
            player.sendError(result.message)
            return
        }

        // Vérifier si en guerre (la vassalisation peut être forcée après une guerre)
        val atWar = plugin.nationService.areAtWar(nationId, targetNation.id)

        if (!atWar) {
            // Envoyer une demande de vassalisation
            // TODO: Implémenter le système de demande diplomatique
            player.sendMini(
                "<gold>${nation.name}</gold> propose de vassaliser <yellow>${targetNation.name}</yellow>."
            )
            player.sendMini("<gray>La nation cible doit accepter ou vous devez la conquérir en guerre.</gray>")

            // Notifier le leader de la nation cible
            Bukkit.getPlayer(targetNation.leaderId)?.let { leader ->
                leader.sendMini(
                    "<gold>${nation.name}</gold> souhaite vous vassaliser! " +
                    "<click:run_command:'/nation accept_vassalize ${nation.name}'><green>[Accepter]</green></click> " +
                    "<click:run_command:'/nation refuse_vassalize ${nation.name}'><red>[Refuser]</red></click>"
                )
            }
            return
        }

        // Vassalisation forcée après guerre
        if (plugin.empireService.vassalize(nationId, targetNation.id)) {
            player.sendSuccess("<gold>${targetNation.name}</gold> est maintenant votre vassal!")

            // Annonce globale
            Bukkit.broadcast(com.hegemonia.core.HegemoniaCore.get().parse(
                "<dark_purple>⚔ VASSALISATION!</dark_purple> <gold>${targetNation.name}</gold> " +
                "est maintenant vassal de <gold>${nation.name}</gold>!"
            ) as net.kyori.adventure.text.Component)
        } else {
            player.sendError("Erreur lors de la vassalisation.")
        }
    }

    private fun liberateNation(player: Player) {
        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (!hegemoniaPlayer.hasNationPermission(NationPermission.MANAGE_RELATIONS)) {
            player.sendError("Vous n'avez pas la permission de gérer les relations diplomatiques.")
            return
        }

        val nationId = hegemoniaPlayer?.nationId ?: return
        val nation = plugin.nationService.getNation(nationId) ?: return

        if (!plugin.empireService.isVassal(nationId)) {
            player.sendError("Votre nation n'est pas vassale.")
            return
        }

        val overlordId = plugin.empireService.getOverlord(nationId) ?: return
        val overlord = plugin.nationService.getNation(overlordId) ?: return

        // Calculer le coût de libération
        val liberationCost = nation.balance * com.hegemonia.nations.service.EmpireService.LIBERATION_COST_MULTIPLIER

        if (nation.balance < liberationCost) {
            player.sendError("Libération coûte ${liberationCost.toCurrency()}. Votre trésor: ${nation.balance.toCurrency()}")
            player.sendMini("<gray>Alternative: Déclarez la guerre à votre suzerain!</gray>")
            return
        }

        // Effectuer la libération
        if (plugin.nationService.withdrawFromTreasury(nationId, liberationCost)) {
            plugin.nationService.depositToTreasury(overlordId, liberationCost)

            if (plugin.empireService.liberateVassal(nationId, forceful = false)) {
                player.sendSuccess("<gold>${nation.name}</gold> s'est libérée de la tutelle de <yellow>${overlord.name}</yellow>!")
                player.sendMini("<gray>Coût: ${liberationCost.toCurrency()}</gray>")

                // Notifier le suzerain
                Bukkit.getPlayer(overlord.leaderId)?.sendWarning(
                    "<gold>${nation.name}</gold> a payé pour se libérer de votre tutelle!"
                )

                // Annonce globale
                Bukkit.broadcast(com.hegemonia.core.HegemoniaCore.get().parse(
                    "<gold>⚜ LIBÉRATION!</gold> <yellow>${nation.name}</yellow> s'est libérée de <gold>${overlord.name}</gold>!"
                ) as net.kyori.adventure.text.Component)
            }
        } else {
            player.sendError("Erreur lors de la libération.")
        }
    }

    private fun collectTribute(player: Player) {
        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (!hegemoniaPlayer.hasNationPermission(NationPermission.ACCESS_TREASURY)) {
            player.sendError("Vous n'avez pas accès au trésor.")
            return
        }

        val nationId = hegemoniaPlayer?.nationId ?: return
        val nation = plugin.nationService.getNation(nationId) ?: return

        if (!plugin.empireService.isOverlord(nationId)) {
            player.sendError("Votre nation n'a pas de vassaux.")
            return
        }

        val totalTribute = plugin.empireService.collectTributes(nationId)

        if (totalTribute > 0) {
            player.sendSuccess("Tributs collectés: <gold>${totalTribute.toCurrency()}</gold>")

            // Notifier les vassaux
            plugin.empireService.getVassals(nationId).forEach { vassalId ->
                val tributeAmount = plugin.empireService.calculateTribute(vassalId)
                plugin.nationService.getMembers(vassalId).forEach { (uuid, _) ->
                    uuid.player?.sendWarning("Tribut de ${tributeAmount.toCurrency()} payé à <gold>${nation.name}</gold>")
                }
            }
        } else {
            player.sendInfo("Aucun tribut à collecter (vassaux sans fonds).")
        }
    }

    // ========================================================================
    // Commandes Élections
    // ========================================================================

    private fun showElection(player: Player) {
        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (hegemoniaPlayer?.hasNation != true) {
            player.sendError("Vous n'êtes pas membre d'une nation.")
            return
        }

        val nationId = hegemoniaPlayer.nationId!!
        val nation = plugin.nationService.getNation(nationId) ?: return
        val electionService = plugin.electionService

        // Vérifier si la nation supporte les élections
        if (!nation.governmentType.features.contains(GovernmentFeature.ELECTIONS) &&
            !nation.governmentType.features.contains(GovernmentFeature.DIRECT_DEMOCRACY)) {
            player.sendError("Votre type de gouvernement (${nation.governmentType.displayName}) ne supporte pas les élections.")
            return
        }

        val election = electionService.getActiveElection(nationId)

        if (election == null) {
            player.sendMini("<blue><bold>═══ ÉLECTIONS ═══</bold></blue>")
            player.sendMini("")
            player.sendMini("<gray>Aucune élection en cours.</gray>")
            player.sendMini("<gray>Le chef peut démarrer une élection avec <yellow>/nation startelection</yellow></gray>")

            // Afficher l'historique
            val history = electionService.getElectionHistory(nationId, 3)
            if (history.isNotEmpty()) {
                player.sendMini("")
                player.sendMini("<blue>Dernières élections:</blue>")
                history.filter { it.status == ElectionStatus.COMPLETED }.forEach { past ->
                    val winnerName = past.winnerId?.let { Bukkit.getOfflinePlayer(it).name } ?: "Inconnu"
                    player.sendMini("  <gray>•</gray> <white>$winnerName</white> <gray>(${past.totalVotes} votes)</gray>")
                }
            }
            return
        }

        player.sendMini("<blue><bold>═══ ÉLECTION EN COURS ═══</bold></blue>")
        player.sendMini("")
        player.sendMini("<white>Statut:</white> <yellow>${election.status.displayName}</yellow>")
        player.sendMini("<white>Position:</white> <yellow>${nation.governmentType.leaderTitle}</yellow>")

        when (election.status) {
            ElectionStatus.REGISTRATION -> {
                val remaining = java.time.Duration.between(java.time.Instant.now(), election.registrationEndsAt)
                player.sendMini("<white>Fin des inscriptions:</white> <yellow>${formatDuration(remaining)}</yellow>")
                player.sendMini("")
                player.sendMini("<gray>Inscrivez-vous avec <yellow>/nation candidate [slogan]</yellow></gray>")
            }
            ElectionStatus.VOTING -> {
                val remaining = java.time.Duration.between(java.time.Instant.now(), election.votingEndsAt)
                player.sendMini("<white>Fin du vote:</white> <yellow>${formatDuration(remaining)}</yellow>")
                player.sendMini("")
                player.sendMini("<gray>Votez avec <yellow>/nation vote <candidat></yellow></gray>")

                val hasVoted = electionService.hasVoted(election.id, player.uniqueId)
                if (hasVoted) {
                    player.sendMini("<green>✓ Vous avez déjà voté</green>")
                }
            }
            else -> {}
        }

        // Afficher les candidats
        val candidates = electionService.getCandidates(election.id).filter { !it.withdrawn }
        player.sendMini("")
        player.sendMini("<blue>Candidats (${candidates.size}):</blue>")

        candidates.sortedByDescending { it.voteCount }.forEach { candidate ->
            val name = Bukkit.getOfflinePlayer(candidate.playerId).name ?: "Inconnu"
            val votes = if (election.status == ElectionStatus.VOTING || election.status == ElectionStatus.COMPLETED)
                " <gray>(${candidate.voteCount} votes)</gray>" else ""
            val slogan = candidate.slogan?.let { " - <italic>\"$it\"</italic>" } ?: ""
            player.sendMini("  <yellow>• $name</yellow>$votes$slogan")
        }
    }

    private fun registerAsCandidate(player: Player, args: Array<out String>) {
        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (hegemoniaPlayer?.hasNation != true) {
            player.sendError("Vous n'êtes pas membre d'une nation.")
            return
        }

        val nationId = hegemoniaPlayer.nationId!!
        val slogan = if (args.size > 1) args.drop(1).joinToString(" ") else null

        val result = plugin.electionService.registerCandidate(nationId, player.uniqueId, slogan)

        when (result) {
            ElectionActionResult.SUCCESS -> {
                player.sendSuccess("Vous êtes maintenant candidat(e) aux élections!")
                if (slogan != null) {
                    player.sendMini("<gray>Slogan: <italic>\"$slogan\"</italic></gray>")
                }
            }
            else -> player.sendError(result.message)
        }
    }

    private fun voteForCandidate(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendError("Usage: /nation vote <candidat>")
            return
        }

        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (hegemoniaPlayer?.hasNation != true) {
            player.sendError("Vous n'êtes pas membre d'une nation.")
            return
        }

        val nationId = hegemoniaPlayer.nationId!!
        val candidateName = args[1]

        // Trouver le candidat
        val election = plugin.electionService.getActiveElection(nationId)
        if (election == null) {
            player.sendError("Aucune élection en cours.")
            return
        }

        val candidates = plugin.electionService.getCandidates(election.id)
        val candidate = candidates.find {
            val name = Bukkit.getOfflinePlayer(it.playerId).name
            name?.equals(candidateName, ignoreCase = true) == true && !it.withdrawn
        }

        if (candidate == null) {
            player.sendError("Candidat non trouvé: $candidateName")
            player.sendMini("<gray>Candidats disponibles: ${candidates.filter { !it.withdrawn }.map {
                Bukkit.getOfflinePlayer(it.playerId).name
            }.joinToString(", ")}</gray>")
            return
        }

        val result = plugin.electionService.vote(nationId, player.uniqueId, candidate.playerId)

        when (result) {
            ElectionActionResult.SUCCESS -> {
                val name = Bukkit.getOfflinePlayer(candidate.playerId).name
                player.sendSuccess("Vous avez voté pour <yellow>$name</yellow>!")
            }
            else -> player.sendError(result.message)
        }
    }

    private fun startElection(player: Player) {
        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)
        if (!hegemoniaPlayer.hasNationPermission(NationPermission.EDIT_INFO)) {
            player.sendError("Seul le chef peut démarrer une élection.")
            return
        }

        val nationId = hegemoniaPlayer?.nationId ?: return
        val nation = plugin.nationService.getNation(nationId) ?: return

        // Vérifier si la nation supporte les élections
        if (!nation.governmentType.features.contains(GovernmentFeature.ELECTIONS) &&
            !nation.governmentType.features.contains(GovernmentFeature.DIRECT_DEMOCRACY)) {
            player.sendError("Votre type de gouvernement ne supporte pas les élections.")
            return
        }

        if (plugin.electionService.hasActiveElection(nationId)) {
            player.sendError("Une élection est déjà en cours.")
            return
        }

        if (plugin.electionService.startElection(nationId)) {
            player.sendSuccess("Élection démarrée!")
            player.sendMini("<gray>Phase d'inscription: ${ElectionService.REGISTRATION_DURATION.toDays()} jours</gray>")
            player.sendMini("<gray>Phase de vote: ${ElectionService.VOTING_DURATION.toDays()} jours</gray>")
        } else {
            player.sendError("Impossible de démarrer l'élection.")
        }
    }

    private fun formatDuration(duration: java.time.Duration): String {
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60

        return when {
            days > 0 -> "${days}j ${hours}h"
            hours > 0 -> "${hours}h ${minutes}min"
            else -> "${minutes}min"
        }
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
                "ally", "enemy", "neutral", "open", "close", "set",
                "empire", "vassalize", "liberate", "tribute",
                "election", "candidate", "vote", "startelection"
            ).filter { it.startsWith(args[0].lowercase()) }

            2 -> when (args[0].lowercase()) {
                "info", "join", "ally", "enemy", "neutral", "vassalize" -> {
                    plugin.nationService.getAllNations().map { it.name }
                        .filter { it.lowercase().startsWith(args[1].lowercase()) }
                }

                "invite", "kick", "promote", "demote" -> {
                    Bukkit.getOnlinePlayers().map { it.name }
                        .filter { it.lowercase().startsWith(args[1].lowercase()) }
                }

                "set" -> listOf("description", "motto", "tax", "color")
                    .filter { it.startsWith(args[1].lowercase()) }

                "vote" -> {
                    // Complétion des candidats
                    val hegemoniaPlayer = plugin.playerService.getPlayer((sender as Player).uniqueId)
                    val nationId = hegemoniaPlayer?.nationId
                    if (nationId != null) {
                        val election = plugin.electionService.getActiveElection(nationId)
                        if (election != null) {
                            plugin.electionService.getCandidates(election.id)
                                .filter { !it.withdrawn }
                                .mapNotNull { Bukkit.getOfflinePlayer(it.playerId).name }
                                .filter { it.lowercase().startsWith(args[1].lowercase()) }
                        } else emptyList()
                    } else emptyList()
                }

                else -> emptyList()
            }

            else -> emptyList()
        }
    }
}
