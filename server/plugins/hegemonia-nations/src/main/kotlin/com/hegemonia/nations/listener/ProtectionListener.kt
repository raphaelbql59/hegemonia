package com.hegemonia.nations.listener

import com.hegemonia.core.utils.sendError
import com.hegemonia.nations.HegemoniaNations
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Listener pour la protection des territoires
 */
class ProtectionListener(private val plugin: HegemoniaNations) : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player

        // Bypass pour les admins
        if (player.hasPermission("hegemonia.nations.bypass")) return

        val location = event.block.location
        val regionId = getRegionId(location)

        if (regionId != null) {
            val territoryOwner = plugin.territoryService.getOwner(regionId)

            if (territoryOwner != null) {
                val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)

                // Vérifier si le joueur peut casser des blocs ici
                if (!canBuild(hegemoniaPlayer?.nationId, territoryOwner)) {
                    event.isCancelled = true
                    player.sendError("Vous ne pouvez pas casser de blocs sur ce territoire.")
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player

        // Bypass pour les admins
        if (player.hasPermission("hegemonia.nations.bypass")) return

        val location = event.block.location
        val regionId = getRegionId(location)

        if (regionId != null) {
            val territoryOwner = plugin.territoryService.getOwner(regionId)

            if (territoryOwner != null) {
                val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)

                // Vérifier si le joueur peut placer des blocs ici
                if (!canBuild(hegemoniaPlayer?.nationId, territoryOwner)) {
                    event.isCancelled = true
                    player.sendError("Vous ne pouvez pas placer de blocs sur ce territoire.")
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val block = event.clickedBlock ?: return

        // Bypass pour les admins
        if (player.hasPermission("hegemonia.nations.bypass")) return

        // Vérifier seulement les conteneurs et mécanismes
        if (!isProtectedInteraction(block.type)) return

        val regionId = getRegionId(block.location)

        if (regionId != null) {
            val territoryOwner = plugin.territoryService.getOwner(regionId)

            if (territoryOwner != null) {
                val hegemoniaPlayer = plugin.playerService.getPlayer(player.uniqueId)

                if (!canInteract(hegemoniaPlayer?.nationId, territoryOwner)) {
                    event.isCancelled = true
                    player.sendError("Vous ne pouvez pas interagir avec ce bloc sur ce territoire.")
                }
            }
        }
    }

    /**
     * Récupère l'ID de région pour une position
     * TODO: Implémenter avec le système de régions prédéfinies
     */
    private fun getRegionId(location: org.bukkit.Location): String? {
        // Pour l'instant, retourne null (pas de protection)
        // À implémenter avec le système de régions JSON
        return null
    }

    /**
     * Vérifie si un joueur peut construire sur un territoire
     */
    private fun canBuild(playerNationId: Int?, territoryOwnerId: Int): Boolean {
        // Même nation
        if (playerNationId == territoryOwnerId) return true

        // Vérifier si nations alliées
        if (playerNationId != null) {
            return plugin.nationService.areAllies(playerNationId, territoryOwnerId)
        }

        return false
    }

    /**
     * Vérifie si un joueur peut interagir sur un territoire
     */
    private fun canInteract(playerNationId: Int?, territoryOwnerId: Int): Boolean {
        // Même nation
        if (playerNationId == territoryOwnerId) return true

        // Les interactions sont plus strictes que la construction
        return false
    }

    /**
     * Vérifie si un type de bloc nécessite une protection d'interaction
     */
    private fun isProtectedInteraction(type: org.bukkit.Material): Boolean {
        return type.name.contains("CHEST") ||
                type.name.contains("FURNACE") ||
                type.name.contains("BARREL") ||
                type.name.contains("HOPPER") ||
                type.name.contains("DISPENSER") ||
                type.name.contains("DROPPER") ||
                type.name.contains("SHULKER") ||
                type.name.contains("DOOR") ||
                type.name.contains("GATE") ||
                type.name.contains("BUTTON") ||
                type.name.contains("LEVER") ||
                type.name.contains("ANVIL") ||
                type.name.contains("BREWING") ||
                type.name.contains("ENCHANT") ||
                type.name.contains("BEACON")
    }
}
