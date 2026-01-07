package com.hegemonia.core.database

import com.hegemonia.core.config.RedisConfig
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisPubSub
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

/**
 * Gestionnaire Redis pour cache et pub/sub cross-server
 */
class RedisManager(private val config: RedisConfig) {

    private lateinit var pool: JedisPool
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    private val subscribers = mutableMapOf<String, JedisPubSub>()

    /**
     * Établit la connexion à Redis
     */
    fun connect() {
        val poolConfig = JedisPoolConfig().apply {
            maxTotal = 16
            maxIdle = 8
            minIdle = 2
            testOnBorrow = true
            testOnReturn = true
            testWhileIdle = true
            blockWhenExhausted = true
        }

        pool = if (config.password.isNullOrEmpty()) {
            JedisPool(poolConfig, config.host, config.port, 2000, null, config.database)
        } else {
            JedisPool(poolConfig, config.host, config.port, 2000, config.password, config.database)
        }

        // Test de connexion
        pool.resource.use { jedis ->
            jedis.ping()
        }
    }

    /**
     * Ferme la connexion
     */
    fun disconnect() {
        // Arrêter tous les subscribers
        subscribers.values.forEach { it.unsubscribe() }
        subscribers.clear()

        executor.shutdown()

        if (::pool.isInitialized && !pool.isClosed) {
            pool.close()
        }
    }

    // ========================================
    // CACHE OPERATIONS
    // ========================================

    /**
     * Récupère une valeur du cache
     */
    fun get(key: String): String? {
        return pool.resource.use { it.get(key) }
    }

    /**
     * Stocke une valeur dans le cache
     */
    fun set(key: String, value: String, ttlSeconds: Long? = null) {
        pool.resource.use { jedis ->
            if (ttlSeconds != null) {
                jedis.setex(key, ttlSeconds, value)
            } else {
                jedis.set(key, value)
            }
        }
    }

    /**
     * Supprime une clé
     */
    fun delete(key: String) {
        pool.resource.use { it.del(key) }
    }

    /**
     * Vérifie si une clé existe
     */
    fun exists(key: String): Boolean {
        return pool.resource.use { it.exists(key) }
    }

    /**
     * Récupère une valeur de hash
     */
    fun hget(key: String, field: String): String? {
        return pool.resource.use { it.hget(key, field) }
    }

    /**
     * Stocke une valeur de hash
     */
    fun hset(key: String, field: String, value: String) {
        pool.resource.use { it.hset(key, field, value) }
    }

    /**
     * Récupère toutes les valeurs d'un hash
     */
    fun hgetAll(key: String): Map<String, String> {
        return pool.resource.use { it.hgetAll(key) }
    }

    // ========================================
    // PUB/SUB OPERATIONS
    // ========================================

    /**
     * Publie un message sur un canal
     */
    fun publish(channel: String, message: String) {
        pool.resource.use { it.publish(channel, message) }
    }

    /**
     * S'abonne à un canal
     */
    fun subscribe(channel: String, onMessage: (String, String) -> Unit) {
        val pubSub = object : JedisPubSub() {
            override fun onMessage(channel: String, message: String) {
                onMessage(channel, message)
            }
        }

        subscribers[channel] = pubSub

        executor.submit {
            pool.resource.use { jedis ->
                jedis.subscribe(pubSub, channel)
            }
        }
    }

    /**
     * Se désabonne d'un canal
     */
    fun unsubscribe(channel: String) {
        subscribers[channel]?.unsubscribe()
        subscribers.remove(channel)
    }

    /**
     * Vérifie si la connexion est active
     */
    fun isConnected(): Boolean {
        return ::pool.isInitialized && !pool.isClosed
    }
}
