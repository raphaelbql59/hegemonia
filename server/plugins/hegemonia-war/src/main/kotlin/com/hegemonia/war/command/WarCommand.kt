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

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
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

        // TODO: Récupérer les IDs des nations via NationService
        sender.warning("Système en cours d'implémentation - connexion avec hegemonia-nations requise")

        /*
        val attackerNation = nationService.getPlayerNation(sender.uniqueId)
        if (attackerNation == null) {
            sender.error("Vous devez appartenir à une nation pour déclarer la guerre.")
            return
        }

        val defenderNation = nationService.getNationByName(targetNationName)
        if (defenderNation == null) {
            sender.error("Nation introuvable: $targetNationName")
            return
        }

        val result = warService.declareWar(attackerNation.id, defenderNation.id, goal, reason)
        result.fold(
            onSuccess = { war ->
                sender.success("Guerre déclarée contre ${defenderNation.name}!")
                sender.info("Objectif: ${goal.displayName}")
                sender.info("La guerre commencera dans 24 heures.")
            },
            onFailure = { error ->
                sender.error("Échec de la déclaration: ${error.message}")
            }
        )
        */
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

        sender.info("═══════ Guerre #${war.id} ═══════")
        sender.info("Statut: ${war.status.color}${war.status.displayName}")
        sender.info("Objectif: ${war.warGoal.displayName}")
        sender.info("Attaquant: Nation #${war.attackerId} - Score: ${war.attackerScore}")
        sender.info("Défenseur: Nation #${war.defenderId} - Score: ${war.defenderScore}")
        sender.info("Fatigue (Att): ${war.attackerWarWeariness}%")
        sender.info("Fatigue (Déf): ${war.defenderWarWeariness}%")
        sender.info("Raison: ${war.reason}")
        if (war.status == WarStatus.NEGOTIATING && war.peaceTerms != null) {
            sender.warning("Termes de paix proposés: ${war.peaceTerms}")
        }
    }

    private fun handleList(sender: CommandSender, args: Array<out String>) {
        // TODO: Lister les guerres actives
        sender.info("═══════ Guerres Actives ═══════")
        sender.info("Fonctionnalité en cours d'implémentation")
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

        val terms = args.drop(2).joinToString(" ")

        // TODO: Vérifier que le joueur appartient à une nation participante
        val success = warService.proposePeace(warId, 0, terms) // 0 = placeholder
        if (success) {
            sender.success("Proposition de paix envoyée!")
            sender.info("L'ennemi doit maintenant accepter ou refuser.")
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

        // TODO: Vérifier que le joueur peut accepter
        val success = warService.acceptPeace(warId)
        if (success) {
            sender.success("Paix acceptée! La guerre est terminée.")
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

        // TODO: Implémenter le rejet de paix
        sender.warning("Le rejet de paix n'est pas encore implémenté.")
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

        // TODO: Vérifier les permissions et la nation
        sender.warning("Confirmation requise: /war surrender $warId confirm")

        if (args.size > 2 && args[2].equals("confirm", ignoreCase = true)) {
            val success = warService.surrender(warId, 0) // 0 = placeholder
            if (success) {
                sender.error("Votre nation a capitulé...")
            } else {
                sender.error("Impossible de capituler pour cette guerre.")
            }
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

        val side = when (args[2].lowercase()) {
            "attacker", "attaquant" -> WarSide.ATTACKER
            "defender", "défenseur", "defenseur" -> WarSide.DEFENDER
            else -> {
                sender.error("Côté invalide. Utilisez 'attacker' ou 'defender'.")
                return
            }
        }

        // TODO: Récupérer la nation du joueur
        val success = warService.joinWar(warId, 0, side) // 0 = placeholder
        if (success) {
            sender.success("Votre nation a rejoint la guerre comme ${if (side == WarSide.ATTACKER) "attaquant" else "défenseur"}!")
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

        sender.info("═══════ Score Guerre #$warId ═══════")
        sender.info("Attaquant: ${war.attackerScore} points")
        sender.info("Défenseur: ${war.defenderScore} points")
        sender.info("Différence: ${war.totalScore} (${if (war.isAttackerWinning) "Attaquant" else "Défenseur"} en tête)")
        sender.info("Seuil de victoire: ${war.warGoal.requiredScore} points")
    }

    private fun handleStatus(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }

        // TODO: Afficher les guerres de la nation du joueur
        sender.info("═══════ Statut de Guerre ═══════")
        sender.info("Connectez-vous à une nation pour voir vos guerres.")
    }

    private fun sendHelp(sender: CommandSender) {
        sender.info("═══════ Commandes de Guerre ═══════")
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
                "declare", "info", "list", "peace", "accept", "reject",
                "surrender", "join", "history", "score", "status", "help"
            ).filter { it.startsWith(args[0], ignoreCase = true) }

            2 -> when (args[0].lowercase()) {
                "declare" -> emptyList() // TODO: Liste des nations
                "join" -> emptyList() // TODO: Liste des IDs de guerre
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
