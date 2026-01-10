package com.hegemonia.economy.service

import com.hegemonia.core.database.DatabaseManager
import com.hegemonia.economy.dao.EconomyTables.Transactions
import com.hegemonia.economy.model.Transaction
import com.hegemonia.economy.model.TransactionType
import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Service de gestion des transactions
 */
class TransactionService(private val db: DatabaseManager) {

    /**
     * Récupère l'historique des transactions d'un joueur
     */
    fun getPlayerTransactions(playerId: UUID, limit: Int = 50): List<Transaction> {
        return db.transaction {
            Transactions.select {
                (Transactions.senderId eq playerId) or (Transactions.receiverId eq playerId)
            }
                .orderBy(Transactions.timestamp, SortOrder.DESC)
                .limit(limit)
                .map { it.toTransaction() }
        }
    }

    /**
     * Récupère les transactions récentes (dernières 24h)
     */
    fun getRecentTransactions(limit: Int = 100): List<Transaction> {
        val since = Instant.now().minus(24, ChronoUnit.HOURS)
        return db.transaction {
            Transactions.select { Transactions.timestamp greater since }
                .orderBy(Transactions.timestamp, SortOrder.DESC)
                .limit(limit)
                .map { it.toTransaction() }
        }
    }

    /**
     * Récupère les transactions par type
     */
    fun getTransactionsByType(type: TransactionType, limit: Int = 100): List<Transaction> {
        return db.transaction {
            Transactions.select { Transactions.type eq type }
                .orderBy(Transactions.timestamp, SortOrder.DESC)
                .limit(limit)
                .map { it.toTransaction() }
        }
    }

    /**
     * Calcule le volume total des transactions (24h)
     */
    fun getDailyVolume(): Double {
        val since = Instant.now().minus(24, ChronoUnit.HOURS)
        return db.transaction {
            Transactions.select { Transactions.timestamp greater since }
                .sumOf { it[Transactions.amount] }
        }
    }

    /**
     * Calcule les frais totaux collectés (24h)
     */
    fun getDailyFees(): Double {
        val since = Instant.now().minus(24, ChronoUnit.HOURS)
        return db.transaction {
            Transactions.select { Transactions.timestamp greater since }
                .sumOf { it[Transactions.fee] }
        }
    }

    /**
     * Compte les transactions d'un joueur
     */
    fun countPlayerTransactions(playerId: UUID): Int {
        return db.transaction {
            Transactions.select {
                (Transactions.senderId eq playerId) or (Transactions.receiverId eq playerId)
            }.count().toInt()
        }
    }

    /**
     * Convertit un ResultRow en Transaction
     */
    private fun ResultRow.toTransaction(): Transaction {
        return Transaction(
            id = this[Transactions.id].value,
            type = this[Transactions.type],
            senderId = this[Transactions.senderId],
            receiverId = this[Transactions.receiverId],
            amount = this[Transactions.amount],
            fee = this[Transactions.fee],
            description = this[Transactions.description],
            timestamp = this[Transactions.timestamp]
        )
    }
}
