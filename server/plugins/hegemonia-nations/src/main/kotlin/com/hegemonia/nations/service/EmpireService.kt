package com.hegemonia.nations.service

import com.hegemonia.nations.HegemoniaNations
import com.hegemonia.nations.dao.NationTables
import com.hegemonia.nations.model.Nation
import com.hegemonia.nations.model.NationRelation
import com.hegemonia.nations.model.RelationType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Service de gestion des Empires et Vassaux
 * Gère la hiérarchie entre nations (suzerain/vassal)
 */
class EmpireService(private val plugin: HegemoniaNations) {

    private val vassalCache = ConcurrentHashMap<Int, MutableSet<Int>>() // overlordId -> vassalIds
    private val overlordCache = ConcurrentHashMap<Int, Int>() // vassalId -> overlordId

    companion object {
        // Configuration
        const val MAX_VASSALS = 5
        const val VASSAL_TAX_RATE = 0.15 // 15% des revenus vont au suzerain
        const val LIBERATION_COST_MULTIPLIER = 2.0 // Coût pour se libérer
        const val MIN_POWER_RATIO = 1.5 // Le suzerain doit avoir 1.5x la puissance du vassal
        val PROTECTION_DURATION: Duration = Duration.ofDays(7) // Protection contre les attaques
        val VASSALIZATION_COOLDOWN: Duration = Duration.ofDays(3) // Cooldown après libération
    }

    /**
     * Initialise le cache au démarrage
     */
    fun initialize() {
        transaction {
            NationTables.NationRelations
                .select { NationTables.NationRelations.relationType eq RelationType.VASSAL }
                .forEach { row ->
                    val vassalId = row[NationTables.NationRelations.nationId]
                    val overlordId = row[NationTables.NationRelations.targetNationId]

                    vassalCache.getOrPut(overlordId) { mutableSetOf() }.add(vassalId)
                    overlordCache[vassalId] = overlordId
                }
        }
        plugin.logger.info("EmpireService initialisé: ${overlordCache.size} relations vassales chargées")
    }

    // ========================================================================
    // Gestion des Vassaux
    // ========================================================================

    /**
     * Vérifie si une nation peut devenir suzerain d'une autre
     */
    fun canVassalize(overlord: Nation, target: Nation): VassalizationResult {
        // Vérifier si le suzerain n'est pas déjà vassal
        if (isVassal(overlord.id)) {
            return VassalizationResult.OVERLORD_IS_VASSAL
        }

        // Vérifier si la cible n'est pas déjà vassal
        if (isVassal(target.id)) {
            return VassalizationResult.TARGET_ALREADY_VASSAL
        }

        // Vérifier si la cible n'a pas de vassaux
        if (hasVassals(target.id)) {
            return VassalizationResult.TARGET_HAS_VASSALS
        }

        // Vérifier le nombre max de vassaux
        if (getVassalCount(overlord.id) >= MAX_VASSALS) {
            return VassalizationResult.MAX_VASSALS_REACHED
        }

        // Vérifier le ratio de puissance
        if (overlord.power < target.power * MIN_POWER_RATIO) {
            return VassalizationResult.INSUFFICIENT_POWER
        }

        // Vérifier le cooldown
        if (hasVassalizationCooldown(target.id)) {
            return VassalizationResult.ON_COOLDOWN
        }

        return VassalizationResult.SUCCESS
    }

    /**
     * Établit une relation de vassalité (après guerre ou diplomatie)
     * @return true si la vassalisation a réussi, false sinon
     */
    fun vassalize(overlordId: Int, vassalId: Int): Boolean {
        // Vérification de base: une nation ne peut pas se vassaliser elle-même
        if (overlordId == vassalId) return false

        // Vérifier que les deux nations existent
        plugin.nationService.getNationById(overlordId) ?: return false
        plugin.nationService.getNationById(vassalId) ?: return false

        return transaction {
            // Supprimer les anciennes relations entre ces deux nations
            NationTables.NationRelations.deleteWhere {
                ((nationId eq overlordId) and (targetNationId eq vassalId)) or
                ((nationId eq vassalId) and (targetNationId eq overlordId))
            }

            // Créer la relation vassal -> suzerain
            NationTables.NationRelations.insert {
                it[nationId] = vassalId
                it[targetNationId] = overlordId
                it[relationType] = RelationType.VASSAL
                it[since] = Instant.now()
                it[expiresAt] = null
            }

            // Créer la relation suzerain -> vassal
            NationTables.NationRelations.insert {
                it[nationId] = overlordId
                it[targetNationId] = vassalId
                it[relationType] = RelationType.OVERLORD
                it[since] = Instant.now()
                it[expiresAt] = null
            }

            // Mettre à jour le cache
            vassalCache.getOrPut(overlordId) { mutableSetOf() }.add(vassalId)
            overlordCache[vassalId] = overlordId

            true
        }
    }

    /**
     * Libère un vassal (coûte de l'argent ou nécessite une guerre)
     */
    fun liberateVassal(vassalId: Int, forceful: Boolean = false): Boolean {
        val overlordId = overlordCache[vassalId] ?: return false

        return transaction {
            // Supprimer les relations
            NationTables.NationRelations.deleteWhere {
                ((nationId eq overlordId) and (targetNationId eq vassalId)) or
                ((nationId eq vassalId) and (targetNationId eq overlordId))
            }

            // Si libération forcée, ajouter un cooldown
            if (forceful) {
                NationTables.NationRelations.insert {
                    it[nationId] = vassalId
                    it[targetNationId] = overlordId
                    it[relationType] = RelationType.TRUCE
                    it[since] = Instant.now()
                    it[expiresAt] = Instant.now().plus(VASSALIZATION_COOLDOWN)
                }
            }

            // Mettre à jour le cache
            vassalCache[overlordId]?.remove(vassalId)
            overlordCache.remove(vassalId)

            true
        }
    }

    /**
     * Calcule le tribut qu'un vassal doit payer
     */
    fun calculateTribute(vassalId: Int): Double {
        val vassal = plugin.nationService.getNationById(vassalId) ?: return 0.0
        return vassal.balance * VASSAL_TAX_RATE
    }

    /**
     * Collecte le tribut de tous les vassaux d'un suzerain
     * @return Le montant total collecté
     */
    fun collectTributes(overlordId: Int): Double {
        val vassals = getVassals(overlordId)
        if (vassals.isEmpty()) return 0.0

        return transaction {
            var totalTribute = 0.0

            vassals.forEach { vassalId ->
                val vassal = plugin.nationService.getNationById(vassalId) ?: return@forEach
                val tribute = vassal.balance * VASSAL_TAX_RATE

                if (tribute > 0 && vassal.balance >= tribute) {
                    // Effectuer le transfert atomiquement dans la même transaction
                    val newVassalBalance = vassal.balance - tribute
                    NationTables.Nations.update({ NationTables.Nations.id eq vassalId }) {
                        it[balance] = newVassalBalance
                    }

                    val overlord = plugin.nationService.getNationById(overlordId)
                    if (overlord != null) {
                        val newOverlordBalance = overlord.balance + tribute
                        NationTables.Nations.update({ NationTables.Nations.id eq overlordId }) {
                            it[balance] = newOverlordBalance
                        }
                        totalTribute += tribute
                    } else {
                        // Rollback du retrait si le suzerain n'existe plus
                        NationTables.Nations.update({ NationTables.Nations.id eq vassalId }) {
                            it[balance] = vassal.balance
                        }
                    }
                }
            }

            totalTribute
        }
    }

    // ========================================================================
    // Queries
    // ========================================================================

    fun isVassal(nationId: Int): Boolean = overlordCache.containsKey(nationId)

    fun isOverlord(nationId: Int): Boolean = vassalCache[nationId]?.isNotEmpty() == true

    fun hasVassals(nationId: Int): Boolean = isOverlord(nationId)

    fun getOverlord(vassalId: Int): Int? = overlordCache[vassalId]

    fun getVassals(overlordId: Int): Set<Int> = vassalCache[overlordId]?.toSet() ?: emptySet()

    fun getVassalCount(overlordId: Int): Int = vassalCache[overlordId]?.size ?: 0

    fun hasVassalizationCooldown(nationId: Int): Boolean {
        return transaction {
            NationTables.NationRelations
                .select {
                    (NationTables.NationRelations.nationId eq nationId) and
                    (NationTables.NationRelations.relationType eq RelationType.TRUCE) and
                    (NationTables.NationRelations.expiresAt greater Instant.now())
                }
                .count() > 0
        }
    }

    /**
     * Calcule la puissance totale d'un empire (suzerain + vassaux)
     */
    fun getEmpirePower(nationId: Int): Int {
        val basePower = plugin.nationService.getNationById(nationId)?.power ?: 0
        val vassalPower = getVassals(nationId).sumOf { vassalId ->
            (plugin.nationService.getNationById(vassalId)?.power ?: 0) / 2 // Vassaux comptent pour 50%
        }
        return basePower + vassalPower
    }

    /**
     * Obtient toutes les nations d'un empire (suzerain + vassaux)
     */
    fun getEmpireNations(nationId: Int): Set<Int> {
        val nations = mutableSetOf(nationId)

        // Si c'est un suzerain, ajouter ses vassaux
        nations.addAll(getVassals(nationId))

        // Si c'est un vassal, ajouter le suzerain et ses autres vassaux
        getOverlord(nationId)?.let { overlordId ->
            nations.add(overlordId)
            nations.addAll(getVassals(overlordId))
        }

        return nations
    }

    /**
     * Vérifie si deux nations sont dans le même empire
     */
    fun areInSameEmpire(nationId1: Int, nationId2: Int): Boolean {
        if (nationId1 == nationId2) return true

        // Vérifier si l'un est vassal de l'autre
        if (getOverlord(nationId1) == nationId2) return true
        if (getOverlord(nationId2) == nationId1) return true

        // Vérifier s'ils ont le même suzerain (co-vassaux)
        val overlord1 = getOverlord(nationId1)
        val overlord2 = getOverlord(nationId2)
        if (overlord1 != null && overlord1 == overlord2) return true

        // Vérifier si nation1 est suzerain et nation2 est son vassal (ou vice-versa)
        // Note: Ce cas est déjà couvert par les vérifications ci-dessus

        return false
    }
}

enum class VassalizationResult(val message: String) {
    SUCCESS("Vassalisation réussie"),
    OVERLORD_IS_VASSAL("Vous ne pouvez pas vassaliser en étant vous-même vassal"),
    TARGET_ALREADY_VASSAL("Cette nation est déjà vassale d'une autre"),
    TARGET_HAS_VASSALS("Cette nation a des vassaux et ne peut pas devenir vassale"),
    MAX_VASSALS_REACHED("Vous avez atteint le nombre maximum de vassaux"),
    INSUFFICIENT_POWER("Votre puissance est insuffisante pour vassaliser cette nation"),
    ON_COOLDOWN("Cette nation a récemment été libérée et ne peut pas être vassalisée"),
    TARGET_REFUSED("Cette nation a refusé la vassalisation")
}
