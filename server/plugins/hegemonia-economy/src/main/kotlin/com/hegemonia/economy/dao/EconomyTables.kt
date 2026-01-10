package com.hegemonia.economy.dao

import com.hegemonia.economy.model.*
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Tables de base de données pour le système économique
 */
object EconomyTables {

    /**
     * Comptes des joueurs
     */
    object PlayerAccounts : IntIdTable("economy_player_accounts") {
        val playerId = uuid("player_id").uniqueIndex()
        val balance = double("balance").default(100.0)
        val bankBalance = double("bank_balance").default(0.0)
        val totalEarned = double("total_earned").default(0.0)
        val totalSpent = double("total_spent").default(0.0)
        val lastTransaction = timestamp("last_transaction").nullable()
        val createdAt = timestamp("created_at")
    }

    /**
     * Trésoreries des nations
     */
    object NationTreasuries : IntIdTable("economy_nation_treasuries") {
        val nationId = integer("nation_id").uniqueIndex()
        val balance = double("balance").default(0.0)
        val taxRate = double("tax_rate").default(10.0)
        val importTax = double("import_tax").default(5.0)
        val exportTax = double("export_tax").default(2.0)
        val totalTaxCollected = double("total_tax_collected").default(0.0)
        val lastTaxCollection = timestamp("last_tax_collection").nullable()
    }

    /**
     * Historique des transactions
     */
    object Transactions : IntIdTable("economy_transactions") {
        val type = enumerationByName("type", 32, TransactionType::class)
        val senderId = uuid("sender_id").nullable()
        val receiverId = uuid("receiver_id").nullable()
        val amount = double("amount")
        val fee = double("fee").default(0.0)
        val description = varchar("description", 255)
        val timestamp = timestamp("timestamp")

        init {
            index(false, senderId)
            index(false, receiverId)
            index(false, timestamp)
        }
    }

    /**
     * Articles du marché
     */
    object MarketItems : IntIdTable("economy_market_items") {
        val material = varchar("material", 64).uniqueIndex()
        val displayName = varchar("display_name", 64)
        val category = enumerationByName("category", 32, MarketCategory::class)
        val basePrice = double("base_price")
        val currentPrice = double("current_price")
        val supply = integer("supply").default(1000)
        val demand = integer("demand").default(1000)
        val priceVolatility = double("price_volatility").default(0.1)
    }

    /**
     * Ordres de marché (achat/vente)
     */
    object MarketOrders : IntIdTable("economy_market_orders") {
        val playerId = uuid("player_id")
        val itemId = integer("item_id").references(MarketItems.id)
        val type = enumerationByName("order_type", 16, OrderType::class)
        val quantity = integer("quantity")
        val pricePerUnit = double("price_per_unit")
        val filledQuantity = integer("filled_quantity").default(0)
        val status = enumerationByName("status", 16, OrderStatus::class).default(OrderStatus.PENDING)
        val createdAt = timestamp("created_at")
        val expiresAt = timestamp("expires_at")

        init {
            index(false, playerId)
            index(false, status)
        }
    }

    /**
     * Entreprises
     */
    object Enterprises : IntIdTable("economy_enterprises") {
        val uuid = uuid("uuid").uniqueIndex()
        val name = varchar("name", 64)
        val ownerId = uuid("owner_id")
        val nationId = integer("nation_id").nullable()
        val type = enumerationByName("type", 32, EnterpriseType::class)
        val level = integer("level").default(1)
        val balance = double("balance").default(0.0)
        val productionRate = double("production_rate")
        val employees = integer("employees").default(0)
        val maxEmployees = integer("max_employees").default(5)
        val efficiency = double("efficiency").default(100.0)
        val createdAt = timestamp("created_at")
        val lastProduction = timestamp("last_production").nullable()

        init {
            index(false, ownerId)
            index(false, nationId)
        }
    }

    /**
     * Employés d'entreprises
     */
    object EnterpriseEmployees : IntIdTable("economy_enterprise_employees") {
        val enterpriseId = integer("enterprise_id").references(Enterprises.id)
        val playerId = uuid("player_id")
        val role = enumerationByName("role", 16, EmployeeRole::class).default(EmployeeRole.WORKER)
        val salary = double("salary")
        val hiredAt = timestamp("hired_at")

        init {
            uniqueIndex(enterpriseId, playerId)
        }
    }

    /**
     * Registre des taxes collectées
     */
    object TaxRecords : IntIdTable("economy_tax_records") {
        val nationId = integer("nation_id")
        val playerId = uuid("player_id").nullable()
        val type = enumerationByName("type", 32, TaxType::class)
        val amount = double("amount")
        val timestamp = timestamp("timestamp")

        init {
            index(false, nationId)
            index(false, timestamp)
        }
    }
}
