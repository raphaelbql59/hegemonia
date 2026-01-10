package com.hegemonia.war.bridge

import org.bukkit.Bukkit
import java.util.*

/**
 * Bridge pour accéder aux fonctionnalités de HegemoniaNations depuis HegemoniaWar
 * Utilise la réflexion pour éviter les dépendances compile-time strictes
 */
class NationBridge {

    private var nationsPlugin: Any? = null
    private var nationService: Any? = null
    private var playerService: Any? = null
    private var isAvailable = false

    /**
     * Initialise le bridge avec HegemoniaNations
     */
    fun initialize(): Boolean {
        try {
            nationsPlugin = Bukkit.getPluginManager().getPlugin("HegemoniaNations")
            if (nationsPlugin == null) {
                Bukkit.getLogger().warning("[HegemoniaWar] HegemoniaNations non trouvé - fonctionnalités nations désactivées")
                return false
            }

            // Récupérer les services via réflexion
            val pluginClass = nationsPlugin!!::class.java
            nationService = pluginClass.getMethod("getNationService").invoke(nationsPlugin)
            playerService = pluginClass.getMethod("getPlayerService").invoke(nationsPlugin)

            isAvailable = true
            Bukkit.getLogger().info("[HegemoniaWar] Bridge HegemoniaNations initialisé avec succès")
            return true
        } catch (e: Exception) {
            Bukkit.getLogger().warning("[HegemoniaWar] Erreur initialisation bridge: ${e.message}")
            isAvailable = false
            return false
        }
    }

    /**
     * Vérifie si HegemoniaNations est disponible
     */
    fun isAvailable(): Boolean = isAvailable

    /**
     * Récupère l'ID de la nation d'un joueur
     */
    fun getPlayerNationId(playerId: UUID): Int? {
        if (!isAvailable || playerService == null) return null
        return try {
            val playerServiceClass = playerService!!::class.java
            val player = playerServiceClass.getMethod("getPlayer", UUID::class.java)
                .invoke(playerService, playerId) ?: return null
            val playerClass = player::class.java
            playerClass.getMethod("getNationId").invoke(player) as? Int
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Récupère le nom d'une nation par ID
     */
    fun getNationName(nationId: Int): String? {
        if (!isAvailable || nationService == null) return null
        return try {
            val nation = getNationById(nationId) ?: return null
            nation::class.java.getMethod("getName").invoke(nation) as? String
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Récupère le tag d'une nation par ID
     */
    fun getNationTag(nationId: Int): String? {
        if (!isAvailable || nationService == null) return null
        return try {
            val nation = getNationById(nationId) ?: return null
            nation::class.java.getMethod("getTag").invoke(nation) as? String
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Récupère une nation par son nom
     */
    fun getNationIdByName(name: String): Int? {
        if (!isAvailable || nationService == null) return null
        return try {
            val nationServiceClass = nationService!!::class.java
            val nation = nationServiceClass.getMethod("getNationByName", String::class.java)
                .invoke(nationService, name) ?: return null
            nation::class.java.getMethod("getId").invoke(nation) as? Int
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Récupère une nation par son tag
     */
    fun getNationIdByTag(tag: String): Int? {
        if (!isAvailable || nationService == null) return null
        return try {
            val nationServiceClass = nationService!!::class.java
            val nation = nationServiceClass.getMethod("getNationByTag", String::class.java)
                .invoke(nationService, tag) ?: return null
            nation::class.java.getMethod("getId").invoke(nation) as? Int
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Vérifie si un joueur est le leader de sa nation
     */
    fun isNationLeader(playerId: UUID): Boolean {
        if (!isAvailable || playerService == null) return false
        return try {
            val playerServiceClass = playerService!!::class.java
            val player = playerServiceClass.getMethod("getPlayer", UUID::class.java)
                .invoke(playerService, playerId) ?: return false
            val role = player::class.java.getMethod("getRole").invoke(player)
            role?.toString() == "LEADER"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Vérifie si un joueur peut déclarer la guerre (Leader ou Général)
     */
    fun canDeclareWar(playerId: UUID): Boolean {
        if (!isAvailable || playerService == null) return false
        return try {
            val playerServiceClass = playerService!!::class.java
            val player = playerServiceClass.getMethod("getPlayer", UUID::class.java)
                .invoke(playerService, playerId) ?: return false
            val role = player::class.java.getMethod("getRole").invoke(player)?.toString()
            role in listOf("LEADER", "GENERAL", "MINISTER")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Récupère tous les membres d'une nation
     */
    fun getNationMembers(nationId: Int): List<UUID> {
        if (!isAvailable || nationService == null) return emptyList()
        return try {
            val nationServiceClass = nationService!!::class.java
            val members = nationServiceClass.getMethod("getMembers", Int::class.java)
                .invoke(nationService, nationId) as? List<*> ?: return emptyList()
            members.mapNotNull { pair ->
                if (pair is Pair<*, *>) pair.first as? UUID else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Récupère le nombre de membres d'une nation
     */
    fun getNationMemberCount(nationId: Int): Int {
        if (!isAvailable || nationService == null) return 0
        return try {
            val nationServiceClass = nationService!!::class.java
            nationServiceClass.getMethod("getMemberCount", Int::class.java)
                .invoke(nationService, nationId) as? Int ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Vérifie si deux nations sont alliées
     */
    fun areNationsAllied(nationId1: Int, nationId2: Int): Boolean {
        if (!isAvailable || nationService == null) return false
        return try {
            val nationServiceClass = nationService!!::class.java
            nationServiceClass.getMethod("areAllies", Int::class.java, Int::class.java)
                .invoke(nationService, nationId1, nationId2) as? Boolean ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Vérifie si deux nations sont en guerre
     */
    fun areNationsAtWar(nationId1: Int, nationId2: Int): Boolean {
        if (!isAvailable || nationService == null) return false
        return try {
            val nationServiceClass = nationService!!::class.java
            nationServiceClass.getMethod("areAtWar", Int::class.java, Int::class.java)
                .invoke(nationService, nationId1, nationId2) as? Boolean ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Modifie la fatigue de guerre d'une nation
     */
    fun modifyWarWeariness(nationId: Int, amount: Int): Boolean {
        if (!isAvailable || nationService == null) return false
        return try {
            val nation = getNationById(nationId) ?: return false
            val nationClass = nation::class.java
            val currentWeariness = nationClass.getMethod("getWarWeariness").invoke(nation) as? Int ?: 0
            val newWeariness = (currentWeariness + amount).coerceIn(0, 100)

            // Créer une copie avec la nouvelle fatigue
            val copyMethod = nationClass.methods.find { it.name == "copy" }
            if (copyMethod != null) {
                val updatedNation = nationClass.getMethod("copy",
                    *copyMethod.parameterTypes
                ).invoke(nation, /* ... paramètres de copy ... */)

                val nationServiceClass = nationService!!::class.java
                nationServiceClass.getMethod("updateNation", nation::class.java)
                    .invoke(nationService, updatedNation)
                return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Récupère toutes les nations
     */
    fun getAllNationIds(): List<Int> {
        if (!isAvailable || nationService == null) return emptyList()
        return try {
            val nationServiceClass = nationService!!::class.java
            val nations = nationServiceClass.getMethod("getAllNations")
                .invoke(nationService) as? List<*> ?: return emptyList()
            nations.mapNotNull { nation ->
                nation?.let { it::class.java.getMethod("getId").invoke(it) as? Int }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Helper privé pour récupérer une nation par ID
     */
    private fun getNationById(nationId: Int): Any? {
        if (!isAvailable || nationService == null) return null
        return try {
            val nationServiceClass = nationService!!::class.java
            nationServiceClass.getMethod("getNation", Int::class.java)
                .invoke(nationService, nationId)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private var instance: NationBridge? = null

        fun getInstance(): NationBridge {
            if (instance == null) {
                instance = NationBridge()
            }
            return instance!!
        }
    }
}
