package com.hegemonia.economy.service

import com.hegemonia.core.database.DatabaseManager
import com.hegemonia.economy.HegemoniaEconomy
import com.hegemonia.economy.dao.EconomyTables.PlayerAccounts
import com.hegemonia.economy.dao.EconomyTables.Transactions
import com.hegemonia.economy.model.PlayerAccount
import com.hegemonia.economy.model.TransactionType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Service de gestion bancaire
 */
class BankService(private val db: DatabaseManager) {

    private val accountCache = ConcurrentHashMap<UUID, PlayerAccount>()

    /**
     * Récupère ou crée un compte joueur
     */
    fun getOrCreateAccount(playerId: UUID): PlayerAccount {
        accountCache[playerId]?.let { return it }

        return db.transaction {
            val existing = PlayerAccounts.select { PlayerAccounts.playerId eq playerId }.singleOrNull()

            if (existing != null) {
                existing.toPlayerAccount()
            } else {
                val now = Instant.now()
                PlayerAccounts.insert {
                    it[PlayerAccounts.playerId] = playerId
                    it[balance] = HegemoniaEconomy.STARTING_BALANCE
                    it[bankBalance] = 0.0
                    it[totalEarned] = HegemoniaEconomy.STARTING_BALANCE
                    it[totalSpent] = 0.0
                    it[createdAt] = now
                }

                PlayerAccount(
                    playerId = playerId,
                    balance = HegemoniaEconomy.STARTING_BALANCE,
                    bankBalance = 0.0,
                    totalEarned = HegemoniaEconomy.STARTING_BALANCE,
                    totalSpent = 0.0,
                    lastTransaction = null,
                    createdAt = now
                )
            }
        }.also { accountCache[playerId] = it }
    }

    /**
     * Récupère le solde d'un joueur
     */
    fun getBalance(playerId: UUID): Double {
        return getOrCreateAccount(playerId).balance
    }

    /**
     * Récupère le solde en banque
     */
    fun getBankBalance(playerId: UUID): Double {
        return getOrCreateAccount(playerId).bankBalance
    }

    /**
     * Vérifie si un joueur a assez d'argent
     */
    fun hasEnough(playerId: UUID, amount: Double): Boolean {
        return getBalance(playerId) >= amount
    }

    /**
     * Ajoute de l'argent au portefeuille
     */
    fun deposit(playerId: UUID, amount: Double, description: String = "Dépôt"): Boolean {
        if (amount <= 0) return false

        val now = Instant.now()
        return db.transaction {
            PlayerAccounts.update({ PlayerAccounts.playerId eq playerId }) {
                it[balance] = balance + amount
                it[totalEarned] = totalEarned + amount
                it[lastTransaction] = now
            }

            logTransaction(null, playerId, amount, TransactionType.DEPOSIT, description)
            true
        }.also { invalidateCache(playerId) }
    }

    /**
     * Retire de l'argent du portefeuille
     */
    fun withdraw(playerId: UUID, amount: Double, description: String = "Retrait"): Boolean {
        if (amount <= 0) return false
        if (!hasEnough(playerId, amount)) return false

        val now = Instant.now()
        return db.transaction {
            PlayerAccounts.update({ PlayerAccounts.playerId eq playerId }) {
                it[balance] = balance - amount
                it[totalSpent] = totalSpent + amount
                it[lastTransaction] = now
            }

            logTransaction(playerId, null, amount, TransactionType.WITHDRAWAL, description)
            true
        }.also { invalidateCache(playerId) }
    }

    /**
     * Transfère de l'argent entre deux joueurs
     */
    fun transfer(senderId: UUID, receiverId: UUID, amount: Double, description: String = "Transfert"): Boolean {
        if (amount <= 0) return false
        if (senderId == receiverId) return false
        if (!hasEnough(senderId, amount)) return false

        val fee = amount * HegemoniaEconomy.TRANSACTION_FEE
        val netAmount = amount - fee
        val now = Instant.now()

        return db.transaction {
            // Débiter l'envoyeur
            PlayerAccounts.update({ PlayerAccounts.playerId eq senderId }) {
                it[balance] = balance - amount
                it[totalSpent] = totalSpent + amount
                it[lastTransaction] = now
            }

            // Créditer le receveur
            getOrCreateAccount(receiverId) // S'assurer que le compte existe
            PlayerAccounts.update({ PlayerAccounts.playerId eq receiverId }) {
                it[balance] = balance + netAmount
                it[totalEarned] = totalEarned + netAmount
                it[lastTransaction] = now
            }

            logTransaction(senderId, receiverId, amount, TransactionType.TRANSFER, description, fee)
            true
        }.also {
            invalidateCache(senderId)
            invalidateCache(receiverId)
        }
    }

    /**
     * Dépose de l'argent du portefeuille vers la banque
     */
    fun depositToBank(playerId: UUID, amount: Double): Boolean {
        if (amount <= 0) return false
        if (!hasEnough(playerId, amount)) return false

        val account = getOrCreateAccount(playerId)
        if (account.bankBalance + amount > PlayerAccount.MAX_BANK_BALANCE) return false

        return db.transaction {
            PlayerAccounts.update({ PlayerAccounts.playerId eq playerId }) {
                it[balance] = balance - amount
                it[bankBalance] = bankBalance + amount
                it[lastTransaction] = Instant.now()
            }
            true
        }.also { invalidateCache(playerId) }
    }

    /**
     * Retire de l'argent de la banque vers le portefeuille
     */
    fun withdrawFromBank(playerId: UUID, amount: Double): Boolean {
        if (amount <= 0) return false
        if (getBankBalance(playerId) < amount) return false

        return db.transaction {
            PlayerAccounts.update({ PlayerAccounts.playerId eq playerId }) {
                it[balance] = balance + amount
                it[bankBalance] = bankBalance - amount
                it[lastTransaction] = Instant.now()
            }
            true
        }.also { invalidateCache(playerId) }
    }

    /**
     * Paie un salaire (depuis le système)
     */
    fun paySalary(playerId: UUID, amount: Double, employer: String): Boolean {
        return db.transaction {
            getOrCreateAccount(playerId)
            PlayerAccounts.update({ PlayerAccounts.playerId eq playerId }) {
                it[balance] = balance + amount
                it[totalEarned] = totalEarned + amount
                it[lastTransaction] = Instant.now()
            }

            logTransaction(null, playerId, amount, TransactionType.SALARY, "Salaire de $employer")
            true
        }.also { invalidateCache(playerId) }
    }

    /**
     * Applique les intérêts bancaires
     */
    fun applyInterests() {
        db.transaction {
            PlayerAccounts.selectAll().forEach { row ->
                val bankBalance = row[PlayerAccounts.bankBalance]
                if (bankBalance > 0) {
                    val interest = bankBalance * PlayerAccount.INTEREST_RATE
                    val playerId = row[PlayerAccounts.playerId]

                    PlayerAccounts.update({ PlayerAccounts.playerId eq playerId }) {
                        it[PlayerAccounts.bankBalance] = PlayerAccounts.bankBalance + interest
                        it[totalEarned] = totalEarned + interest
                    }

                    logTransaction(null, playerId, interest, TransactionType.INTEREST, "Intérêts bancaires")
                }
            }
        }
    }

    /**
     * Récupère les 10 joueurs les plus riches
     */
    fun getTopPlayers(limit: Int = 10): List<Pair<UUID, Double>> {
        return db.transaction {
            PlayerAccounts.selectAll()
                .orderBy(PlayerAccounts.balance + PlayerAccounts.bankBalance, SortOrder.DESC)
                .limit(limit)
                .map { it[PlayerAccounts.playerId] to (it[PlayerAccounts.balance] + it[PlayerAccounts.bankBalance]) }
        }
    }

    /**
     * Enregistre une transaction
     */
    private fun logTransaction(
        senderId: UUID?,
        receiverId: UUID?,
        amount: Double,
        type: TransactionType,
        description: String,
        fee: Double = 0.0
    ) {
        Transactions.insert {
            it[Transactions.type] = type
            it[Transactions.senderId] = senderId
            it[Transactions.receiverId] = receiverId
            it[Transactions.amount] = amount
            it[Transactions.fee] = fee
            it[Transactions.description] = description
            it[timestamp] = Instant.now()
        }
    }

    /**
     * Invalide le cache d'un joueur
     */
    fun invalidateCache(playerId: UUID) {
        accountCache.remove(playerId)
    }

    /**
     * Convertit un ResultRow en PlayerAccount
     */
    private fun ResultRow.toPlayerAccount(): PlayerAccount {
        return PlayerAccount(
            playerId = this[PlayerAccounts.playerId],
            balance = this[PlayerAccounts.balance],
            bankBalance = this[PlayerAccounts.bankBalance],
            totalEarned = this[PlayerAccounts.totalEarned],
            totalSpent = this[PlayerAccounts.totalSpent],
            lastTransaction = this[PlayerAccounts.lastTransaction],
            createdAt = this[PlayerAccounts.createdAt]
        )
    }
}
