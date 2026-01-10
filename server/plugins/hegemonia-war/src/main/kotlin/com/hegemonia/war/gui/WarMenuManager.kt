package com.hegemonia.war.gui

import com.hegemonia.war.HegemoniaWar
import com.hegemonia.war.model.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Gestionnaire des menus GUI pour le système de guerre
 */
class WarMenuManager(private val plugin: HegemoniaWar) : Listener {

    private val warService = plugin.warService
    private val nationBridge by lazy { plugin.nationBridge }

    // Sessions de menu actives
    private val menuSessions = ConcurrentHashMap<UUID, MenuSession>()

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    // ═══════════════════════════════════════════════════════════════
    // MENU PRINCIPAL DE GUERRE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Ouvre le menu principal de guerre
     */
    fun openMainMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 45,
            Component.text("⚔ Menu de Guerre", NamedTextColor.DARK_RED, TextDecoration.BOLD))

        val playerNationId = nationBridge.getPlayerNationId(player.uniqueId)
        val nationName = playerNationId?.let { nationBridge.getNationName(it) } ?: "Aucune"
        val canDeclareWar = nationBridge.canDeclareWar(player.uniqueId)

        // Décoration de bordure
        fillBorder(inventory, Material.RED_STAINED_GLASS_PANE)

        // === Slot 10: Déclarer la guerre ===
        if (playerNationId != null && canDeclareWar) {
            inventory.setItem(10, createItem(
                Material.DIAMOND_SWORD,
                "<red><bold>⚔ Déclarer la Guerre",
                listOf(
                    "<gray>Déclarez la guerre à une autre nation",
                    "",
                    "<yellow>Cliquez pour sélectionner une cible"
                )
            ))
        } else {
            inventory.setItem(10, createItem(
                Material.BARRIER,
                "<dark_gray>⚔ Déclarer la Guerre",
                listOf(
                    "<gray>Vous ne pouvez pas déclarer la guerre",
                    if (playerNationId == null) "<red>→ Rejoignez une nation d'abord"
                    else "<red>→ Permissions insuffisantes"
                )
            ))
        }

        // === Slot 12: Mes Guerres ===
        val myWars = playerNationId?.let { warService.getNationWars(it) } ?: emptyList()
        inventory.setItem(12, createItem(
            Material.FILLED_MAP,
            "<gold><bold>📋 Mes Guerres",
            listOf(
                "<gray>Consultez vos guerres en cours",
                "",
                "<yellow>Guerres actives: <white>${myWars.size}",
                "",
                "<yellow>Cliquez pour voir les détails"
            )
        ))

        // === Slot 14: Toutes les Guerres ===
        val allWars = warService.getActiveWars()
        inventory.setItem(14, createItem(
            Material.GLOBE_BANNER_PATTERN,
            "<aqua><bold>🌍 Guerres Mondiales",
            listOf(
                "<gray>Consultez toutes les guerres",
                "",
                "<yellow>Conflits en cours: <white>${allWars.size}",
                "",
                "<yellow>Cliquez pour voir la liste"
            )
        ))

        // === Slot 16: Rejoindre une Guerre ===
        inventory.setItem(16, createItem(
            Material.IRON_SWORD,
            "<green><bold>🤝 Rejoindre une Guerre",
            listOf(
                "<gray>Soutenez un allié dans une guerre",
                "",
                "<yellow>Cliquez pour voir les guerres disponibles"
            )
        ))

        // === Slot 28: Proposer la Paix ===
        inventory.setItem(28, createItem(
            Material.PAPER,
            "<white><bold>🕊 Proposer la Paix",
            listOf(
                "<gray>Proposez la paix à vos ennemis",
                "",
                "<yellow>Cliquez pour négocier"
            )
        ))

        // === Slot 30: Accepter/Refuser la Paix ===
        val pendingPeace = myWars.filter { it.status == WarStatus.NEGOTIATING }
        inventory.setItem(30, createItem(
            if (pendingPeace.isNotEmpty()) Material.LIME_DYE else Material.GRAY_DYE,
            "<yellow><bold>📜 Négociations",
            listOf(
                "<gray>Gérez les propositions de paix",
                "",
                if (pendingPeace.isNotEmpty())
                    "<green>→ ${pendingPeace.size} proposition(s) en attente"
                else
                    "<gray>→ Aucune proposition"
            )
        ))

        // === Slot 32: Capituler ===
        if (myWars.isNotEmpty() && nationBridge.isNationLeader(player.uniqueId)) {
            inventory.setItem(32, createItem(
                Material.WHITE_BANNER,
                "<dark_red><bold>🏳 Capituler",
                listOf(
                    "<gray>Abandonnez une guerre",
                    "",
                    "<red>⚠ Action irréversible!",
                    "<red>Votre nation subira des pénalités"
                )
            ))
        }

        // === Slot 34: Historique ===
        inventory.setItem(34, createItem(
            Material.BOOK,
            "<light_purple><bold>📖 Historique",
            listOf(
                "<gray>Consultez l'historique des guerres",
                "",
                "<yellow>Cliquez pour voir l'historique"
            )
        ))

        // === Info Nation (slot 4) ===
        inventory.setItem(4, createItem(
            Material.PLAYER_HEAD,
            "<gold>Nation: <white>$nationName",
            listOf(
                if (playerNationId != null) {
                    val tag = nationBridge.getNationTag(playerNationId) ?: "???"
                    "<gray>Tag: <white>[$tag]"
                } else "<red>Rejoignez une nation!"
            )
        ))

        // === Fermer (slot 40) ===
        inventory.setItem(40, createItem(
            Material.BARRIER,
            "<red>Fermer",
            emptyList()
        ))

        menuSessions[player.uniqueId] = MenuSession(MenuType.MAIN_MENU)
        player.openInventory(inventory)
    }

    // ═══════════════════════════════════════════════════════════════
    // MENU SÉLECTION DE CIBLE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Ouvre le menu de sélection de cible pour déclarer la guerre
     */
    fun openDeclareWarMenu(player: Player, page: Int = 0) {
        val inventory = Bukkit.createInventory(null, 54,
            Component.text("⚔ Déclarer la Guerre - Cible", NamedTextColor.RED, TextDecoration.BOLD))

        fillBorder(inventory, Material.RED_STAINED_GLASS_PANE)

        val playerNationId = nationBridge.getPlayerNationId(player.uniqueId) ?: return

        // Récupérer toutes les nations sauf la notre
        val allNations = nationBridge.getAllNationIds()
            .filter { it != playerNationId }
            .filter { !nationBridge.areNationsAllied(playerNationId, it) }
            .filter { !nationBridge.areNationsAtWar(playerNationId, it) }

        val pageSize = 28  // 4 lignes de 7 items
        val totalPages = (allNations.size + pageSize - 1) / pageSize
        val startIndex = page * pageSize
        val pageNations = allNations.drop(startIndex).take(pageSize)

        // Afficher les nations (slots 10-16, 19-25, 28-34, 37-43)
        val slots = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        )

        pageNations.forEachIndexed { index, nationId ->
            if (index < slots.size) {
                val nationName = nationBridge.getNationName(nationId) ?: "Nation #$nationId"
                val nationTag = nationBridge.getNationTag(nationId) ?: "???"
                val memberCount = nationBridge.getNationMemberCount(nationId)

                inventory.setItem(slots[index], createItem(
                    Material.RED_BANNER,
                    "<red>[$nationTag] $nationName",
                    listOf(
                        "<gray>Membres: <white>$memberCount",
                        "",
                        "<yellow>Cliquez pour sélectionner"
                    ),
                    nationId
                ))
            }
        }

        // Navigation
        if (page > 0) {
            inventory.setItem(48, createItem(
                Material.ARROW,
                "<yellow>← Page Précédente",
                listOf("<gray>Page ${page + 1}/$totalPages")
            ))
        }
        if (page < totalPages - 1) {
            inventory.setItem(50, createItem(
                Material.ARROW,
                "<yellow>Page Suivante →",
                listOf("<gray>Page ${page + 1}/$totalPages")
            ))
        }

        // Retour
        inventory.setItem(45, createItem(
            Material.DARK_OAK_DOOR,
            "<gray>← Retour",
            emptyList()
        ))

        menuSessions[player.uniqueId] = MenuSession(MenuType.DECLARE_WAR_TARGET, page = page)
        player.openInventory(inventory)
    }

    // ═══════════════════════════════════════════════════════════════
    // MENU SÉLECTION D'OBJECTIF
    // ═══════════════════════════════════════════════════════════════

    /**
     * Ouvre le menu de sélection d'objectif de guerre
     */
    fun openWarGoalMenu(player: Player, targetNationId: Int) {
        val targetName = nationBridge.getNationName(targetNationId) ?: "Nation #$targetNationId"
        val inventory = Bukkit.createInventory(null, 45,
            Component.text("⚔ Objectif vs $targetName", NamedTextColor.RED, TextDecoration.BOLD))

        fillBorder(inventory, Material.RED_STAINED_GLASS_PANE)

        // Les objectifs de guerre avec leurs materials
        val goals = listOf(
            Triple(WarGoal.CONQUEST, Material.IRON_SWORD, 10),
            Triple(WarGoal.SUBJUGATION, Material.CHAIN, 12),
            Triple(WarGoal.LIBERATION, Material.TORCH, 14),
            Triple(WarGoal.HUMILIATION, Material.ROTTEN_FLESH, 16),
            Triple(WarGoal.INDEPENDENCE, Material.FEATHER, 28),
            Triple(WarGoal.TOTAL_WAR, Material.TNT, 30),
            Triple(WarGoal.DEFENSIVE, Material.SHIELD, 32),
            Triple(WarGoal.REVENGE, Material.BLAZE_POWDER, 34)
        )

        goals.forEach { (goal, material, slot) ->
            inventory.setItem(slot, createItem(
                material,
                "<gold><bold>${goal.displayName}",
                listOf(
                    "<gray>${goal.description}",
                    "",
                    "<yellow>Score requis: <white>${goal.requiredScore}",
                    "<yellow>Multiplicateur: <white>x${goal.scoreMultiplier}",
                    "",
                    "<green>Cliquez pour choisir"
                ),
                goal.ordinal
            ))
        }

        // Retour
        inventory.setItem(36, createItem(
            Material.DARK_OAK_DOOR,
            "<gray>← Retour",
            emptyList()
        ))

        menuSessions[player.uniqueId] = MenuSession(
            MenuType.DECLARE_WAR_GOAL,
            targetNationId = targetNationId
        )
        player.openInventory(inventory)
    }

    // ═══════════════════════════════════════════════════════════════
    // MENU MES GUERRES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Ouvre le menu des guerres du joueur
     */
    fun openMyWarsMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 54,
            Component.text("📋 Mes Guerres", NamedTextColor.GOLD, TextDecoration.BOLD))

        fillBorder(inventory, Material.YELLOW_STAINED_GLASS_PANE)

        val playerNationId = nationBridge.getPlayerNationId(player.uniqueId)
        if (playerNationId == null) {
            inventory.setItem(22, createItem(
                Material.BARRIER,
                "<red>Aucune nation",
                listOf("<gray>Rejoignez une nation pour participer aux guerres")
            ))
            inventory.setItem(49, createItem(Material.DARK_OAK_DOOR, "<gray>← Retour", emptyList()))
            player.openInventory(inventory)
            return
        }

        val wars = warService.getNationWars(playerNationId)

        if (wars.isEmpty()) {
            inventory.setItem(22, createItem(
                Material.LIME_DYE,
                "<green>En paix",
                listOf("<gray>Votre nation n'est impliquée dans aucune guerre")
            ))
        } else {
            val slots = listOf(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25)
            wars.take(14).forEachIndexed { index, war ->
                val isAttacker = war.attackerId == playerNationId
                val enemyId = if (isAttacker) war.defenderId else war.attackerId
                val enemyName = nationBridge.getNationName(enemyId) ?: "Nation #$enemyId"
                val role = if (isAttacker) "Attaquant" else "Défenseur"

                val statusColor = when (war.status) {
                    WarStatus.ACTIVE -> NamedTextColor.RED
                    WarStatus.DECLARED -> NamedTextColor.YELLOW
                    WarStatus.NEGOTIATING -> NamedTextColor.AQUA
                    else -> NamedTextColor.GRAY
                }

                inventory.setItem(slots[index], createItem(
                    if (isAttacker) Material.IRON_SWORD else Material.SHIELD,
                    "<${statusColor.toString().lowercase()}>Guerre #${war.id} vs $enemyName",
                    listOf(
                        "<gray>Rôle: <white>$role",
                        "<gray>Statut: ${war.status.color}${war.status.displayName}",
                        "<gray>Objectif: <white>${war.warGoal.displayName}",
                        "",
                        "<yellow>Score:",
                        "<white>  Vous: ${if (isAttacker) war.attackerScore else war.defenderScore}",
                        "<white>  Ennemi: ${if (isAttacker) war.defenderScore else war.attackerScore}",
                        "",
                        "<green>Cliquez pour plus d'options"
                    ),
                    war.id
                ))
            }
        }

        // Retour
        inventory.setItem(49, createItem(
            Material.DARK_OAK_DOOR,
            "<gray>← Retour au menu",
            emptyList()
        ))

        menuSessions[player.uniqueId] = MenuSession(MenuType.MY_WARS)
        player.openInventory(inventory)
    }

    // ═══════════════════════════════════════════════════════════════
    // MENU DÉTAILS D'UNE GUERRE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Ouvre le menu de détails d'une guerre
     */
    fun openWarDetailsMenu(player: Player, warId: Int) {
        val war = warService.getWar(warId) ?: return
        val inventory = Bukkit.createInventory(null, 45,
            Component.text("⚔ Guerre #$warId", NamedTextColor.DARK_RED, TextDecoration.BOLD))

        fillBorder(inventory, Material.RED_STAINED_GLASS_PANE)

        val attackerName = nationBridge.getNationName(war.attackerId) ?: "Nation #${war.attackerId}"
        val defenderName = nationBridge.getNationName(war.defenderId) ?: "Nation #${war.defenderId}"
        val playerNationId = nationBridge.getPlayerNationId(player.uniqueId)
        val isParticipant = playerNationId == war.attackerId || playerNationId == war.defenderId

        // Info attaquant (slot 11)
        inventory.setItem(11, createItem(
            Material.IRON_SWORD,
            "<red><bold>⚔ $attackerName",
            listOf(
                "<gray>Score: <white>${war.attackerScore}",
                "<gray>Fatigue: <white>${war.attackerWarWeariness}%"
            )
        ))

        // VS (slot 13)
        inventory.setItem(13, createItem(
            Material.NETHER_STAR,
            "<gold><bold>VS",
            listOf(
                "<gray>Objectif: <white>${war.warGoal.displayName}",
                "<gray>Statut: ${war.status.color}${war.status.displayName}"
            )
        ))

        // Info défenseur (slot 15)
        inventory.setItem(15, createItem(
            Material.SHIELD,
            "<blue><bold>🛡 $defenderName",
            listOf(
                "<gray>Score: <white>${war.defenderScore}",
                "<gray>Fatigue: <white>${war.defenderWarWeariness}%"
            )
        ))

        // Score barre (slot 22)
        val totalScore = war.attackerScore + war.defenderScore
        val attackerPercent = if (totalScore > 0) (war.attackerScore * 100 / totalScore) else 50
        inventory.setItem(22, createItem(
            Material.EXPERIENCE_BOTTLE,
            "<yellow><bold>Score de Guerre",
            listOf(
                "<gray>Attaquant: <red>${war.attackerScore}",
                "<gray>Défenseur: <blue>${war.defenderScore}",
                "",
                buildProgressBar(attackerPercent, 20)
            )
        ))

        // Actions si participant
        if (isParticipant && nationBridge.canDeclareWar(player.uniqueId)) {
            // Proposer la paix (slot 28)
            if (war.status == WarStatus.ACTIVE) {
                inventory.setItem(28, createItem(
                    Material.PAPER,
                    "<white><bold>🕊 Proposer la Paix",
                    listOf(
                        "<gray>Envoyez une proposition de paix",
                        "",
                        "<yellow>Cliquez pour proposer"
                    ),
                    1
                ))
            }

            // Accepter/Refuser la paix (slot 30, 32)
            if (war.status == WarStatus.NEGOTIATING) {
                inventory.setItem(30, createItem(
                    Material.LIME_DYE,
                    "<green><bold>✓ Accepter la Paix",
                    listOf(
                        "<gray>Termes: ${war.peaceTerms ?: "Non définis"}",
                        "",
                        "<green>Cliquez pour accepter"
                    ),
                    2
                ))
                inventory.setItem(32, createItem(
                    Material.RED_DYE,
                    "<red><bold>✗ Refuser la Paix",
                    listOf(
                        "<gray>Continuez la guerre",
                        "",
                        "<red>Cliquez pour refuser"
                    ),
                    3
                ))
            }

            // Capituler (slot 34)
            if (war.status == WarStatus.ACTIVE && nationBridge.isNationLeader(player.uniqueId)) {
                inventory.setItem(34, createItem(
                    Material.WHITE_BANNER,
                    "<dark_red><bold>🏳 Capituler",
                    listOf(
                        "<red>⚠ Action irréversible!",
                        "",
                        "<gray>Shift+Clic pour confirmer"
                    ),
                    4
                ))
            }
        }

        // Historique (slot 40)
        inventory.setItem(40, createItem(
            Material.BOOK,
            "<light_purple><bold>📖 Voir l'Historique",
            emptyList(),
            5
        ))

        // Retour
        inventory.setItem(36, createItem(
            Material.DARK_OAK_DOOR,
            "<gray>← Retour",
            emptyList()
        ))

        menuSessions[player.uniqueId] = MenuSession(MenuType.WAR_DETAILS, warId = warId)
        player.openInventory(inventory)
    }

    // ═══════════════════════════════════════════════════════════════
    // CONFIRMATION DE DÉCLARATION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Ouvre le menu de confirmation de déclaration de guerre
     */
    fun openDeclareWarConfirmMenu(player: Player, targetNationId: Int, goal: WarGoal) {
        val targetName = nationBridge.getNationName(targetNationId) ?: "Nation #$targetNationId"
        val inventory = Bukkit.createInventory(null, 27,
            Component.text("⚠ Confirmer la Guerre", NamedTextColor.DARK_RED, TextDecoration.BOLD))

        fillBorder(inventory, Material.BLACK_STAINED_GLASS_PANE)

        // Info guerre
        inventory.setItem(4, createItem(
            Material.TNT,
            "<red><bold>Déclarer la Guerre!",
            listOf(
                "",
                "<gray>Cible: <white>$targetName",
                "<gray>Objectif: <white>${goal.displayName}",
                "",
                "<yellow>Score requis: <white>${goal.requiredScore}",
                "",
                "<red>⚠ La guerre commencera dans 24h"
            )
        ))

        // Confirmer
        inventory.setItem(11, createItem(
            Material.LIME_CONCRETE,
            "<green><bold>✓ CONFIRMER",
            listOf("<gray>Cliquez pour déclarer la guerre"),
            1
        ))

        // Annuler
        inventory.setItem(15, createItem(
            Material.RED_CONCRETE,
            "<red><bold>✗ ANNULER",
            listOf("<gray>Cliquez pour annuler"),
            0
        ))

        menuSessions[player.uniqueId] = MenuSession(
            MenuType.DECLARE_WAR_CONFIRM,
            targetNationId = targetNationId,
            warGoal = goal
        )
        player.openInventory(inventory)
    }

    // ═══════════════════════════════════════════════════════════════
    // GESTIONNAIRE D'ÉVÉNEMENTS
    // ═══════════════════════════════════════════════════════════════

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val session = menuSessions[player.uniqueId] ?: return

        event.isCancelled = true

        val item = event.currentItem ?: return
        if (item.type == Material.AIR) return

        val customData = item.itemMeta?.persistentDataContainer
            ?.get(org.bukkit.NamespacedKey(plugin, "data"), org.bukkit.persistence.PersistentDataType.INTEGER) ?: -1

        when (session.type) {
            MenuType.MAIN_MENU -> handleMainMenuClick(player, event.slot)
            MenuType.DECLARE_WAR_TARGET -> handleDeclareTargetClick(player, event.slot, customData, session.page)
            MenuType.DECLARE_WAR_GOAL -> handleDeclareGoalClick(player, event.slot, customData, session.targetNationId!!)
            MenuType.DECLARE_WAR_CONFIRM -> handleDeclareConfirmClick(player, customData, session)
            MenuType.MY_WARS -> handleMyWarsClick(player, event.slot, customData)
            MenuType.WAR_DETAILS -> handleWarDetailsClick(player, event.slot, customData, session.warId!!, event.isShiftClick)
            MenuType.ALL_WARS -> handleAllWarsClick(player, event.slot, customData)
            MenuType.NEGOTIATIONS -> handleMyWarsClick(player, event.slot, customData) // Redirige vers mes guerres
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        // Garder la session pendant un court instant pour les changements de menu
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (player.openInventory.topInventory.size == 0) {
                menuSessions.remove(player.uniqueId)
            }
        }, 2L)
    }

    // ═══════════════════════════════════════════════════════════════
    // HANDLERS DE CLICS
    // ═══════════════════════════════════════════════════════════════

    private fun handleMainMenuClick(player: Player, slot: Int) {
        when (slot) {
            10 -> openDeclareWarMenu(player)  // Déclarer la guerre
            12 -> openMyWarsMenu(player)       // Mes guerres
            14 -> openAllWarsMenu(player)      // Toutes les guerres
            16 -> openJoinWarMenu(player)      // Rejoindre une guerre
            28 -> openPeaceProposalMenu(player) // Proposer la paix
            30 -> openNegotiationsMenu(player)  // Négociations
            34 -> openHistoryMenu(player)       // Historique
            40 -> player.closeInventory()       // Fermer
        }
    }

    private fun handleDeclareTargetClick(player: Player, slot: Int, nationId: Int, currentPage: Int) {
        when (slot) {
            45 -> openMainMenu(player)  // Retour
            48 -> if (currentPage > 0) openDeclareWarMenu(player, currentPage - 1)  // Page précédente
            50 -> openDeclareWarMenu(player, currentPage + 1)  // Page suivante
            else -> if (nationId > 0) openWarGoalMenu(player, nationId)
        }
    }

    private fun handleDeclareGoalClick(player: Player, slot: Int, goalOrdinal: Int, targetNationId: Int) {
        when (slot) {
            36 -> openDeclareWarMenu(player)  // Retour
            else -> {
                if (goalOrdinal >= 0 && goalOrdinal < WarGoal.entries.size) {
                    val goal = WarGoal.entries[goalOrdinal]
                    openDeclareWarConfirmMenu(player, targetNationId, goal)
                }
            }
        }
    }

    private fun handleDeclareConfirmClick(player: Player, action: Int, session: MenuSession) {
        when (action) {
            1 -> { // Confirmer
                val targetNationId = session.targetNationId ?: return
                val goal = session.warGoal ?: return
                val playerNationId = nationBridge.getPlayerNationId(player.uniqueId) ?: return

                player.closeInventory()

                val result = warService.declareWar(playerNationId, targetNationId, goal, "Déclaration via menu")
                result.fold(
                    onSuccess = { war ->
                        val targetName = nationBridge.getNationName(targetNationId) ?: "Nation #$targetNationId"
                        player.sendMessage(Component.text("✓ Guerre déclarée contre $targetName!", NamedTextColor.GREEN))
                        player.sendMessage(Component.text("  ID de guerre: #${war.id}", NamedTextColor.GRAY))
                        player.sendMessage(Component.text("  La guerre commencera dans 24 heures.", NamedTextColor.YELLOW))
                    },
                    onFailure = { error ->
                        player.sendMessage(Component.text("✗ Échec: ${error.message}", NamedTextColor.RED))
                    }
                )
            }
            0 -> openWarGoalMenu(player, session.targetNationId!!)  // Annuler
        }
    }

    private fun handleMyWarsClick(player: Player, slot: Int, warId: Int) {
        when (slot) {
            49 -> openMainMenu(player)  // Retour
            else -> if (warId > 0) openWarDetailsMenu(player, warId)
        }
    }

    private fun handleWarDetailsClick(player: Player, slot: Int, action: Int, warId: Int, shiftClick: Boolean) {
        val war = warService.getWar(warId) ?: return
        val playerNationId = nationBridge.getPlayerNationId(player.uniqueId) ?: return

        when (slot) {
            36 -> openMyWarsMenu(player)  // Retour
            else -> when (action) {
                1 -> { // Proposer la paix
                    // Pour simplifier, on propose une paix standard
                    val success = warService.proposePeace(warId, playerNationId, "Paix proposée via menu")
                    if (success) {
                        player.sendMessage(Component.text("✓ Proposition de paix envoyée!", NamedTextColor.GREEN))
                    } else {
                        player.sendMessage(Component.text("✗ Impossible de proposer la paix", NamedTextColor.RED))
                    }
                    openWarDetailsMenu(player, warId)
                }
                2 -> { // Accepter la paix
                    val success = warService.acceptPeace(warId)
                    if (success) {
                        player.sendMessage(Component.text("✓ Paix acceptée! La guerre est terminée.", NamedTextColor.GREEN))
                        player.closeInventory()
                    } else {
                        player.sendMessage(Component.text("✗ Impossible d'accepter la paix", NamedTextColor.RED))
                    }
                }
                3 -> { // Refuser la paix
                    val success = warService.rejectPeace(warId)
                    if (success) {
                        player.sendMessage(Component.text("✗ Proposition de paix refusée", NamedTextColor.YELLOW))
                    }
                    openWarDetailsMenu(player, warId)
                }
                4 -> { // Capituler (nécessite shift+clic)
                    if (shiftClick) {
                        val success = warService.surrender(warId, playerNationId)
                        if (success) {
                            player.sendMessage(Component.text("Votre nation a capitulé...", NamedTextColor.DARK_RED))
                            player.closeInventory()
                        }
                    } else {
                        player.sendMessage(Component.text("⚠ Maintenez Shift et cliquez pour confirmer la capitulation", NamedTextColor.YELLOW))
                    }
                }
                5 -> { // Historique
                    showWarHistory(player, warId)
                }
            }
        }
    }

    private fun handleAllWarsClick(player: Player, slot: Int, warId: Int) {
        when (slot) {
            49 -> openMainMenu(player)
            else -> if (warId > 0) openWarDetailsMenu(player, warId)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MENUS SUPPLÉMENTAIRES (stubs)
    // ═══════════════════════════════════════════════════════════════

    fun openAllWarsMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 54,
            Component.text("🌍 Guerres Mondiales", NamedTextColor.AQUA, TextDecoration.BOLD))

        fillBorder(inventory, Material.LIGHT_BLUE_STAINED_GLASS_PANE)

        val wars = warService.getActiveWars()
        val slots = listOf(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)

        wars.take(21).forEachIndexed { index, war ->
            val attackerName = nationBridge.getNationName(war.attackerId) ?: "[${war.attackerId}]"
            val defenderName = nationBridge.getNationName(war.defenderId) ?: "[${war.defenderId}]"

            inventory.setItem(slots[index], createItem(
                Material.IRON_SWORD,
                "<red>$attackerName vs $defenderName",
                listOf(
                    "<gray>ID: #${war.id}",
                    "<gray>Statut: ${war.status.color}${war.status.displayName}",
                    "<gray>Score: <white>${war.attackerScore} - ${war.defenderScore}",
                    "",
                    "<yellow>Cliquez pour les détails"
                ),
                war.id
            ))
        }

        inventory.setItem(49, createItem(Material.DARK_OAK_DOOR, "<gray>← Retour", emptyList()))
        menuSessions[player.uniqueId] = MenuSession(MenuType.ALL_WARS)
        player.openInventory(inventory)
    }

    private fun openJoinWarMenu(player: Player) {
        // Simplifié pour l'instant
        player.sendMessage(Component.text("Utilisez /war join <id> <attacker|defender>", NamedTextColor.YELLOW))
        player.closeInventory()
    }

    private fun openPeaceProposalMenu(player: Player) {
        player.sendMessage(Component.text("Utilisez /war peace <id> <termes>", NamedTextColor.YELLOW))
        player.closeInventory()
    }

    private fun openNegotiationsMenu(player: Player) {
        openMyWarsMenu(player) // Redirige vers mes guerres
    }

    private fun openHistoryMenu(player: Player) {
        player.sendMessage(Component.text("Utilisez /war history <id>", NamedTextColor.YELLOW))
        player.closeInventory()
    }

    private fun showWarHistory(player: Player, warId: Int) {
        val events = warService.getWarHistory(warId)
        player.sendMessage(Component.text("═══════ Historique Guerre #$warId ═══════", NamedTextColor.LIGHT_PURPLE))
        events.takeLast(10).forEach { event ->
            player.sendMessage(Component.text("${event.type.icon} ${event.type.displayName}: ${event.description}", NamedTextColor.GRAY))
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════════

    private fun createItem(
        material: Material,
        name: String,
        lore: List<String>,
        customData: Int = -1
    ): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item

        meta.displayName(plugin.server.pluginManager.getPlugin("HegemoniaCore")
            ?.let { (it as com.hegemonia.core.HegemoniaCore).parse(name) }
            ?: Component.text(name))

        if (lore.isNotEmpty()) {
            meta.lore(lore.map { line ->
                plugin.server.pluginManager.getPlugin("HegemoniaCore")
                    ?.let { (it as com.hegemonia.core.HegemoniaCore).parse(line) }
                    ?: Component.text(line)
            })
        }

        if (customData >= 0) {
            meta.persistentDataContainer.set(
                org.bukkit.NamespacedKey(plugin, "data"),
                org.bukkit.persistence.PersistentDataType.INTEGER,
                customData
            )
        }

        item.itemMeta = meta
        return item
    }

    private fun fillBorder(inventory: Inventory, material: Material) {
        val borderItem = ItemStack(material).apply {
            itemMeta = itemMeta?.also { it.displayName(Component.text(" ")) }
        }
        val size = inventory.size
        val columns = 9

        for (i in 0 until columns) inventory.setItem(i, borderItem)  // Top
        for (i in (size - columns) until size) inventory.setItem(i, borderItem)  // Bottom
        for (i in 0 until size step columns) inventory.setItem(i, borderItem)  // Left
        for (i in (columns - 1) until size step columns) inventory.setItem(i, borderItem)  // Right
    }

    private fun buildProgressBar(percent: Int, length: Int): String {
        val filled = (percent * length / 100).coerceIn(0, length)
        val empty = length - filled
        return "<red>${"█".repeat(filled)}<gray>${"░".repeat(empty)}<blue>"
    }

    // ═══════════════════════════════════════════════════════════════
    // CLASSES DE DONNÉES
    // ═══════════════════════════════════════════════════════════════

    data class MenuSession(
        val type: MenuType,
        val page: Int = 0,
        val targetNationId: Int? = null,
        val warId: Int? = null,
        val warGoal: WarGoal? = null
    )

    enum class MenuType {
        MAIN_MENU,
        DECLARE_WAR_TARGET,
        DECLARE_WAR_GOAL,
        DECLARE_WAR_CONFIRM,
        MY_WARS,
        WAR_DETAILS,
        ALL_WARS,
        NEGOTIATIONS
    }
}
