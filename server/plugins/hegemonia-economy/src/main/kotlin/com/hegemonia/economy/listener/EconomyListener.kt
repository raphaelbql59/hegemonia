package com.hegemonia.economy.listener

import com.hegemonia.core.utils.*
import com.hegemonia.economy.HegemoniaEconomy
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Listener pour les événements économiques
 */
class EconomyListener(private val plugin: HegemoniaEconomy) : Listener {

    private val bankService = plugin.bankService

    /**
     * Crée un compte pour les nouveaux joueurs
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val account = bankService.getOrCreateAccount(player.uniqueId)

        // Premier login -> message de bienvenue économique
        if (!player.hasPlayedBefore()) {
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                val currency = HegemoniaEconomy.CURRENCY_SYMBOL
                player.sendInfo("═══════ Bienvenue dans l'économie ═══════")
                player.sendInfo("Vous recevez <green>${HegemoniaEconomy.STARTING_BALANCE} $currency<reset> de départ!")
                player.sendInfo("")
                player.sendInfo("Commandes utiles:")
                player.sendInfo("<yellow>/money</yellow> - Menu économique")
                player.sendInfo("<yellow>/bank</yellow> - Gestion bancaire")
                player.sendInfo("<yellow>/market</yellow> - Marché")
            }, 60L) // 3 secondes après connexion
        }
    }

    /**
     * Sauvegarde les données à la déconnexion
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // Les données sont déjà synchronisées en base
        // Rien de spécial à faire ici
    }

    /**
     * Pénalité économique à la mort (optionnel)
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity

        // Perte d'argent à la mort (configurable)
        if (HegemoniaEconomy.DEATH_PENALTY_ENABLED) {
            val balance = bankService.getBalance(player.uniqueId)
            val penalty = (balance * HegemoniaEconomy.DEATH_PENALTY_PERCENT).coerceAtMost(HegemoniaEconomy.DEATH_PENALTY_MAX)

            if (penalty > 0) {
                bankService.withdraw(player.uniqueId, penalty, "Pénalité de mort")
                val currency = HegemoniaEconomy.CURRENCY_SYMBOL
                player.sendWarning("Vous avez perdu ${String.format("%,.2f", penalty)} $currency à cause de votre mort.")
            }
        }
    }
}
