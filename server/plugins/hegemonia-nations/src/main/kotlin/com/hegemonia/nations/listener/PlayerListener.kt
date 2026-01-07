package com.hegemonia.nations.listener

import com.hegemonia.core.utils.sendMini
import com.hegemonia.nations.HegemoniaNations
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Listener pour les événements joueurs
 */
class PlayerListener(private val plugin: HegemoniaNations) : Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPreLogin(event: AsyncPlayerPreLoginEvent) {
        // Créer ou charger le joueur en async
        plugin.playerService.getOrCreatePlayer(event.uniqueId, event.name)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val hegemoniaPlayer = plugin.playerService.getOrCreatePlayer(player.uniqueId, player.name)

        // Mettre à jour la dernière connexion
        plugin.playerService.updateLastSeen(player.uniqueId)

        // Afficher les informations de nation
        if (hegemoniaPlayer.hasNation) {
            val nation = plugin.nationService.getNation(hegemoniaPlayer.nationId!!)
            if (nation != null) {
                player.sendMini("<gray>Vous êtes membre de <gold>${nation.name}</gold> [${nation.tag}]</gray>")

                // Notifier les membres de la nation
                plugin.nationService.getMembers(nation.id).forEach { (uuid, _) ->
                    if (uuid != player.uniqueId) {
                        org.bukkit.Bukkit.getPlayer(uuid)?.sendMini(
                            "<green>${player.name}</green> <gray>s'est connecté</gray>"
                        )
                    }
                }
            }
        } else {
            // Vérifier les invitations en attente
            val invites = plugin.playerService.getInvites(player.uniqueId)
            if (invites.isNotEmpty()) {
                player.sendMini("<gold>Vous avez ${invites.size} invitation(s) en attente!</gold>")
                invites.take(3).forEach { invite ->
                    val nation = plugin.nationService.getNation(invite.nationId)
                    if (nation != null) {
                        player.sendMini(
                            "<gray>- <gold>${nation.name}</gold> " +
                                    "<click:run_command:'/nation join ${nation.name}'><green>[Accepter]</green></click></gray>"
                        )
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)

        // Mettre à jour la dernière connexion
        plugin.playerService.updateLastSeen(player.uniqueId)

        // Notifier les membres de la nation
        if (hegemoniaPlayer?.hasNation == true) {
            plugin.nationService.getMembers(hegemoniaPlayer.nationId!!).forEach { (uuid, _) ->
                if (uuid != player.uniqueId) {
                    org.bukkit.Bukkit.getPlayer(uuid)?.sendMini(
                        "<red>${player.name}</red> <gray>s'est déconnecté</gray>"
                    )
                }
            }
        }

        // Retirer du cache
        plugin.playerService.uncache(player.uniqueId)
    }
}
