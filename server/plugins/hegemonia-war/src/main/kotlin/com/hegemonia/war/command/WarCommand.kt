package com.hegemonia.war.command

import com.hegemonia.core.utils.*
import com.hegemonia.war.HegemoniaWar
import com.hegemonia.war.model.WarGoal
import com.hegemonia.war.model.WarSide
import com.hegemonia.war.model.WarStatus
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * Commande principale de gestion des guerres
 */
class WarCommand(private val plugin: HegemoniaWar) : CommandExecutor, TabCompleter {

    private val warService = plugin.warService
    private val nationBridge by lazy { plugin.nationBridge }
    private val menuManager by lazy { plugin.menuManager }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Sans argument -> ouvrir le menu GUI (plus intuitif)
        if (args.isEmpty()) {
            if (sender is Player) {
                menuManager.openMainMenu(sender)
            } else {
                sendHelp(sender)
            }
            return true
        }

        when (args[0].lowercase()) {
            "menu", "gui" -> handleMenu(sender)
            "declare", "déclarer" -> handleDeclare(sender, args)
            "info" -> handleInfo(sender, args)
            "list", "liste" -> handleList(sender, args)
            "peace", "paix" -> handlePeace(sender, args)
            "accept", "accepter" -> handleAccept(sender, args)
            "reject", "refuser" -> handleReject(sender, args)
            "surrender", "capituler" -> handleSurrender(sender, args)
            "join", "rejoindre" -> handleJoin(sender, args)
            "history", "historique" -> handleHistory(sender, args)
            "score" -> handleScore(sender, args)
            "status", "statut" -> handleStatus(sender, args)
            "help", "aide" -> sendHelp(sender)
            else -> {
                sender.error("Sous-commande inconnue: ${args[0]}")
                sendHelp(sender)
            }
        }
        return true
    }

    private fun handleDeclare(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }

        if (!nationBridge.isAvailable()) {
            sender.error("Le système de nations n'est pas disponible.")
            return
        }

        if (args.size < 3) {
            sender.error("Usage: /war declare <nation> <objectif> [raison]")
            sender.info("Objectifs: ${WarGoal.entries.joinToString(", ") { it.name.lowercase() }}")
            return
        }

        val targetNationName = args[1]
        val goalName = args[2]
        val reason = if (args.size > 3) args.drop(3).joinToString(" ") else "Déclaration de guerre"

        val goal = WarGoal.fromString(goalName)
        if (goal == null) {
            sender.error("Objectif de guerre invalide: $goalName")
            sender.info("Objectifs valides: ${WarGoal.entries.joinToString(", ") { it.name.lowercase() }}")
            return
        }

        // Récupérer la nation de l'attaquant
        val attackerNationId = nationBridge.getPlayerNationId(sender.uniqueId)
        if (attackerNationId == null) {
            sender.error("Vous devez appartenir à une nation pour déclarer la guerre.")
            return
        }

        // Vérifier les permissions
        if (!nationBridge.canDeclareWar(sender.uniqueId)) {
            sender.error("Vous n'avez pas les permissions pour déclarer la guerre.")
            sender.info("Seuls les Leaders, Ministres et Généraux peuvent déclarer la guerre.")
            return
        }

        // Récupérer la nation cible (par nom ou tag)
        var defenderNationId = nationBridge.getNationIdByName(targetNationName)
        if (defenderNationId == null) {
            defenderNationId = nationBridge.getNationIdByTag(targetNationName.uppercase())
        }
        if (defenderNationId == null) {
            sender.error("Nation introuvable: $targetNationName")
            sender.info("Essayez avec le nom complet ou le tag de la nation.")
            return
        }

        // Vérifications
        if (attackerNationId == defenderNationId) {
            sender.error("Vous ne pouvez pas déclarer la guerre à votre propre nation.")
            return
        }

        if (nationBridge.areNationsAllied(attackerNationId, defenderNationId)) {
            sender.error("Vous ne pouvez pas déclarer la guerre à une nation alliée.")
            sender.info("Rompez d'abord l'alliance avant de déclarer la guerre.")
            return
        }

        if (nationBridge.areNationsAtWar(attackerNationId, defenderNationId)) {
            sender.error("Vos nations sont déjà en guerre.")
            return
        }

        // Déclarer la guerre
        val result = warService.declareWar(attackerNationId, defenderNationId, goal, reason)
        result.fold(
            onSuccess = { war ->
                val defenderName = nationBridge.getNationName(defenderNationId) ?: "Nation #$defenderNationId"
                sender.success("Guerre déclarée contre $defenderName!")
                sender.info("ID de guerre: #${war.id}")
                sender.info("Objectif: ${goal.displayName}")
                sender.info("La guerre commencera dans 24 heures.")

                // Notifier les membres de la nation ennemie
                nationBridge.getNationMembers(defenderNationId).forEach { memberId ->
                    memberId.player?.let { player ->
                        val attackerName = nationBridge.getNationName(attackerNationId) ?: "Nation #$attackerNationId"
                        player.warning("$attackerName vous a déclaré la guerre!")
                        player.info("Objectif ennemi: ${goal.displayName}")
                        player.info("Raison: $reason")
                    }
                }
            },
            onFailure = { error ->
                sender.error("Échec de la déclaration: ${error.message}")
            }
        )
    }

    private fun handleInfo(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.error("Usage: /war info <id>")
            return
        }

        val warId = args[1].toIntOrNull()
        if (warId == null) {
            sender.error("ID de guerre invalide")
            return
        }

        val war = warService.getWar(warId)
        if (war == null) {
            sender.error("Guerre introuvable: #$warId")
            return
        }

        val attackerName = nationBridge.getNationName(war.attackerId) ?: "Nation #${war.attackerId}"
        val defenderName = nationBridge.getNationName(war.defenderId) ?: "Nation #${war.defenderId}"

        sender.info("═══════ Guerre #${war.id} ═══════")
        sender.info("Statut: ${war.status.color}${war.status.displayName}")
        sender.info("Objectif: ${war.warGoal.displayName}")
        sender.info("Attaquant: $attackerName - Score: ${war.attackerScore}")
        sender.info("Défenseur: $defenderName - Score: ${war.defenderScore}")
        sender.info("Fatigue (Att): ${war.attackerWarWeariness}%")
        sender.info("Fatigue (Déf): ${war.defenderWarWeariness}%")
        sender.info("Raison: ${war.reason}")
        if (war.status == WarStatus.NEGOTIATING && war.peaceTerms != null) {
            sender.warning("Termes de paix proposés: ${war.peaceTerms}")
        }
    }

    private fun handleList(sender: CommandSender, args: Array<out String>) {
        val activeWars = warService.getActiveWars()

        sender.info("═══════ Guerres Actives (${activeWars.size}) ═══════")

        if (activeWars.isEmpty()) {
            sender.info("Aucune guerre en cours.")
            return
        }

        activeWars.forEach { war ->
            val attackerName = nationBridge.getNationName(war.attackerId) ?: "[${war.attackerId}]"
            val defenderName = nationBridge.getNationName(war.defenderId) ?: "[${war.defenderId}]"
            val attackerTag = nationBridge.getNationTag(war.attackerId) ?: "???"
            val defenderTag = nationBridge.getNationTag(war.defenderId) ?: "???"

            sender.info("#${war.id}: [$attackerTag] $attackerName vs [$defenderTag] $defenderName")
            sender.info("    ${war.status.color}${war.status.displayName} | Score: ${war.attackerScore}-${war.defenderScore}")
        }
    }

    private fun handlePeace(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }

        if (args.size < 3) {
            sender.error("Usage: /war peace <war_id> <termes>")
            return
        }

        val warId = args[1].toIntOrNull()
        if (warId == null) {
            sender.error("ID de guerre invalide")
            return
        }

        val war = warService.getWar(warId)
        if (war == null) {
            sender.error("Guerre introuvable: #$warId")
            return
        }

        // Vérifier que le joueur appartient à une nation participante
        val playerNationId = nationBridge.getPlayerNationId(sender.uniqueId)
        if (playerNationId == null) {
            sender.error("Vous n'appartenez à aucune nation.")
            return
        }

        if (playerNationId != war.attackerId && playerNationId != war.defenderId) {
            sender.error("Votre nation ne participe pas à cette guerre.")
            return
        }

        if (!nationBridge.canDeclareWar(sender.uniqueId)) {
            sender.error("Vous n'avez pas les permissions pour négocier la paix.")
            return
        }

        val terms = args.drop(2).joinToString(" ")

        val success = warService.proposePeace(warId, playerNationId, terms)
        if (success) {
            sender.success("Proposition de paix envoyée!")
            sender.info("L'ennemi doit maintenant accepter ou refuser.")

            // Notifier l'autre camp
            val otherNationId = if (playerNationId == war.attackerId) war.defenderId else war.attackerId
            nationBridge.getNationMembers(otherNationId).forEach { memberId ->
                memberId.player?.let { player ->
                    val proposerName = nationBridge.getNationName(playerNationId) ?: "Nation #$playerNationId"
                    player.info("$proposerName propose la paix!")
                    player.info("Termes: $terms")
                    player.info("Utilisez /war accept $warId ou /war reject $warId")
                }
            }
        } else {
            sender.error("Impossible de proposer la paix pour cette guerre.")
        }
    }

    private fun handleAccept(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }

        if (args.size < 2) {
            sender.error("Usage: /war accept <war_id>")
            return
        }

        val warId = args[1].toIntOrNull()
        if (warId == null) {
            sender.error("ID de guerre invalide")
            return
        }

        val war = warService.getWar(warId)
        if (war == null) {
            sender.error("Guerre introuvable: #$warId")
            return
        }

        // Vérifier les permissions
        val playerNationId = nationBridge.getPlayerNationId(sender.uniqueId)
        if (playerNationId == null || (playerNationId != war.attackerId && playerNationId != war.defenderId)) {
            sender.error("Votre nation ne participe pas à cette guerre.")
            return
        }

        if (!nationBridge.canDeclareWar(sender.uniqueId)) {
            sender.error("Vous n'avez pas les permissions pour accepter la paix.")
            return
        }

        if (war.status != WarStatus.NEGOTIATING) {
            sender.error("Aucune proposition de paix n'est en cours.")
            return
        }

        val success = warService.acceptPeace(warId)
        if (success) {
            sender.success("Paix acceptée! La guerre est terminée.")

            // Notifier toutes les nations participantes
            listOf(war.attackerId, war.defenderId).forEach { nationId ->
                nationBridge.getNationMembers(nationId).forEach { memberId ->
                    memberId.player?.let { player ->
                        if (player != sender) {
                            player.success("La paix a été signée! La guerre #$warId est terminée.")
                        }
                    }
                }
            }
        } else {
            sender.error("Impossible d'accepter la paix pour cette guerre.")
        }
    }

    private fun handleReject(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }

        if (args.size < 2) {
            sender.error("Usage: /war reject <war_id>")
            return
        }

        val warId = args[1].toIntOrNull()
        if (warId == null) {
            sender.error("ID de guerre invalide")
            return
        }

        val war = warService.getWar(warId)
        if (war == null) {
            sender.error("Guerre introuvable: #$warId")
            return
        }

        // Vérifier les permissions
        val playerNationId = nationBridge.getPlayerNationId(sender.uniqueId)
        if (playerNationId == null || (playerNationId != war.attackerId && playerNationId != war.defenderId)) {
            sender.error("Votre nation ne participe pas à cette guerre.")
            return
        }

        if (!nationBridge.canDeclareWar(sender.uniqueId)) {
            sender.error("Vous n'avez pas les permissions pour refuser la paix.")
            return
        }

        if (war.status != WarStatus.NEGOTIATING) {
            sender.error("Aucune proposition de paix n'est en cours.")
            return
        }

        val success = warService.rejectPeace(warId)
        if (success) {
            sender.warning("Proposition de paix refusée. La guerre continue!")

            // Notifier l'autre camp
            val otherNationId = if (playerNationId == war.attackerId) war.defenderId else war.attackerId
            nationBridge.getNationMembers(otherNationId).forEach { memberId ->
                memberId.player?.let { player ->
                    val rejecterName = nationBridge.getNationName(playerNationId) ?: "Nation #$playerNationId"
                    player.warning("$rejecterName a refusé votre proposition de paix!")
                }
            }
        } else {
            sender.error("Impossible de refuser la paix.")
        }
    }

    private fun handleSurrender(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }

        if (args.size < 2) {
            sender.error("Usage: /war surrender <war_id>")
            sender.warning("ATTENTION: La capitulation est définitive!")
            return
        }

        val warId = args[1].toIntOrNull()
        if (warId == null) {
            sender.error("ID de guerre invalide")
            return
        }

        val war = warService.getWar(warId)
        if (war == null) {
            sender.error("Guerre introuvable: #$warId")
            return
        }

        // Vérifier les permissions
        val playerNationId = nationBridge.getPlayerNationId(sender.uniqueId)
        if (playerNationId == null || (playerNationId != war.attackerId && playerNationId != war.defenderId)) {
            sender.error("Votre nation ne participe pas à cette guerre.")
            return
        }

        if (!nationBridge.isNationLeader(sender.uniqueId)) {
            sender.error("Seul le leader de la nation peut capituler.")
            return
        }

        // Demander confirmation
        if (args.size < 3 || !args[2].equals("confirm", ignoreCase = true)) {
            sender.warning("═══════ CAPITULATION ═══════")
            sender.warning("Cette action est IRRÉVERSIBLE!")
            sender.warning("Votre nation subira les conséquences de la défaite.")
            sender.info("Pour confirmer: /war surrender $warId confirm")
            return
        }

        val success = warService.surrender(warId, playerNationId)
        if (success) {
            val nationName = nationBridge.getNationName(playerNationId) ?: "Nation #$playerNationId"
            sender.error("$nationName a capitulé...")

            // Notifier tout le monde
            listOf(war.attackerId, war.defenderId).forEach { nationId ->
                nationBridge.getNationMembers(nationId).forEach { memberId ->
                    memberId.player?.let { player ->
                        if (player != sender) {
                            if (nationId == playerNationId) {
                                player.error("Votre nation a capitulé dans la guerre #$warId...")
                            } else {
                                player.success("$nationName a capitulé! Victoire!")
                            }
                        }
                    }
                }
            }
        } else {
            sender.error("Impossible de capituler pour cette guerre.")
        }
    }

    private fun handleJoin(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }

        if (args.size < 3) {
            sender.error("Usage: /war join <war_id> <attacker|defender>")
            return
        }

        val warId = args[1].toIntOrNull()
        if (warId == null) {
            sender.error("ID de guerre invalide")
            return
        }

        val war = warService.getWar(warId)
        if (war == null) {
            sender.error("Guerre introuvable: #$warId")
            return
        }

        val side = when (args[2].lowercase()) {
            "attacker", "attaquant" -> WarSide.ATTACKER
            "defender", "défenseur", "defenseur" -> WarSide.DEFENDER
            else -> {
                sender.error("Côté invalide. Utilisez 'attacker' ou 'defender'.")
                return
            }
        }

        // Vérifier que le joueur a une nation
        val playerNationId = nationBridge.getPlayerNationId(sender.uniqueId)
        if (playerNationId == null) {
            sender.error("Vous devez appartenir à une nation pour rejoindre une guerre.")
            return
        }

        // Vérifier qu'il n'est pas déjà dans la guerre
        if (playerNationId == war.attackerId || playerNationId == war.defenderId) {
            sender.error("Votre nation participe déjà à cette guerre.")
            return
        }

        // Vérifier les permissions
        if (!nationBridge.canDeclareWar(sender.uniqueId)) {
            sender.error("Vous n'avez pas les permissions pour engager votre nation dans une guerre.")
            return
        }

        // Vérifier les alliances
        val targetNationId = if (side == WarSide.ATTACKER) war.attackerId else war.defenderId
        if (!nationBridge.areNationsAllied(playerNationId, targetNationId)) {
            sender.error("Vous ne pouvez rejoindre que les guerres de vos alliés.")
            return
        }

        val success = warService.joinWar(warId, playerNationId, side)
        if (success) {
            val sideName = if (side == WarSide.ATTACKER) "attaquant" else "défenseur"
            sender.success("Votre nation a rejoint la guerre comme $sideName!")

            // Notifier les participants
            listOf(war.attackerId, war.defenderId).forEach { nationId ->
                nationBridge.getNationMembers(nationId).forEach { memberId ->
                    memberId.player?.let { player ->
                        val joinerName = nationBridge.getNationName(playerNationId) ?: "Nation #$playerNationId"
                        player.info("$joinerName a rejoint la guerre comme $sideName!")
                    }
                }
            }
        } else {
            sender.error("Impossible de rejoindre cette guerre.")
        }
    }

    private fun handleHistory(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.error("Usage: /war history <war_id>")
            return
        }

        val warId = args[1].toIntOrNull()
        if (warId == null) {
            sender.error("ID de guerre invalide")
            return
        }

        val events = warService.getWarHistory(warId)
        if (events.isEmpty()) {
            sender.info("Aucun événement pour cette guerre.")
            return
        }

        sender.info("═══════ Historique Guerre #$warId ═══════")
        events.takeLast(10).forEach { event ->
            sender.info("${event.type.icon} ${event.type.displayName}: ${event.description}")
        }
        if (events.size > 10) {
            sender.info("... et ${events.size - 10} autres événements")
        }
    }

    private fun handleScore(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.error("Usage: /war score <war_id>")
            return
        }

        val warId = args[1].toIntOrNull()
        if (warId == null) {
            sender.error("ID de guerre invalide")
            return
        }

        val war = warService.getWar(warId)
        if (war == null) {
            sender.error("Guerre introuvable: #$warId")
            return
        }

        val attackerName = nationBridge.getNationName(war.attackerId) ?: "Attaquant"
        val defenderName = nationBridge.getNationName(war.defenderId) ?: "Défenseur"

        sender.info("═══════ Score Guerre #$warId ═══════")
        sender.info("$attackerName: ${war.attackerScore} points")
        sender.info("$defenderName: ${war.defenderScore} points")
        sender.info("Différence: ${war.totalScore} (${if (war.isAttackerWinning) attackerName else defenderName} en tête)")
        sender.info("Seuil de victoire: ${war.warGoal.requiredScore} points")
    }

    private fun handleStatus(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }

        val playerNationId = nationBridge.getPlayerNationId(sender.uniqueId)
        if (playerNationId == null) {
            sender.info("═══════ Statut de Guerre ═══════")
            sender.info("Vous n'appartenez à aucune nation.")
            return
        }

        val nationName = nationBridge.getNationName(playerNationId) ?: "Votre nation"
        val wars = warService.getNationWars(playerNationId)

        sender.info("═══════ Guerres de $nationName ═══════")

        if (wars.isEmpty()) {
            sender.info("Aucune guerre en cours.")
            return
        }

        wars.forEach { war ->
            val isAttacker = war.attackerId == playerNationId
            val enemyId = if (isAttacker) war.defenderId else war.attackerId
            val enemyName = nationBridge.getNationName(enemyId) ?: "Nation #$enemyId"
            val role = if (isAttacker) "Attaquant" else "Défenseur"

            sender.info("#${war.id}: vs $enemyName ($role)")
            sender.info("    ${war.status.color}${war.status.displayName} | Score: ${war.attackerScore}-${war.defenderScore}")
        }
    }

    private fun handleMenu(sender: CommandSender) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }
        menuManager.openMainMenu(sender)
    }

    private fun sendHelp(sender: CommandSender) {
        sender.info("═══════ Commandes de Guerre ═══════")
        sender.info("/war - Ouvre le menu GUI interactif")
        sender.info("/war menu - Ouvre le menu GUI")
        sender.info("/war declare <nation> <objectif> [raison] - Déclarer la guerre")
        sender.info("/war info <id> - Informations sur une guerre")
        sender.info("/war list - Liste des guerres actives")
        sender.info("/war peace <id> <termes> - Proposer la paix")
        sender.info("/war accept <id> - Accepter une proposition de paix")
        sender.info("/war reject <id> - Refuser une proposition de paix")
        sender.info("/war surrender <id> - Capituler")
        sender.info("/war join <id> <côté> - Rejoindre une guerre comme allié")
        sender.info("/war history <id> - Historique d'une guerre")
        sender.info("/war score <id> - Score d'une guerre")
        sender.info("/war status - Statut de vos guerres")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf(
                "menu", "declare", "info", "list", "peace", "accept", "reject",
                "surrender", "join", "history", "score", "status", "help"
            ).filter { it.startsWith(args[0], ignoreCase = true) }

            2 -> when (args[0].lowercase()) {
                "declare" -> {
                    // Suggérer les noms/tags des nations
                    if (nationBridge.isAvailable()) {
                        nationBridge.getAllNationIds()
                            .mapNotNull { nationBridge.getNationName(it) }
                            .filter { it.startsWith(args[1], ignoreCase = true) }
                    } else emptyList()
                }
                "info", "peace", "accept", "reject", "surrender", "join", "history", "score" -> {
                    // Suggérer les IDs des guerres actives
                    warService.getActiveWars().map { it.id.toString() }
                        .filter { it.startsWith(args[1]) }
                }
                else -> emptyList()
            }

            3 -> when (args[0].lowercase()) {
                "declare" -> WarGoal.entries.map { it.name.lowercase() }
                    .filter { it.startsWith(args[2], ignoreCase = true) }
                "join" -> listOf("attacker", "defender")
                    .filter { it.startsWith(args[2], ignoreCase = true) }
                else -> emptyList()
            }

            else -> emptyList()
        }
    }
}
