package com.hegemonia.economy.model

import java.time.Instant
import java.util.*

/**
 * Compte bancaire d'un joueur
 */
data class PlayerAccount(
    val playerId: UUID,
    val balance: Double,
    val bankBalance: Double,  // Épargne en banque (génère des intérêts)
    val totalEarned: Double,
    val totalSpent: Double,
    val lastTransaction: Instant?,
    val createdAt: Instant
) {
    val totalBalance: Double get() = balance + bankBalance

    companion object {
        const val INTEREST_RATE = 0.001  // 0.1% par jour
        const val MAX_BANK_BALANCE = 1_000_000.0
    }
}

/**
 * Trésorerie d'une nation
 */
data class NationTreasury(
    val nationId: Int,
    val balance: Double,
    val taxRate: Double,        // Taux d'imposition (%)
    val importTax: Double,      // Taxe sur les importations
    val exportTax: Double,      // Taxe sur les exportations
    val totalTaxCollected: Double,
    val lastTaxCollection: Instant?
) {
    companion object {
        const val MAX_TAX_RATE = 50.0
        const val MIN_TAX_RATE = 0.0
        const val DEFAULT_IMPORT_TAX = 5.0
        const val DEFAULT_EXPORT_TAX = 2.0
    }
}

/**
 * Transaction financière
 */
data class Transaction(
    val id: Int,
    val type: TransactionType,
    val senderId: UUID?,        // null si système
    val receiverId: UUID?,      // null si système
    val amount: Double,
    val fee: Double,
    val description: String,
    val timestamp: Instant
) {
    val netAmount: Double get() = amount - fee
}

/**
 * Types de transactions
 */
enum class TransactionType(val displayName: String, val icon: String) {
    TRANSFER("Transfert", "💸"),
    PAYMENT("Paiement", "💰"),
    DEPOSIT("Dépôt", "📥"),
    WITHDRAWAL("Retrait", "📤"),
    TAX("Taxe", "🏛"),
    SALARY("Salaire", "💵"),
    MARKET_BUY("Achat marché", "🛒"),
    MARKET_SELL("Vente marché", "🏪"),
    ENTERPRISE_PROFIT("Profit entreprise", "🏭"),
    INTEREST("Intérêts", "📈"),
    FINE("Amende", "⚖"),
    REWARD("Récompense", "🎁")
}

/**
 * Article sur le marché
 */
data class MarketItem(
    val id: Int,
    val material: String,       // Nom Bukkit du matériau
    val displayName: String,
    val category: MarketCategory,
    val basePrice: Double,      // Prix de base
    val currentPrice: Double,   // Prix actuel (offre/demande)
    val supply: Int,            // Stock disponible
    val demand: Int,            // Demande totale
    val priceVolatility: Double // Volatilité des prix (0-1)
) {
    /**
     * Calcule le prix d'achat (légèrement plus élevé)
     */
    fun getBuyPrice(quantity: Int): Double {
        val baseTotal = currentPrice * quantity
        val demandMultiplier = 1.0 + (demand - supply).coerceAtLeast(0) * 0.001
        return baseTotal * demandMultiplier * 1.02  // +2% marge
    }

    /**
     * Calcule le prix de vente
     */
    fun getSellPrice(quantity: Int): Double {
        val baseTotal = currentPrice * quantity
        val supplyMultiplier = 1.0 - (supply - demand).coerceAtLeast(0) * 0.001
        return baseTotal * supplyMultiplier * 0.98  // -2% marge
    }
}

/**
 * Catégories du marché
 */
enum class MarketCategory(val displayName: String, val icon: String) {
    RESOURCES("Ressources", "⛏"),
    FOOD("Nourriture", "🍖"),
    WEAPONS("Armes", "⚔"),
    ARMOR("Armures", "🛡"),
    TOOLS("Outils", "🔧"),
    BUILDING("Construction", "🏗"),
    REDSTONE("Redstone", "⚡"),
    MAGIC("Magie", "✨"),
    LUXURY("Luxe", "💎"),
    MISC("Divers", "📦")
}

/**
 * Ordre sur le marché (achat/vente)
 */
data class MarketOrder(
    val id: Int,
    val playerId: UUID,
    val itemId: Int,
    val type: OrderType,
    val quantity: Int,
    val pricePerUnit: Double,
    val filledQuantity: Int,
    val status: OrderStatus,
    val createdAt: Instant,
    val expiresAt: Instant
) {
    val remainingQuantity: Int get() = quantity - filledQuantity
    val totalValue: Double get() = quantity * pricePerUnit
    val isExpired: Boolean get() = Instant.now().isAfter(expiresAt)
}

enum class OrderType { BUY, SELL }

enum class OrderStatus {
    PENDING,    // En attente
    PARTIAL,    // Partiellement rempli
    FILLED,     // Complètement rempli
    CANCELLED,  // Annulé
    EXPIRED     // Expiré
}

/**
 * Entreprise (usine, ferme, mine, etc.)
 */
data class Enterprise(
    val id: Int,
    val uuid: UUID,
    val name: String,
    val ownerId: UUID,
    val nationId: Int?,         // Nation propriétaire (optionnel)
    val type: EnterpriseType,
    val level: Int,
    val balance: Double,
    val productionRate: Double, // Unités/heure
    val employees: Int,
    val maxEmployees: Int,
    val efficiency: Double,     // 0-100%
    val createdAt: Instant,
    val lastProduction: Instant?
) {
    /**
     * Calcule la production par heure
     */
    fun getHourlyProduction(): Double {
        val employeeBonus = employees.toDouble() / maxEmployees
        return productionRate * (efficiency / 100) * employeeBonus * level
    }

    /**
     * Coût de maintenance par heure
     */
    fun getMaintenanceCost(): Double {
        return type.baseMaintenance * level * (1 + employees * 0.1)
    }
}

/**
 * Types d'entreprises
 */
enum class EnterpriseType(
    val displayName: String,
    val icon: String,
    val baseCost: Double,
    val baseMaintenance: Double,
    val baseProduction: Double,
    val maxLevel: Int
) {
    MINE("Mine", "⛏", 5000.0, 50.0, 10.0, 10),
    FARM("Ferme", "🌾", 3000.0, 30.0, 15.0, 10),
    FACTORY("Usine", "🏭", 10000.0, 100.0, 20.0, 10),
    LUMBER_MILL("Scierie", "🪵", 4000.0, 40.0, 12.0, 10),
    FISHING("Pêcherie", "🐟", 2000.0, 20.0, 8.0, 10),
    BREWERY("Brasserie", "🍺", 6000.0, 60.0, 5.0, 10),
    FORGE("Forge", "🔨", 8000.0, 80.0, 8.0, 10),
    ENCHANTING("Enchantement", "✨", 15000.0, 150.0, 3.0, 10),
    BANK("Banque", "🏦", 50000.0, 200.0, 0.0, 5),
    TRADING_POST("Comptoir", "🏪", 7000.0, 70.0, 0.0, 10)
}

/**
 * Employé d'une entreprise
 */
data class EnterpriseEmployee(
    val enterpriseId: Int,
    val playerId: UUID,
    val role: EmployeeRole,
    val salary: Double,
    val hiredAt: Instant
)

enum class EmployeeRole(val displayName: String, val salaryMultiplier: Double) {
    WORKER("Ouvrier", 1.0),
    SUPERVISOR("Superviseur", 1.5),
    MANAGER("Gestionnaire", 2.0),
    DIRECTOR("Directeur", 3.0)
}

/**
 * Enregistrement de taxe
 */
data class TaxRecord(
    val id: Int,
    val nationId: Int,
    val playerId: UUID?,
    val type: TaxType,
    val amount: Double,
    val timestamp: Instant
)

enum class TaxType(val displayName: String) {
    INCOME("Impôt sur le revenu"),
    SALES("Taxe de vente"),
    IMPORT("Taxe d'importation"),
    EXPORT("Taxe d'exportation"),
    PROPERTY("Impôt foncier"),
    ENTERPRISE("Impôt entreprise")
}
