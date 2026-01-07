package com.hegemonia.core.config

import org.bukkit.plugin.java.JavaPlugin

/**
 * Configuration centrale pour HegemoniaCore
 */
data class CoreConfig(
    val database: DatabaseConfig,
    val redis: RedisConfig,
    val server: ServerConfig
) {
    companion object {
        fun load(plugin: JavaPlugin): CoreConfig {
            val config = plugin.config

            return CoreConfig(
                database = DatabaseConfig(
                    host = config.getString("database.host", "postgres")!!,
                    port = config.getInt("database.port", 5432),
                    database = config.getString("database.name", "hegemonia_main")!!,
                    username = config.getString("database.username", "hegemonia")!!,
                    password = config.getString("database.password", "")!!,
                    poolSize = config.getInt("database.pool-size", 10)
                ),
                redis = RedisConfig(
                    host = config.getString("redis.host", "redis")!!,
                    port = config.getInt("redis.port", 6379),
                    password = config.getString("redis.password"),
                    database = config.getInt("redis.database", 0)
                ),
                server = ServerConfig(
                    serverId = config.getString("server.id", "earth")!!,
                    serverName = config.getString("server.name", "Hegemonia Earth")!!,
                    debug = config.getBoolean("server.debug", false)
                )
            )
        }
    }
}

/**
 * Configuration PostgreSQL
 */
data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
    val poolSize: Int
) {
    val jdbcUrl: String
        get() = "jdbc:postgresql://$host:$port/$database"
}

/**
 * Configuration Redis
 */
data class RedisConfig(
    val host: String,
    val port: Int,
    val password: String?,
    val database: Int
)

/**
 * Configuration serveur
 */
data class ServerConfig(
    val serverId: String,
    val serverName: String,
    val debug: Boolean
)
