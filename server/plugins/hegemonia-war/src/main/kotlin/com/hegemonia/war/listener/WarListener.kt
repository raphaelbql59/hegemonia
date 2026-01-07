package com.hegemonia.war.listener

import com.hegemonia.war.HegemoniaWar
import com.hegemonia.war.model.WarStatus
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.time.Duration

/**
 * Listener pour les événements de guerre au niveau global
 */
class WarListener(private val plugin: HegemoniaWar) : Listener {

    private val warService = plugin.warService

    /**
     * Informe le joueur des guerres actives à la connexion
     */
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        // Programmer la notification après un court délai
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            notifyPlayerWarStatus(player)
        }, 40L) // 2 secondes après connexion
    }

    /**
     * Notifie un joueur de son statut de guerre
     */
    private fun notifyPlayerWarStatus(player: org.bukkit.entity.Player) {
        // TODO: Récupérer la nation du joueur via hegemonia-nations
        // val nationId = nationService.getPlayerNation(player.uniqueId)?.id ?: return

        // Placeholder: notifier de toutes les guerres actives (à remplacer par les guerres du joueur)
        /*
        val activeWars = warService.getActiveWars(nationId)

        if (activeWars.isNotEmpty()) {
            player.showTitle(Title.title(
                Component.text("⚔ GUERRE ⚔", NamedTextColor.RED),
                Component.text("Votre nation est en guerre!", NamedTextColor.YELLOW),
                Title.Times.times(
                    Duration.ofMillis(500),
                    Duration.ofSeconds(3),
                    Duration.ofMillis(500)
                )
            ))

            player.sendMessage(Component.text()
                .append(Component.text("════════════════════════════", NamedTextColor.DARK_RED))
                .build()
            )

            activeWars.forEach { war ->
                val statusColor = when (war.status) {
                    WarStatus.DECLARED -> NamedTextColor.YELLOW
                    WarStatus.ACTIVE -> NamedTextColor.RED
                    WarStatus.NEGOTIATING -> NamedTextColor.AQUA
                    else -> NamedTextColor.GRAY
                }

                val isAttacker = war.attackerId == nationId
                val enemyId = if (isAttacker) war.defenderId else war.attackerId
                val role = if (isAttacker) "Attaquant" else "Défenseur"

                player.sendMessage(Component.text()
                    .append(Component.text("⚔ ", NamedTextColor.RED))
                    .append(Component.text("Guerre #${war.id}", NamedTextColor.GOLD))
                    .append(Component.text(" - ", NamedTextColor.GRAY))
                    .append(Component.text(war.status.displayName, statusColor))
                    .build()
                )

                player.sendMessage(Component.text()
                    .append(Component.text("   Rôle: ", NamedTextColor.GRAY))
                    .append(Component.text(role, if (isAttacker) NamedTextColor.RED else NamedTextColor.BLUE))
                    .append(Component.text(" | Objectif: ", NamedTextColor.GRAY))
                    .append(Component.text(war.warGoal.displayName, NamedTextColor.GOLD))
                    .build()
                )

                player.sendMessage(Component.text()
                    .append(Component.text("   Score: ", NamedTextColor.GRAY))
                    .append(Component.text("${war.attackerScore}", NamedTextColor.RED))
                    .append(Component.text(" vs ", NamedTextColor.GRAY))
                    .append(Component.text("${war.defenderScore}", NamedTextColor.BLUE))
                    .build()
                )
            }

            player.sendMessage(Component.text()
                .append(Component.text("════════════════════════════", NamedTextColor.DARK_RED))
                .build()
            )

            player.sendMessage(Component.text()
                .append(Component.text("Utilisez ", NamedTextColor.GRAY))
                .append(Component.text("/war status", NamedTextColor.YELLOW))
                .append(Component.text(" pour plus de détails.", NamedTextColor.GRAY))
                .build()
            )
        }
        */
    }

    /**
     * Annonce globale d'une déclaration de guerre
     */
    fun announceWarDeclaration(attackerName: String, defenderName: String, reason: String) {
        val message = Component.text()
            .append(Component.text("\n"))
            .append(Component.text("═══════════════════════════════════════", NamedTextColor.DARK_RED))
            .append(Component.text("\n"))
            .append(Component.text("          ⚔ DÉCLARATION DE GUERRE ⚔", NamedTextColor.RED))
            .append(Component.text("\n"))
            .append(Component.text("═══════════════════════════════════════", NamedTextColor.DARK_RED))
            .append(Component.text("\n\n"))
            .append(Component.text("  $attackerName", NamedTextColor.GOLD))
            .append(Component.text(" déclare la guerre à ", NamedTextColor.WHITE))
            .append(Component.text("$defenderName", NamedTextColor.GOLD))
            .append(Component.text("\n\n"))
            .append(Component.text("  Raison: ", NamedTextColor.GRAY))
            .append(Component.text(reason, NamedTextColor.YELLOW))
            .append(Component.text("\n\n"))
            .append(Component.text("═══════════════════════════════════════", NamedTextColor.DARK_RED))
            .append(Component.text("\n"))
            .build()

        plugin.server.broadcast(message)

        // Jouer un son à tous les joueurs
        plugin.server.onlinePlayers.forEach { player ->
            player.playSound(player.location, org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f)
        }
    }

    /**
     * Annonce globale de fin de guerre
     */
    fun announceWarEnd(winnerName: String?, war: com.hegemonia.war.model.War) {
        val resultText = when {
            winnerName != null -> Component.text("$winnerName est victorieux!", NamedTextColor.GOLD)
            else -> Component.text("La guerre se termine sans vainqueur.", NamedTextColor.GRAY)
        }

        val message = Component.text()
            .append(Component.text("\n"))
            .append(Component.text("═══════════════════════════════════════", NamedTextColor.GREEN))
            .append(Component.text("\n"))
            .append(Component.text("            🏳 FIN DE GUERRE 🏳", NamedTextColor.WHITE))
            .append(Component.text("\n"))
            .append(Component.text("═══════════════════════════════════════", NamedTextColor.GREEN))
            .append(Component.text("\n\n"))
            .append(Component.text("  ", NamedTextColor.WHITE))
            .append(resultText)
            .append(Component.text("\n\n"))
            .append(Component.text("  Score final: ", NamedTextColor.GRAY))
            .append(Component.text("${war.attackerScore}", NamedTextColor.RED))
            .append(Component.text(" - ", NamedTextColor.WHITE))
            .append(Component.text("${war.defenderScore}", NamedTextColor.BLUE))
            .append(Component.text("\n\n"))
            .append(Component.text("═══════════════════════════════════════", NamedTextColor.GREEN))
            .append(Component.text("\n"))
            .build()

        plugin.server.broadcast(message)

        // Son de victoire/paix
        plugin.server.onlinePlayers.forEach { player ->
            player.playSound(player.location, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f)
        }
    }

    /**
     * Annonce une bataille commençant
     */
    fun announceBattleStart(battleId: Int, battleType: String, regionName: String) {
        val message = Component.text()
            .append(Component.text("[GUERRE] ", NamedTextColor.DARK_RED))
            .append(Component.text("$battleType", NamedTextColor.GOLD))
            .append(Component.text(" en cours à ", NamedTextColor.WHITE))
            .append(Component.text(regionName, NamedTextColor.YELLOW))
            .append(Component.text(" (ID: $battleId)", NamedTextColor.GRAY))
            .build()

        plugin.server.broadcast(message)
    }

    /**
     * Annonce le résultat d'une bataille
     */
    fun announceBattleEnd(battleId: Int, winnerName: String?, attackerKills: Int, defenderKills: Int) {
        val resultText = winnerName?.let {
            Component.text("$it remporte la victoire!", NamedTextColor.GOLD)
        } ?: Component.text("La bataille se termine par une égalité.", NamedTextColor.YELLOW)

        val message = Component.text()
            .append(Component.text("[GUERRE] ", NamedTextColor.DARK_RED))
            .append(Component.text("Bataille #$battleId terminée - ", NamedTextColor.WHITE))
            .append(resultText)
            .append(Component.text(" (", NamedTextColor.GRAY))
            .append(Component.text("$attackerKills", NamedTextColor.RED))
            .append(Component.text("-", NamedTextColor.GRAY))
            .append(Component.text("$defenderKills", NamedTextColor.BLUE))
            .append(Component.text(")", NamedTextColor.GRAY))
            .build()

        plugin.server.broadcast(message)
    }
}
