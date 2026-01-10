package com.hegemonia.economy.service

import com.hegemonia.core.database.DatabaseManager
import com.hegemonia.economy.dao.EconomyTables.MarketItems
import com.hegemonia.economy.dao.EconomyTables.MarketOrders
import com.hegemonia.economy.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Service de gestion du marché
 */
class MarketService(private val db: DatabaseManager) {

    private val itemCache = ConcurrentHashMap<Int, MarketItem>()
    private val itemByMaterial = ConcurrentHashMap<String, Int>()

    init {
        loadMarketItems()
    }

    /**
     * Charge les items du marché au démarrage
     */
    private fun loadMarketItems() {
        db.transaction {
            // S'assurer que les items de base existent
            initializeBaseItems()

            MarketItems.selectAll().forEach { row ->
                val item = row.toMarketItem()
                itemCache[item.id] = item
                itemByMaterial[item.material] = item.id
            }
        }
    }

    /**
     * Initialise les items de base du marché
     */
    private fun initializeBaseItems() {
        val baseItems = listOf(
            // Ressources
            Triple("COBBLESTONE", "Pierre", MarketCategory.RESOURCES) to 0.1,
            Triple("STONE", "Pierre lisse", MarketCategory.RESOURCES) to 0.15,
            Triple("IRON_INGOT", "Lingot de fer", MarketCategory.RESOURCES) to 5.0,
            Triple("GOLD_INGOT", "Lingot d'or", MarketCategory.RESOURCES) to 15.0,
            Triple("DIAMOND", "Diamant", MarketCategory.RESOURCES) to 100.0,
            Triple("EMERALD", "Émeraude", MarketCategory.RESOURCES) to 80.0,
            Triple("COAL", "Charbon", MarketCategory.RESOURCES) to 1.0,
            Triple("REDSTONE", "Redstone", MarketCategory.REDSTONE) to 2.0,
            Triple("LAPIS_LAZULI", "Lapis-Lazuli", MarketCategory.RESOURCES) to 3.0,
            Triple("COPPER_INGOT", "Lingot de cuivre", MarketCategory.RESOURCES) to 3.0,

            // Bois
            Triple("OAK_LOG", "Bûche de chêne", MarketCategory.BUILDING) to 0.5,
            Triple("SPRUCE_LOG", "Bûche de sapin", MarketCategory.BUILDING) to 0.5,
            Triple("BIRCH_LOG", "Bûche de bouleau", MarketCategory.BUILDING) to 0.5,
            Triple("OAK_PLANKS", "Planches de chêne", MarketCategory.BUILDING) to 0.15,

            // Nourriture
            Triple("WHEAT", "Blé", MarketCategory.FOOD) to 0.2,
            Triple("BREAD", "Pain", MarketCategory.FOOD) to 1.0,
            Triple("COOKED_BEEF", "Steak", MarketCategory.FOOD) to 3.0,
            Triple("COOKED_PORKCHOP", "Côtelette cuite", MarketCategory.FOOD) to 3.0,
            Triple("GOLDEN_APPLE", "Pomme dorée", MarketCategory.FOOD) to 200.0,
            Triple("APPLE", "Pomme", MarketCategory.FOOD) to 1.5,
            Triple("CARROT", "Carotte", MarketCategory.FOOD) to 0.3,
            Triple("POTATO", "Pomme de terre", MarketCategory.FOOD) to 0.3,

            // Armes
            Triple("IRON_SWORD", "Épée en fer", MarketCategory.WEAPONS) to 25.0,
            Triple("DIAMOND_SWORD", "Épée en diamant", MarketCategory.WEAPONS) to 250.0,
            Triple("BOW", "Arc", MarketCategory.WEAPONS) to 15.0,
            Triple("ARROW", "Flèche", MarketCategory.WEAPONS) to 0.5,
            Triple("CROSSBOW", "Arbalète", MarketCategory.WEAPONS) to 30.0,

            // Armures
            Triple("IRON_HELMET", "Casque en fer", MarketCategory.ARMOR) to 25.0,
            Triple("IRON_CHESTPLATE", "Plastron en fer", MarketCategory.ARMOR) to 40.0,
            Triple("IRON_LEGGINGS", "Jambières en fer", MarketCategory.ARMOR) to 35.0,
            Triple("IRON_BOOTS", "Bottes en fer", MarketCategory.ARMOR) to 20.0,
            Triple("DIAMOND_HELMET", "Casque en diamant", MarketCategory.ARMOR) to 250.0,
            Triple("DIAMOND_CHESTPLATE", "Plastron en diamant", MarketCategory.ARMOR) to 400.0,
            Triple("DIAMOND_LEGGINGS", "Jambières en diamant", MarketCategory.ARMOR) to 350.0,
            Triple("DIAMOND_BOOTS", "Bottes en diamant", MarketCategory.ARMOR) to 200.0,
            Triple("SHIELD", "Bouclier", MarketCategory.ARMOR) to 15.0,

            // Outils
            Triple("IRON_PICKAXE", "Pioche en fer", MarketCategory.TOOLS) to 15.0,
            Triple("DIAMOND_PICKAXE", "Pioche en diamant", MarketCategory.TOOLS) to 150.0,
            Triple("IRON_AXE", "Hache en fer", MarketCategory.TOOLS) to 15.0,
            Triple("IRON_SHOVEL", "Pelle en fer", MarketCategory.TOOLS) to 5.0,

            // Construction
            Triple("GLASS", "Verre", MarketCategory.BUILDING) to 0.5,
            Triple("BRICK", "Brique", MarketCategory.BUILDING) to 1.0,
            Triple("STONE_BRICKS", "Pierres taillées", MarketCategory.BUILDING) to 0.3,

            // Magie
            Triple("ENDER_PEARL", "Perle de l'Ender", MarketCategory.MAGIC) to 50.0,
            Triple("BLAZE_ROD", "Bâton de Blaze", MarketCategory.MAGIC) to 30.0,
            Triple("EXPERIENCE_BOTTLE", "Fiole d'XP", MarketCategory.MAGIC) to 20.0,

            // Luxe
            Triple("NETHERITE_INGOT", "Lingot de Netherite", MarketCategory.LUXURY) to 1000.0,
            Triple("BEACON", "Balise", MarketCategory.LUXURY) to 5000.0,
            Triple("NETHER_STAR", "Étoile du Nether", MarketCategory.LUXURY) to 3000.0
        )

        baseItems.forEach { (info, price) ->
            val (material, name, category) = info
            val exists = MarketItems.select { MarketItems.material eq material }.count() > 0
            if (!exists) {
                MarketItems.insert {
                    it[MarketItems.material] = material
                    it[displayName] = name
                    it[MarketItems.category] = category
                    it[basePrice] = price
                    it[currentPrice] = price
                    it[supply] = 1000
                    it[demand] = 1000
                    it[priceVolatility] = 0.1
                }
            }
        }
    }

    /**
     * Récupère un item par son matériau
     */
    fun getItem(material: String): MarketItem? {
        val id = itemByMaterial[material] ?: return null
        return itemCache[id]
    }

    /**
     * Récupère un item par ID
     */
    fun getItemById(id: Int): MarketItem? {
        return itemCache[id]
    }

    /**
     * Récupère tous les items d'une catégorie
     */
    fun getItemsByCategory(category: MarketCategory): List<MarketItem> {
        return itemCache.values.filter { it.category == category }
    }

    /**
     * Récupère tous les items du marché
     */
    fun getAllItems(): List<MarketItem> {
        return itemCache.values.toList()
    }

    /**
     * Calcule le prix d'achat pour une quantité
     */
    fun getBuyPrice(material: String, quantity: Int): Double? {
        val item = getItem(material) ?: return null
        return item.getBuyPrice(quantity)
    }

    /**
     * Calcule le prix de vente pour une quantité
     */
    fun getSellPrice(material: String, quantity: Int): Double? {
        val item = getItem(material) ?: return null
        return item.getSellPrice(quantity)
    }

    /**
     * Enregistre un achat
     */
    fun recordPurchase(material: String, quantity: Int): Boolean {
        val item = getItem(material) ?: return false

        return db.transaction {
            // Augmenter la demande, réduire l'offre
            MarketItems.update({ MarketItems.material eq material }) {
                it[demand] = demand + quantity
                it[supply] = supply - (quantity / 2).coerceAtLeast(1)
            }
            true
        }.also { reloadItem(material) }
    }

    /**
     * Enregistre une vente
     */
    fun recordSale(material: String, quantity: Int): Boolean {
        val item = getItem(material) ?: return false

        return db.transaction {
            // Augmenter l'offre, réduire la demande
            MarketItems.update({ MarketItems.material eq material }) {
                it[supply] = supply + quantity
                it[demand] = demand - (quantity / 2).coerceAtLeast(1)
            }
            true
        }.also { reloadItem(material) }
    }

    /**
     * Crée un ordre de marché
     */
    fun createOrder(
        playerId: UUID,
        itemId: Int,
        type: OrderType,
        quantity: Int,
        pricePerUnit: Double
    ): Int? {
        val now = Instant.now()
        val expiresAt = now.plus(7, ChronoUnit.DAYS) // Expire dans 7 jours

        return db.transaction {
            MarketOrders.insertAndGetId {
                it[MarketOrders.playerId] = playerId
                it[MarketOrders.itemId] = itemId
                it[MarketOrders.type] = type
                it[MarketOrders.quantity] = quantity
                it[MarketOrders.pricePerUnit] = pricePerUnit
                it[filledQuantity] = 0
                it[status] = OrderStatus.PENDING
                it[createdAt] = now
                it[MarketOrders.expiresAt] = expiresAt
            }.value
        }
    }

    /**
     * Récupère les ordres d'un joueur
     */
    fun getPlayerOrders(playerId: UUID): List<MarketOrder> {
        return db.transaction {
            MarketOrders.select {
                (MarketOrders.playerId eq playerId) and
                        (MarketOrders.status inList listOf(OrderStatus.PENDING, OrderStatus.PARTIAL))
            }.map { it.toMarketOrder() }
        }
    }

    /**
     * Annule un ordre
     */
    fun cancelOrder(orderId: Int, playerId: UUID): Boolean {
        return db.transaction {
            MarketOrders.update({
                (MarketOrders.id eq orderId) and
                        (MarketOrders.playerId eq playerId) and
                        (MarketOrders.status inList listOf(OrderStatus.PENDING, OrderStatus.PARTIAL))
            }) {
                it[status] = OrderStatus.CANCELLED
            } > 0
        }
    }

    /**
     * Met à jour les prix du marché selon l'offre et la demande
     */
    fun updateMarketPrices() {
        db.transaction {
            MarketItems.selectAll().forEach { row ->
                val id = row[MarketItems.id].value
                val basePrice = row[MarketItems.basePrice]
                val supply = row[MarketItems.supply].coerceAtLeast(1)
                val demand = row[MarketItems.demand].coerceAtLeast(1)
                val volatility = row[MarketItems.priceVolatility]

                // Calculer le nouveau prix basé sur offre/demande
                val ratio = demand.toDouble() / supply
                val priceFactor = (ratio - 1.0) * volatility + 1.0
                val newPrice = (basePrice * priceFactor).coerceIn(basePrice * 0.5, basePrice * 2.0)

                MarketItems.update({ MarketItems.id eq id }) {
                    it[currentPrice] = newPrice
                    // Normaliser progressivement l'offre et la demande
                    it[MarketItems.supply] = ((supply + 1000) / 2)
                    it[MarketItems.demand] = ((demand + 1000) / 2)
                }
            }
        }

        // Recharger le cache
        loadMarketItems()
    }

    /**
     * Recharge un item spécifique dans le cache
     */
    private fun reloadItem(material: String) {
        db.transaction {
            MarketItems.select { MarketItems.material eq material }
                .singleOrNull()
                ?.toMarketItem()
                ?.let {
                    itemCache[it.id] = it
                    itemByMaterial[it.material] = it.id
                }
        }
    }

    /**
     * Convertit un ResultRow en MarketItem
     */
    private fun ResultRow.toMarketItem(): MarketItem {
        return MarketItem(
            id = this[MarketItems.id].value,
            material = this[MarketItems.material],
            displayName = this[MarketItems.displayName],
            category = this[MarketItems.category],
            basePrice = this[MarketItems.basePrice],
            currentPrice = this[MarketItems.currentPrice],
            supply = this[MarketItems.supply],
            demand = this[MarketItems.demand],
            priceVolatility = this[MarketItems.priceVolatility]
        )
    }

    /**
     * Convertit un ResultRow en MarketOrder
     */
    private fun ResultRow.toMarketOrder(): MarketOrder {
        return MarketOrder(
            id = this[MarketOrders.id].value,
            playerId = this[MarketOrders.playerId],
            itemId = this[MarketOrders.itemId],
            type = this[MarketOrders.type],
            quantity = this[MarketOrders.quantity],
            pricePerUnit = this[MarketOrders.pricePerUnit],
            filledQuantity = this[MarketOrders.filledQuantity],
            status = this[MarketOrders.status],
            createdAt = this[MarketOrders.createdAt],
            expiresAt = this[MarketOrders.expiresAt]
        )
    }
}
