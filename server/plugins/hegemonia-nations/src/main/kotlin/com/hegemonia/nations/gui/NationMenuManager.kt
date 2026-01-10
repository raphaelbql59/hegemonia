package com.hegemonia.nations.gui

import com.hegemonia.nations.HegemoniaNations
import com.hegemonia.nations.model.*
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
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Gestionnaire des menus GUI pour le système de nations
 */
class NationMenuManager(private val plugin: HegemoniaNations) : Listener {

    private val nationService = plugin.nationService
    private val playerService = plugin.playerService

    private val menuSessions = ConcurrentHashMap<UUID, MenuSession>()

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    // ═══════════════════════════════════════════════════════════════
    // MENU PRINCIPAL NATION
    // ═══════════════════════════════════════════════════════════════

    fun openMainMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 45,
            Component.text("🏛 Menu des Nations", NamedTextColor.GOLD, TextDecoration.BOLD))

        fillBorder(inventory, Material.YELLOW_STAINED_GLASS_PANE)

        val hegemoniaPlayer = playerService.getPlayer(player.uniqueId)
        val playerNation = hegemoniaPlayer?.nationId?.let { nationService.getNation(it) }

        if (playerNation != null) {
            // === Joueur dans une nation ===

            // Info Nation (slot 4)
            inventory.setItem(4, createItem(
                Material.GOLDEN_HELMET,
                "<gold><bold>[${playerNation.tag}] ${playerNation.name}",
                listOf(
                    "<gray>Gouvernement: <white>${playerNation.governmentType.displayName}",
                    "<gray>Membres: <white>${nationService.getMemberCount(playerNation.id)}",
                    "<gray>Balance: <green>${String.format("%.2f", playerNation.balance)} H$",
                    "<gray>Puissance: <yellow>${playerNation.power}",
                    "<gray>Stabilité: <aqua>${playerNation.stability}%"
                )
            ))

            // Voir ma nation (slot 10)
            inventory.setItem(10, createItem(
                Material.BOOK,
                "<gold><bold>📜 Ma Nation",
                listOf(
                    "<gray>Voir les détails de votre nation",
                    "",
                    "<yellow>Cliquez pour voir"
                )
            ))

            // Membres (slot 12)
            inventory.setItem(12, createItem(
                Material.PLAYER_HEAD,
                "<aqua><bold>👥 Membres",
                listOf(
                    "<gray>Voir les membres de la nation",
                    "",
                    "<yellow>Cliquez pour voir la liste"
                )
            ))

            // Territoires (slot 14)
            inventory.setItem(14, createItem(
                Material.FILLED_MAP,
                "<green><bold>🗺 Territoires",
                listOf(
                    "<gray>Gérer les territoires de la nation",
                    "",
                    "<yellow>Cliquez pour voir"
                )
            ))

            // Relations (slot 16)
            inventory.setItem(16, createItem(
                Material.COMPASS,
                "<light_purple><bold>🤝 Relations",
                listOf(
                    "<gray>Voir les relations diplomatiques",
                    "",
                    "<yellow>Cliquez pour voir"
                )
            ))

            // Trésorerie (slot 28)
            inventory.setItem(28, createItem(
                Material.GOLD_INGOT,
                "<yellow><bold>💰 Trésorerie",
                listOf(
                    "<gray>Balance: <green>${String.format("%.2f", playerNation.balance)} H$",
                    "",
                    "<yellow>Cliquez pour gérer"
                )
            ))

            // Paramètres (slot 30) - si leader/minister
            val playerRole = hegemoniaPlayer.role
            if (playerRole == NationRole.LEADER || playerRole == NationRole.MINISTER) {
                inventory.setItem(30, createItem(
                    Material.COMPARATOR,
                    "<white><bold>⚙ Paramètres",
                    listOf(
                        "<gray>Modifier les paramètres de la nation",
                        "",
                        "<yellow>Cliquez pour configurer"
                    )
                ))
            }

            // Quitter la nation (slot 34)
            if (playerRole != NationRole.LEADER) {
                inventory.setItem(34, createItem(
                    Material.DARK_OAK_DOOR,
                    "<red><bold>🚪 Quitter",
                    listOf(
                        "<gray>Quitter votre nation actuelle",
                        "",
                        "<red>⚠ Action irréversible"
                    )
                ))
            } else {
                inventory.setItem(34, createItem(
                    Material.TNT,
                    "<dark_red><bold>💥 Dissoudre",
                    listOf(
                        "<gray>Dissoudre la nation",
                        "",
                        "<red>⚠ Action irréversible!",
                        "<red>Shift+Clic pour confirmer"
                    )
                ))
            }

        } else {
            // === Joueur sans nation ===

            // Message central
            inventory.setItem(13, createItem(
                Material.PAPER,
                "<yellow><bold>Vous n'avez pas de nation",
                listOf(
                    "<gray>Créez votre propre nation ou",
                    "<gray>rejoignez une nation existante!"
                )
            ))

            // Créer une nation (slot 20)
            inventory.setItem(20, createItem(
                Material.GOLDEN_HELMET,
                "<gold><bold>🏛 Créer une Nation",
                listOf(
                    "<gray>Fondez votre propre nation",
                    "<gray>et devenez son leader!",
                    "",
                    "<yellow>Coût: <white>1,000 H$",
                    "",
                    "<green>Cliquez pour créer"
                )
            ))

            // Rejoindre une nation (slot 24)
            inventory.setItem(24, createItem(
                Material.IRON_DOOR,
                "<aqua><bold>📨 Invitations",
                listOf(
                    "<gray>Voir vos invitations à rejoindre",
                    "<gray>des nations existantes",
                    "",
                    "<yellow>Cliquez pour voir"
                )
            ))
        }

        // === Toutes les nations (slot 22) ===
        val allNations = nationService.getAllNations()
        inventory.setItem(22, createItem(
            Material.GLOBE_BANNER_PATTERN,
            "<aqua><bold>🌍 Toutes les Nations",
            listOf(
                "<gray>Parcourir la liste des nations",
                "",
                "<yellow>Nations existantes: <white>${allNations.size}",
                "",
                "<yellow>Cliquez pour voir"
            )
        ))

        // Fermer (slot 40)
        inventory.setItem(40, createItem(
            Material.BARRIER,
            "<red>Fermer",
            emptyList()
        ))

        menuSessions[player.uniqueId] = MenuSession(MenuType.MAIN_MENU)
        player.openInventory(inventory)
    }

    // ═══════════════════════════════════════════════════════════════
    // MENU LISTE DES NATIONS
    // ═══════════════════════════════════════════════════════════════

    fun openNationListMenu(player: Player, page: Int = 0) {
        val inventory = Bukkit.createInventory(null, 54,
            Component.text("🌍 Toutes les Nations", NamedTextColor.AQUA, TextDecoration.BOLD))

        fillBorder(inventory, Material.LIGHT_BLUE_STAINED_GLASS_PANE)

        val allNations = nationService.getAllNations()
        val pageSize = 28
        val totalPages = (allNations.size + pageSize - 1) / pageSize
        val pageNations = allNations.drop(page * pageSize).take(pageSize)

        val slots = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        )

        pageNations.forEachIndexed { index, nation ->
            if (index < slots.size) {
                val memberCount = nationService.getMemberCount(nation.id)
                inventory.setItem(slots[index], createItem(
                    Material.YELLOW_BANNER,
                    "<gold>[${nation.tag}] ${nation.name}",
                    listOf(
                        "<gray>Gouvernement: <white>${nation.governmentType.displayName}",
                        "<gray>Membres: <white>$memberCount",
                        "<gray>Puissance: <yellow>${nation.power}",
                        "",
                        "<yellow>Cliquez pour plus d'infos"
                    ),
                    nation.id
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

        menuSessions[player.uniqueId] = MenuSession(MenuType.NATION_LIST, page = page)
        player.openInventory(inventory)
    }

    // ═══════════════════════════════════════════════════════════════
    // MENU DÉTAILS D'UNE NATION
    // ═══════════════════════════════════════════════════════════════

    fun openNationDetailsMenu(player: Player, nationId: Int) {
        val nation = nationService.getNation(nationId) ?: return
        val inventory = Bukkit.createInventory(null, 45,
            Component.text("[${nation.tag}] ${nation.name}", NamedTextColor.GOLD, TextDecoration.BOLD))

        fillBorder(inventory, Material.YELLOW_STAINED_GLASS_PANE)

        // Info principale (slot 4)
        inventory.setItem(4, createItem(
            Material.GOLDEN_HELMET,
            "<gold><bold>${nation.name}",
            listOf(
                "<gray>Tag: <white>[${nation.tag}]",
                if (nation.motto != null) "<gray>Devise: <italic>${nation.motto}" else "",
                "",
                "<gray>Gouvernement: <white>${nation.governmentType.displayName}",
                "<gray>Fondée le: <white>${java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy").format(nation.createdAt.atZone(java.time.ZoneId.systemDefault()))}"
            ).filter { it.isNotEmpty() }
        ))

        val memberCount = nationService.getMemberCount(nation.id)

        // Stats (slot 11)
        inventory.setItem(11, createItem(
            Material.PLAYER_HEAD,
            "<aqua><bold>👥 Population",
            listOf(
                "<gray>Membres: <white>$memberCount",
                "",
                "<yellow>Cliquez pour voir la liste"
            ),
            1
        ))

        // Puissance (slot 13)
        inventory.setItem(13, createItem(
            Material.DIAMOND_SWORD,
            "<red><bold>⚔ Puissance Militaire",
            listOf(
                "<gray>Puissance: <yellow>${nation.power}",
                "<gray>Fatigue de guerre: <white>${nation.warWeariness}%"
            )
        ))

        // Économie (slot 15)
        inventory.setItem(15, createItem(
            Material.GOLD_INGOT,
            "<yellow><bold>💰 Économie",
            listOf(
                "<gray>Balance: <green>${String.format("%.2f", nation.balance)} H$",
                "<gray>Taux d'imposition: <white>${nation.taxRate}%"
            )
        ))

        // Stabilité (slot 22)
        val stabilityColor = when {
            nation.stability >= 75 -> "<green>"
            nation.stability >= 50 -> "<yellow>"
            nation.stability >= 25 -> "<gold>"
            else -> "<red>"
        }
        inventory.setItem(22, createItem(
            Material.HEART_OF_THE_SEA,
            "<white><bold>⚖ Stabilité & Réputation",
            listOf(
                "<gray>Stabilité: $stabilityColor${nation.stability}%",
                "<gray>Réputation: <white>${nation.reputation}"
            )
        ))

        // Description (slot 31)
        if (nation.description.isNotBlank()) {
            inventory.setItem(31, createItem(
                Material.BOOK,
                "<white><bold>📜 Description",
                nation.description.chunked(40).map { "<gray>$it" }
            ))
        }

        // Rejoindre (si nation ouverte et joueur sans nation)
        val hegemoniaPlayer = playerService.getPlayer(player.uniqueId)
        if (hegemoniaPlayer?.nationId == null && nation.isOpen) {
            inventory.setItem(34, createItem(
                Material.LIME_DYE,
                "<green><bold>➕ Rejoindre",
                listOf(
                    "<gray>Cette nation accepte de nouveaux membres",
                    "",
                    "<green>Cliquez pour rejoindre"
                ),
                2
            ))
        }

        // Retour (slot 36)
        inventory.setItem(36, createItem(
            Material.DARK_OAK_DOOR,
            "<gray>← Retour",
            emptyList()
        ))

        menuSessions[player.uniqueId] = MenuSession(MenuType.NATION_DETAILS, nationId = nationId)
        player.openInventory(inventory)
    }

    // ═══════════════════════════════════════════════════════════════
    // MENU MEMBRES
    // ═══════════════════════════════════════════════════════════════

    fun openMembersMenu(player: Player, nationId: Int, page: Int = 0) {
        val nation = nationService.getNation(nationId) ?: return
        val inventory = Bukkit.createInventory(null, 54,
            Component.text("👥 Membres de ${nation.name}", NamedTextColor.AQUA, TextDecoration.BOLD))

        fillBorder(inventory, Material.LIGHT_BLUE_STAINED_GLASS_PANE)

        val members = nationService.getMembers(nationId)
        val pageSize = 28
        val totalPages = (members.size + pageSize - 1) / pageSize
        val pageMembers = members.drop(page * pageSize).take(pageSize)

        val slots = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        )

        pageMembers.forEachIndexed { index, (uuid, role) ->
            if (index < slots.size) {
                val memberPlayer = playerService.getPlayer(uuid)
                val playerName = memberPlayer?.username ?: "Inconnu"
                val isOnline = Bukkit.getPlayer(uuid) != null

                val head = ItemStack(Material.PLAYER_HEAD)
                val meta = head.itemMeta as? org.bukkit.inventory.meta.SkullMeta
                meta?.owningPlayer = Bukkit.getOfflinePlayer(uuid)
                meta?.displayName(parse("${if (isOnline) "<green>" else "<gray>"}$playerName"))
                meta?.lore(listOf(
                    parse("<gray>Rôle: <white>${role.displayName}"),
                    parse("<gray>Statut: ${if (isOnline) "<green>En ligne" else "<red>Hors ligne"}")
                ))
                head.itemMeta = meta

                inventory.setItem(slots[index], head)
            }
        }

        // Navigation
        if (page > 0) {
            inventory.setItem(48, createItem(Material.ARROW, "<yellow>← Page Précédente", emptyList()))
        }
        if (page < totalPages - 1) {
            inventory.setItem(50, createItem(Material.ARROW, "<yellow>Page Suivante →", emptyList()))
        }

        // Retour
        inventory.setItem(45, createItem(Material.DARK_OAK_DOOR, "<gray>← Retour", emptyList()))

        menuSessions[player.uniqueId] = MenuSession(MenuType.MEMBERS, nationId = nationId, page = page)
        player.openInventory(inventory)
    }

    // ═══════════════════════════════════════════════════════════════
    // MENU INVITATIONS
    // ═══════════════════════════════════════════════════════════════

    fun openInvitationsMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 36,
            Component.text("📨 Vos Invitations", NamedTextColor.AQUA, TextDecoration.BOLD))

        fillBorder(inventory, Material.LIGHT_BLUE_STAINED_GLASS_PANE)

        val invites = playerService.getInvites(player.uniqueId)

        if (invites.isEmpty()) {
            inventory.setItem(13, createItem(
                Material.BARRIER,
                "<gray>Aucune invitation",
                listOf("<gray>Vous n'avez aucune invitation en attente")
            ))
        } else {
            val slots = listOf(10, 11, 12, 13, 14, 15, 16)
            invites.take(7).forEachIndexed { index, invite ->
                val nation = nationService.getNation(invite.nationId)
                if (nation != null) {
                    inventory.setItem(slots[index], createItem(
                        Material.LIME_BANNER,
                        "<green>[${nation.tag}] ${nation.name}",
                        listOf(
                            "<gray>Invité par: <white>${playerService.getPlayer(invite.invitedBy)?.username ?: "Inconnu"}",
                            "",
                            "<green>Cliquez pour accepter",
                            "<red>Shift+Clic pour refuser"
                        ),
                        nation.id
                    ))
                }
            }
        }

        // Retour
        inventory.setItem(31, createItem(Material.DARK_OAK_DOOR, "<gray>← Retour", emptyList()))

        menuSessions[player.uniqueId] = MenuSession(MenuType.INVITATIONS)
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
            MenuType.NATION_LIST -> handleNationListClick(player, event.slot, customData, session.page)
            MenuType.NATION_DETAILS -> handleNationDetailsClick(player, event.slot, customData, session.nationId!!)
            MenuType.MEMBERS -> handleMembersClick(player, event.slot, session.nationId!!, session.page)
            MenuType.INVITATIONS -> handleInvitationsClick(player, event.slot, customData, event.isShiftClick)
            MenuType.CREATE_NATION -> handleCreateNationClick(player, event.slot)
            MenuType.SETTINGS -> handleSettingsClick(player, event.slot)
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (player.openInventory.topInventory.size == 0) {
                menuSessions.remove(player.uniqueId)
            }
        }, 2L)
    }

    // ═══════════════════════════════════════════════════════════════
    // HANDLERS
    // ═══════════════════════════════════════════════════════════════

    private fun handleMainMenuClick(player: Player, slot: Int) {
        val hegemoniaPlayer = playerService.getPlayer(player.uniqueId)
        val nationId = hegemoniaPlayer?.nationId

        when (slot) {
            10 -> nationId?.let { openNationDetailsMenu(player, it) }  // Ma nation
            12 -> nationId?.let { openMembersMenu(player, it) }         // Membres
            14 -> player.sendMessage(parse("<yellow>Système de territoires en développement"))  // Territoires
            16 -> player.sendMessage(parse("<yellow>Système de relations en développement"))    // Relations
            20 -> if (nationId == null) openCreateNationPrompt(player)  // Créer
            22 -> openNationListMenu(player)                            // Toutes les nations
            24 -> if (nationId == null) openInvitationsMenu(player)     // Invitations
            28 -> nationId?.let { player.sendMessage(parse("<yellow>Utilisez /nation balance")) }  // Trésorerie
            30 -> nationId?.let { openSettingsMenu(player) }            // Paramètres
            34 -> handleLeaveOrDissolve(player, hegemoniaPlayer)        // Quitter/Dissoudre
            40 -> player.closeInventory()                               // Fermer
        }
    }

    private fun handleNationListClick(player: Player, slot: Int, nationId: Int, currentPage: Int) {
        when (slot) {
            45 -> openMainMenu(player)
            48 -> if (currentPage > 0) openNationListMenu(player, currentPage - 1)
            50 -> openNationListMenu(player, currentPage + 1)
            else -> if (nationId > 0) openNationDetailsMenu(player, nationId)
        }
    }

    private fun handleNationDetailsClick(player: Player, slot: Int, action: Int, nationId: Int) {
        when (slot) {
            36 -> openNationListMenu(player)
            11 -> if (action == 1) openMembersMenu(player, nationId)
            34 -> if (action == 2) {
                // Rejoindre nation ouverte
                val success = playerService.joinNation(player.uniqueId, nationId)
                if (success) {
                    player.sendMessage(parse("<green>✓ Vous avez rejoint la nation!"))
                    player.closeInventory()
                } else {
                    player.sendMessage(parse("<red>✗ Impossible de rejoindre cette nation"))
                }
            }
        }
    }

    private fun handleMembersClick(player: Player, slot: Int, nationId: Int, currentPage: Int) {
        when (slot) {
            45 -> openNationDetailsMenu(player, nationId)
            48 -> if (currentPage > 0) openMembersMenu(player, nationId, currentPage - 1)
            50 -> openMembersMenu(player, nationId, currentPage + 1)
        }
    }

    private fun handleInvitationsClick(player: Player, slot: Int, nationId: Int, shiftClick: Boolean) {
        when (slot) {
            31 -> openMainMenu(player)
            else -> {
                if (nationId > 0) {
                    if (shiftClick) {
                        // Refuser - simplement ne rien faire (l'invite expirera)
                        player.sendMessage(parse("<yellow>Invitation refusée"))
                        openInvitationsMenu(player)
                    } else {
                        // Accepter
                        val success = playerService.joinNation(player.uniqueId, nationId)
                        if (success) {
                            player.sendMessage(parse("<green>✓ Vous avez rejoint la nation!"))
                            player.closeInventory()
                        } else {
                            player.sendMessage(parse("<red>✗ Impossible de rejoindre cette nation"))
                        }
                    }
                }
            }
        }
    }

    private fun handleCreateNationClick(player: Player, slot: Int) {
        // Géré via commande pour la saisie du nom
        player.closeInventory()
    }

    private fun handleSettingsClick(player: Player, slot: Int) {
        when (slot) {
            36 -> openMainMenu(player)
        }
    }

    private fun handleLeaveOrDissolve(player: Player, hegemoniaPlayer: HegemoniaPlayer?) {
        if (hegemoniaPlayer == null || hegemoniaPlayer.nationId == null) return

        if (hegemoniaPlayer.role == NationRole.LEADER) {
            player.sendMessage(parse("<yellow>Utilisez /nation dissolve pour dissoudre votre nation"))
        } else {
            val success = playerService.leaveNation(player.uniqueId)
            if (success) {
                player.sendMessage(parse("<green>✓ Vous avez quitté la nation"))
                player.closeInventory()
            } else {
                player.sendMessage(parse("<red>✗ Impossible de quitter la nation"))
            }
        }
    }

    private fun openCreateNationPrompt(player: Player) {
        player.closeInventory()
        player.sendMessage(parse("<gold>═══════════════════════════════════════"))
        player.sendMessage(parse("<yellow>Pour créer une nation, utilisez:"))
        player.sendMessage(parse("<white>/nation create <nom> <tag>"))
        player.sendMessage(parse(""))
        player.sendMessage(parse("<gray>Exemple: /nation create France FRA"))
        player.sendMessage(parse("<gray>Le tag doit faire 3 caractères"))
        player.sendMessage(parse("<gold>═══════════════════════════════════════"))
    }

    private fun openSettingsMenu(player: Player) {
        player.sendMessage(parse("<yellow>Utilisez /nation set <option> <valeur>"))
        player.closeInventory()
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

        meta.displayName(parse(name))

        if (lore.isNotEmpty()) {
            meta.lore(lore.map { parse(it) })
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

    private fun parse(text: String): Component {
        return try {
            com.hegemonia.core.HegemoniaCore.get().parse(text)
        } catch (e: Exception) {
            Component.text(text)
        }
    }

    private fun fillBorder(inventory: Inventory, material: Material) {
        val borderItem = ItemStack(material).apply {
            itemMeta = itemMeta?.also { it.displayName(Component.text(" ")) }
        }
        val size = inventory.size
        val columns = 9

        for (i in 0 until columns) inventory.setItem(i, borderItem)
        for (i in (size - columns) until size) inventory.setItem(i, borderItem)
        for (i in 0 until size step columns) inventory.setItem(i, borderItem)
        for (i in (columns - 1) until size step columns) inventory.setItem(i, borderItem)
    }

    // ═══════════════════════════════════════════════════════════════
    // CLASSES DE DONNÉES
    // ═══════════════════════════════════════════════════════════════

    data class MenuSession(
        val type: MenuType,
        val page: Int = 0,
        val nationId: Int? = null
    )

    enum class MenuType {
        MAIN_MENU,
        NATION_LIST,
        NATION_DETAILS,
        MEMBERS,
        INVITATIONS,
        CREATE_NATION,
        SETTINGS
    }
}
