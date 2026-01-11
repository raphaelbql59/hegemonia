package com.hegemonia.war.listener

import com.hegemonia.core.utils.*
import com.hegemonia.war.HegemoniaWar
import com.hegemonia.war.model.BattleStatus
import com.hegemonia.war.model.WarSide
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import java.time.Duration

/**
 * Listener pour les événements de bataille
 */
class BattleListener(private val plugin: HegemoniaWar) : Listener {

    private val battleService = plugin.battleService

    /**
     * Gère les dégâts entre joueurs en bataille
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? Player ?: return
        val attacker = event.damager as? Player ?: return

        // Vérifier si les deux joueurs sont dans la même bataille
        val attackerBattle = battleService.isInBattle(attacker.uniqueId)
        val victimBattle = battleService.isInBattle(victim.uniqueId)

        if (attackerBattle == null && victimBattle == null) {
            // Ni l'un ni l'autre n'est en bataille
            return
        }

        if (attackerBattle != victimBattle) {
            // Pas dans la même bataille - annuler les dégâts
            event.isCancelled = true
            attacker.warning("Ce joueur n'est pas dans votre bataille!")
            return
        }

        val battleId = attackerBattle!!
        val battle = battleService.getBattle(battleId) ?: return

        if (battle.status != BattleStatus.IN_PROGRESS) {
            event.isCancelled = true
            attacker.warning("La bataille n'est pas en cours!")
            return
        }

        // Vérifier si les joueurs sont du même côté (friendly fire)
        val attackerSide = battleService.getPlayerSide(battleId, attacker.uniqueId)
        val victimSide = battleService.getPlayerSide(battleId, victim.uniqueId)

        if (attackerSide == victimSide) {
            // Friendly fire désactivé par défaut
            if (!plugin.config.getBoolean("battle.friendly-fire", false)) {
                event.isCancelled = true
                attacker.warning("Tir ami désactivé!")
                return
            }
        }

        // Enregistrer les dégâts dans les stats
        battleService.recordDamage(battleId, attacker.uniqueId, victim.uniqueId, event.finalDamage)
    }

    /**
     * Gère la mort d'un joueur en bataille
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val victim = event.player
        val battleId = battleService.isInBattle(victim.uniqueId) ?: return

        val battle = battleService.getBattle(battleId) ?: return
        if (battle.status != BattleStatus.IN_PROGRESS) return

        // Trouver le tueur
        val killer = victim.killer
        if (killer != null && battleService.isInBattle(killer.uniqueId) == battleId) {
            // Enregistrer le kill
            battleService.registerKill(battleId, killer.uniqueId, victim.uniqueId)

            // Notification du kill
            killer.info("Vous avez éliminé ${victim.name}!")

            // Annonce à tous les participants
            broadcastToBattle(battleId, Component.text()
                .append(Component.text("☠ ", NamedTextColor.RED))
                .append(Component.text(killer.name, NamedTextColor.GOLD))
                .append(Component.text(" a éliminé ", NamedTextColor.GRAY))
                .append(Component.text(victim.name, NamedTextColor.RED))
                .build()
            )

            // Vérifier si la bataille peut se terminer
            checkBattleEnd(battleId)
        }

        // Personnaliser le message de mort
        event.deathMessage(Component.text()
            .append(Component.text("[Bataille] ", NamedTextColor.DARK_RED))
            .append(Component.text(victim.name, NamedTextColor.RED))
            .append(Component.text(" est tombé au combat", NamedTextColor.GRAY))
            .build()
        )
    }

    /**
     * Gère le respawn d'un joueur en bataille
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        val battleId = battleService.isInBattle(player.uniqueId) ?: return

        val battle = battleService.getBattle(battleId) ?: return
        if (battle.status != BattleStatus.IN_PROGRESS) return

        // Récupérer le côté du joueur pour déterminer le point de respawn
        val side = battleService.getPlayerSide(battleId, player.uniqueId) ?: return
        val respawnLocation = battleService.getRespawnLocation(battleId, side)

        // Téléporter au point de respawn de l'équipe
        if (respawnLocation != null) {
            event.respawnLocation = respawnLocation
        }

        // Afficher le titre de respawn
        player.showTitle(Title.title(
            Component.text("RESPAWN", NamedTextColor.RED),
            Component.text("Retournez au combat!", NamedTextColor.YELLOW),
            Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofSeconds(2),
                Duration.ofMillis(500)
            )
        ))

        // Marquer le joueur comme vivant après un délai
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            battleService.respawnPlayer(battleId, player.uniqueId)
        }, 20L * 5) // 5 secondes de délai
    }

    /**
     * Gère le mouvement des joueurs pour la zone de bataille
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val to = event.to ?: return

        // Optimisation: ne vérifier que si le joueur a changé de block
        if (event.from.blockX == to.blockX && event.from.blockZ == to.blockZ) {
            return
        }

        val battleId = battleService.isInBattle(player.uniqueId) ?: return
        val battle = battleService.getBattle(battleId) ?: return

        val wasInZone = battle.isInBattleZone(event.from)
        val isInZone = battle.isInBattleZone(to)

        if (wasInZone && !isInZone) {
            // Le joueur sort de la zone
            player.warning("⚠ Vous quittez la zone de combat!")
            player.warning("Vous avez 30 secondes pour revenir.")

            // Démarrer un timer pour forcer le retour ou désertion
            startDesertionTimer(player, battleId)
        } else if (!wasInZone && isInZone) {
            // Le joueur entre dans la zone - annuler le timer de désertion
            cancelDesertionTimer(player)
            player.info("Vous êtes de retour dans la zone de combat.")
        }
    }

    // Map des timers de désertion par joueur
    private val desertionTimers = mutableMapOf<java.util.UUID, Int>()

    private fun startDesertionTimer(player: Player, battleId: Int) {
        // Annuler un timer existant
        cancelDesertionTimer(player)

        // Créer un nouveau timer de 30 secondes
        val taskId = plugin.server.scheduler.runTaskLater(plugin, Runnable {
            // Vérifier si le joueur est toujours hors zone
            val battle = battleService.getBattle(battleId) ?: return@Runnable
            if (!battle.isInBattleZone(player.location)) {
                // Désertion
                battleService.leaveBattle(battleId, player.uniqueId)
                player.error("Vous avez déserté la bataille!")

                broadcastToBattle(battleId, Component.text()
                    .append(Component.text("⚠ DÉSERTION! ", NamedTextColor.RED))
                    .append(Component.text(player.name, NamedTextColor.GRAY))
                    .append(Component.text(" a abandonné le combat", NamedTextColor.YELLOW))
                    .build()
                )

                // Vérifier si la bataille doit se terminer
                checkBattleEnd(battleId)
            }
            desertionTimers.remove(player.uniqueId)
        }, 20L * 30).taskId // 30 secondes

        desertionTimers[player.uniqueId] = taskId
    }

    private fun cancelDesertionTimer(player: Player) {
        desertionTimers.remove(player.uniqueId)?.let { taskId: Int ->
            plugin.server.scheduler.cancelTask(taskId)
        }
    }

    /**
     * Gère la déconnexion d'un joueur en bataille
     */
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val battleId = battleService.isInBattle(player.uniqueId) ?: return

        val battle = battleService.getBattle(battleId) ?: return

        // Marquer le joueur comme ayant quitté
        battleService.leaveBattle(battleId, player.uniqueId)

        // Annoncer la désertion
        if (battle.status == BattleStatus.IN_PROGRESS) {
            broadcastToBattle(battleId, Component.text()
                .append(Component.text("⚠ ", NamedTextColor.YELLOW))
                .append(Component.text(player.name, NamedTextColor.GRAY))
                .append(Component.text(" a quitté la bataille (déconnexion)", NamedTextColor.YELLOW))
                .build()
            )

            // Vérifier si la bataille doit se terminer
            checkBattleEnd(battleId)
        }
    }

    /**
     * Vérifie si une bataille doit se terminer
     */
    private fun checkBattleEnd(battleId: Int) {
        val (attackers, defenders) = battleService.countActiveParticipants(battleId)

        val battle = battleService.getBattle(battleId) ?: return

        when {
            attackers == 0 && defenders == 0 -> {
                // Égalité - personne ne reste
                battleService.endBattle(battleId, null)
                broadcastToBattle(battleId, Component.text(
                    "La bataille se termine par forfait mutuel!",
                    NamedTextColor.YELLOW
                ))
            }
            attackers == 0 -> {
                // Victoire défenseur
                battleService.endBattle(battleId, battle.defenderNationId)
                broadcastToBattle(battleId, Component.text(
                    "Les défenseurs remportent la bataille!",
                    NamedTextColor.GREEN
                ))
            }
            defenders == 0 -> {
                // Victoire attaquant
                battleService.endBattle(battleId, battle.attackerNationId)
                broadcastToBattle(battleId, Component.text(
                    "Les attaquants remportent la bataille!",
                    NamedTextColor.RED
                ))
            }
        }
    }

    /**
     * Envoie un message à tous les participants d'une bataille
     */
    private fun broadcastToBattle(battleId: Int, message: Component) {
        val participants = battleService.getParticipants(battleId)
        participants.forEach { participant ->
            plugin.server.getPlayer(participant.playerId)?.sendMessage(message)
        }
    }
}
