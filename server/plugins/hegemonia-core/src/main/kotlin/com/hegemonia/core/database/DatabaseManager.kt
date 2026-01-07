package com.hegemonia.core.database

import com.hegemonia.core.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Gestionnaire de connexion PostgreSQL avec Exposed ORM
 */
class DatabaseManager(private val config: DatabaseConfig) {

    private lateinit var dataSource: HikariDataSource
    private lateinit var database: Database

    /**
     * Établit la connexion à la base de données
     */
    fun connect() {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            driverClassName = "org.postgresql.Driver"

            // Pool configuration
            maximumPoolSize = config.poolSize
            minimumIdle = 2
            idleTimeout = 60000
            connectionTimeout = 30000
            maxLifetime = 1800000

            // Performance
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            addDataSourceProperty("useServerPrepStmts", "true")

            poolName = "Hegemonia-DB-Pool"
        }

        dataSource = HikariDataSource(hikariConfig)
        database = Database.connect(dataSource)

        // Test de connexion
        transaction(database) {
            exec("SELECT 1")
        }
    }

    /**
     * Ferme la connexion
     */
    fun disconnect() {
        if (::dataSource.isInitialized && !dataSource.isClosed) {
            dataSource.close()
        }
    }

    /**
     * Exécute une transaction
     */
    fun <T> transaction(block: () -> T): T {
        return transaction(database) {
            block()
        }
    }

    /**
     * Exécute une transaction async
     */
    suspend fun <T> transactionAsync(block: suspend () -> T): T {
        return org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction(db = database) {
            block()
        }
    }

    /**
     * Vérifie si la connexion est active
     */
    fun isConnected(): Boolean {
        return ::dataSource.isInitialized && !dataSource.isClosed
    }
}
