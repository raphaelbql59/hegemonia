package com.hegemonia.war.command

import com.hegemonia.core.utils.*
import com.hegemonia.war.HegemoniaWar
import com.hegemonia.war.model.BattleStatus
import com.hegemonia.war.model.BattleType
import com.hegemonia.war.model.WarSide
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * Commande de gestion des batailles
 */
class BattleCommand(private val plugin: HegemoniaWar) : CommandExecutor, TabCompleter {

    private val battleService = plugin.battleService
    private val warService = plugin.warService

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "create", "créer" -> handleCreate(sender, args)
            "join", "rejoindre" -> handleJoin(sender, args)
            "leave", "quitter" -> handleLeave(sender, args)
            "info" -> handleInfo(sender, args)
            "list", "liste" -> handleList(sender, args)
            "start", "démarrer" -> handleStart(sender, args)
            "end", "terminer" -> handleEnd(sender, args)
            "score" -> handleScore(sender, args)
            "participants" -> handleParticipants(sender, args)
            "zone" -> handleZone(sender, args)
            "respawn" -> handleRespawn(sender, args)
            "help", "aide" -> sendHelp(sender)
            else -> {
                sender.error("Sous-commande inconnue: ${args[0]}")
                sendHelp(sender)
            }
        }
        return true
    }

    private fun handleCreate(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }

        if (!sender.hasPermission("hegemonia.war.battle.create")) {
            sender.error("Vous n'avez pas la permission de créer des batailles.")
            return
        }

        if (args.size < 4) {
            sender.error("Usage: /battle create <war_id> <type> <region> [rayon]")
            sender.info("Types: ${BattleType.entries.joinToString(", ") { it.name.lowercase() }}")
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

        val typeName = args[2]
        val type = BattleType.entries.find { it.name.equals(typeName, ignoreCase = true) }
        if (type == null) {
            sender.error("Type de bataille invalide: $typeName")
            sender.info("Types valides: ${BattleType.entries.joinToString(", ") { it.name.lowercase() }}")
            return
        }

        val regionId = args[3]
        val radius = if (args.size > 4) args[4].toIntOrNull() ?: 100 else 100

        val result = battleService.createBattle(
            warId = warId,
            type = type,
            regionId = regionId,
            attackerNationId = war.attackerId,
            defenderNationId = war.defenderId,
            center = sender.location,
            radius = radius
        )

        result.fold(
            onSuccess = { battle ->
                sender.success("Bataille #${battle.id} créée!")
                sender.info("Type: ${type.displayName}")
                sender.info("Zone: ${radius}m autour de votre position")
                sender.info("Participants max: ${type.maxParticipants}")
            },
            onFailure = { error ->
                sender.error("Échec de création: ${error.message}")
            }
        )
    }

    private fun handleJoin(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }

        // Vérifier si le joueur est déjà dans une bataille
        val currentBattle = battleService.isInBattle(sender.uniqueId)
        if (currentBattle != null) {
            sender.error("Vous êtes déjà dans la bataille #$currentBattle")
            sender.info("Utilisez /battle leave pour quitter.")
            return
        }

        if (args.size < 2) {
            // Rejoindre la bataille à la position actuelle
            val battle = battleService.getBattleAt(sender.location)
            if (battle == null) {
                sender.error("Aucune bataille à cette position.")
                sender.info("Usage: /battle join <id> [attacker|defender]")
                return
            }

            // TODO: Déterminer automatiquement le côté basé sur la nation
            sender.warning("Spécifiez votre côté: /battle join ${battle.id} <attacker|defender>")
            return
        }

        val battleId = args[1].toIntOrNull()
        if (battleId == null) {
            sender.error("ID de bataille invalide")
            return
        }

        val side = if (args.size > 2) {
            when (args[2].lowercase()) {
                "attacker", "attaquant" -> WarSide.ATTACKER
                "defender", "défenseur", "defenseur" -> WarSide.DEFENDER
                else -> {
                    sender.error("Côté invalide. Utilisez 'attacker' ou 'defender'.")
                    return
                }
            }
        } else {
            // TODO: Déterminer automatiquement basé sur la nation
            sender.error("Spécifiez votre côté: /battle join $battleId <attacker|defender>")
            return
        }

        // TODO: Récupérer la nation du joueur
        val nationId = 0 // Placeholder

        val success = battleService.joinBattle(battleId, sender.uniqueId, nationId, side)
        if (success) {
            sender.success("Vous avez rejoint la bataille #$battleId!")
            sender.info("Côté: ${if (side == WarSide.ATTACKER) "Attaquant" else "Défenseur"}")
            sender.warning("Restez dans la zone de combat!")
        } else {
            sender.error("Impossible de rejoindre cette bataille.")
        }
    }

    private fun handleLeave(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }

        val currentBattle = battleService.isInBattle(sender.uniqueId)
        if (currentBattle == null) {
            sender.error("Vous n'êtes dans aucune bataille.")
            return
        }

        val success = battleService.leaveBattle(currentBattle, sender.uniqueId)
        if (success) {
            sender.success("Vous avez quitté la bataille #$currentBattle")
            sender.warning("Attention: déserter peut avoir des conséquences!")
        } else {
            sender.error("Impossible de quitter la bataille.")
        }
    }

    private fun handleInfo(sender: CommandSender, args: Array<out String>) {
        val battleId = if (args.size > 1) {
            args[1].toIntOrNull()
        } else if (sender is Player) {
            battleService.isInBattle(sender.uniqueId)
        } else null

        if (battleId == null) {
            sender.error("Usage: /battle info <id>")
            return
        }

        val battle = battleService.getBattle(battleId)
        if (battle == null) {
            sender.error("Bataille introuvable: #$battleId")
            return
        }

        sender.info("═══════ Bataille #${battle.id} ═══════")
        sender.info("Type: ${battle.type.displayName}")
        sender.info("Statut: ${battle.status.color}${battle.status.displayName}")
        sender.info("Région: ${battle.regionId}")
        sender.info("────────────────────────")
        sender.info("Attaquant (Nation #${battle.attackerNationId})")
        sender.info("  Kills: ${battle.attackerKills} | Deaths: ${battle.attackerDeaths}")
        sender.info("  K/D: ${"%.2f".format(battle.attackerKD)}")
        sender.info("────────────────────────")
        sender.info("Défenseur (Nation #${battle.defenderNationId})")
        sender.info("  Kills: ${battle.defenderKills} | Deaths: ${battle.defenderDeaths}")
        sender.info("  K/D: ${"%.2f".format(battle.defenderKD)}")
        sender.info("────────────────────────")
        sender.info("Zone: ${battle.radius}m de rayon")

        val (attackers, defenders) = battleService.countActiveParticipants(battleId)
        sender.info("Participants actifs: $attackers vs $defenders")
    }

    private fun handleList(sender: CommandSender, args: Array<out String>) {
        val warId = if (args.size > 1) args[1].toIntOrNull() else null

        if (warId != null) {
            val battles = battleService.getActiveBattles(warId)
            if (battles.isEmpty()) {
                sender.info("Aucune bataille active pour la guerre #$warId")
                return
            }

            sender.info("═══════ Batailles Guerre #$warId ═══════")
            battles.forEach { battle ->
                sender.info("#${battle.id} - ${battle.type.displayName} (${battle.status.displayName})")
            }
        } else {
            // TODO: Lister toutes les batailles actives
            sender.info("═══════ Batailles Actives ═══════")
            sender.info("Usage: /battle list <war_id>")
        }
    }

    private fun handleStart(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("hegemonia.war.battle.manage")) {
            sender.error("Vous n'avez pas la permission de gérer les batailles.")
            return
        }

        if (args.size < 2) {
            sender.error("Usage: /battle start <id>")
            return
        }

        val battleId = args[1].toIntOrNull()
        if (battleId == null) {
            sender.error("ID de bataille invalide")
            return
        }

        val success = battleService.startBattle(battleId)
        if (success) {
            sender.success("Bataille #$battleId démarrée!")
        } else {
            sender.error("Impossible de démarrer cette bataille.")
        }
    }

    private fun handleEnd(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("hegemonia.war.battle.manage")) {
            sender.error("Vous n'avez pas la permission de gérer les batailles.")
            return
        }

        if (args.size < 2) {
            sender.error("Usage: /battle end <id> [winner_nation_id]")
            return
        }

        val battleId = args[1].toIntOrNull()
        if (battleId == null) {
            sender.error("ID de bataille invalide")
            return
        }

        val winnerId = if (args.size > 2) args[2].toIntOrNull() else null

        val success = battleService.endBattle(battleId, winnerId)
        if (success) {
            sender.success("Bataille #$battleId terminée!")
        } else {
            sender.error("Impossible de terminer cette bataille.")
        }
    }

    private fun handleScore(sender: CommandSender, args: Array<out String>) {
        val battleId = if (args.size > 1) {
            args[1].toIntOrNull()
        } else if (sender is Player) {
            battleService.isInBattle(sender.uniqueId)
        } else null

        if (battleId == null) {
            sender.error("Usage: /battle score <id>")
            return
        }

        val battle = battleService.getBattle(battleId)
        if (battle == null) {
            sender.error("Bataille introuvable: #$battleId")
            return
        }

        sender.info("═══════ Score Bataille #$battleId ═══════")
        sender.info("Attaquant: ${battle.attackerKills} kills")
        sender.info("Défenseur: ${battle.defenderKills} kills")

        val leader = when {
            battle.attackerKills > battle.defenderKills -> "Attaquant en tête"
            battle.defenderKills > battle.attackerKills -> "Défenseur en tête"
            else -> "Égalité"
        }
        sender.info("Statut: $leader")
    }

    private fun handleParticipants(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.error("Usage: /battle participants <id>")
            return
        }

        val battleId = args[1].toIntOrNull()
        if (battleId == null) {
            sender.error("ID de bataille invalide")
            return
        }

        val participants = battleService.getParticipants(battleId)
        if (participants.isEmpty()) {
            sender.info("Aucun participant dans cette bataille.")
            return
        }

        val attackers = participants.filter { it.side == WarSide.ATTACKER }
        val defenders = participants.filter { it.side == WarSide.DEFENDER }

        sender.info("═══════ Participants Bataille #$battleId ═══════")
        sender.info("─── Attaquants (${attackers.size}) ───")
        attackers.forEach { p ->
            val status = if (p.isAlive) "<green>●" else "<red>✗"
            sender.info("$status ${p.playerId.toString().take(8)}... - K:${p.kills} D:${p.deaths}")
        }

        sender.info("─── Défenseurs (${defenders.size}) ───")
        defenders.forEach { p ->
            val status = if (p.isAlive) "<green>●" else "<red>✗"
            sender.info("$status ${p.playerId.toString().take(8)}... - K:${p.kills} D:${p.deaths}")
        }
    }

    private fun handleZone(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }

        val battle = battleService.getBattleAt(sender.location)
        if (battle == null) {
            sender.info("Vous n'êtes pas dans une zone de bataille.")
        } else {
            sender.warning("Vous êtes dans la zone de bataille #${battle.id}")
            sender.info("Type: ${battle.type.displayName}")
            sender.info("Statut: ${battle.status.displayName}")
        }
    }

    private fun handleRespawn(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.error("Cette commande doit être exécutée par un joueur.")
            return
        }

        val currentBattle = battleService.isInBattle(sender.uniqueId)
        if (currentBattle == null) {
            sender.error("Vous n'êtes dans aucune bataille.")
            return
        }

        val battle = battleService.getBattle(currentBattle)
        if (battle == null || battle.status != BattleStatus.IN_PROGRESS) {
            sender.error("La bataille n'est pas en cours.")
            return
        }

        val success = battleService.respawnPlayer(currentBattle, sender.uniqueId)
        if (success) {
            sender.success("Vous êtes de retour dans la bataille!")
            // TODO: Téléporter le joueur au point de respawn
        } else {
            sender.error("Impossible de respawn pour le moment.")
        }
    }

    private fun sendHelp(sender: CommandSender) {
        sender.info("═══════ Commandes de Bataille ═══════")
        sender.info("/battle create <war_id> <type> <region> [rayon] - Créer une bataille")
        sender.info("/battle join [id] [côté] - Rejoindre une bataille")
        sender.info("/battle leave - Quitter la bataille actuelle")
        sender.info("/battle info [id] - Informations sur une bataille")
        sender.info("/battle list [war_id] - Liste des batailles")
        sender.info("/battle score [id] - Score actuel")
        sender.info("/battle participants <id> - Liste des participants")
        sender.info("/battle zone - Vérifier si vous êtes en zone de combat")
        sender.info("/battle respawn - Revenir après la mort")
        if (sender.hasPermission("hegemonia.war.battle.manage")) {
            sender.info("─── Admin ───")
            sender.info("/battle start <id> - Démarrer une bataille")
            sender.info("/battle end <id> [winner] - Terminer une bataille")
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf(
                "create", "join", "leave", "info", "list",
                "start", "end", "score", "participants", "zone", "respawn", "help"
            ).filter { it.startsWith(args[0], ignoreCase = true) }

            2 -> when (args[0].lowercase()) {
                "create" -> emptyList() // IDs de guerre
                else -> emptyList()
            }

            3 -> when (args[0].lowercase()) {
                "create" -> BattleType.entries.map { it.name.lowercase() }
                    .filter { it.startsWith(args[2], ignoreCase = true) }
                "join" -> listOf("attacker", "defender")
                    .filter { it.startsWith(args[2], ignoreCase = true) }
                else -> emptyList()
            }

            else -> emptyList()
        }
    }
}
