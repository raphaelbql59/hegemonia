package com.hegemonia.war.dao

import com.hegemonia.war.model.*
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Définitions des tables pour le système de guerre
 */
object WarTables {

    /**
     * Table des guerres
     */
    object Wars : IntIdTable("wars") {
        val uuid = uuid("uuid").uniqueIndex()
        val attackerId = integer("attacker_id")
        val defenderId = integer("defender_id")
        val status = enumerationByName("status", 20, WarStatus::class).default(WarStatus.DECLARED)
        val warGoal = enumerationByName("war_goal", 20, WarGoal::class).default(WarGoal.CONQUEST)
        val declaredAt = timestamp("declared_at")
        val startedAt = timestamp("started_at").nullable()
        val endedAt = timestamp("ended_at").nullable()
        val attackerScore = integer("attacker_score").default(0)
        val defenderScore = integer("defender_score").default(0)
        val attackerWarWeariness = integer("attacker_war_weariness").default(0)
        val defenderWarWeariness = integer("defender_war_weariness").default(0)
        val reason = text("reason").default("")
        val peaceTerms = text("peace_terms").nullable()
        val winnerId = integer("winner_id").nullable()

        init {
            index(false, attackerId)
            index(false, defenderId)
            index(false, status)
        }
    }

    /**
     * Table des participants aux guerres (alliés)
     */
    object WarParticipants : IntIdTable("war_participants") {
        val warId = integer("war_id").references(Wars.id)
        val nationId = integer("nation_id")
        val side = enumerationByName("side", 10, WarSide::class)
        val joinedAt = timestamp("joined_at")
        val contribution = integer("contribution").default(0)
        val casualties = integer("casualties").default(0)
        val isOriginal = bool("is_original").default(false)

        init {
            uniqueIndex(warId, nationId)
        }
    }

    /**
     * Table des batailles
     */
    object Battles : IntIdTable("battles") {
        val uuid = uuid("uuid").uniqueIndex()
        val warId = integer("war_id").references(Wars.id)
        val type = enumerationByName("type", 20, BattleType::class).default(BattleType.BATTLE)
        val status = enumerationByName("status", 20, BattleStatus::class).default(BattleStatus.SCHEDULED)
        val regionId = varchar("region_id", 64)
        val attackerNationId = integer("attacker_nation_id")
        val defenderNationId = integer("defender_nation_id")
        val scheduledAt = timestamp("scheduled_at").nullable()
        val startedAt = timestamp("started_at").nullable()
        val endedAt = timestamp("ended_at").nullable()
        val attackerKills = integer("attacker_kills").default(0)
        val defenderKills = integer("defender_kills").default(0)
        val attackerDeaths = integer("attacker_deaths").default(0)
        val defenderDeaths = integer("defender_deaths").default(0)
        val winnerId = integer("winner_id").nullable()
        val scoreAwarded = integer("score_awarded").default(0)
        // Zone de combat
        val centerX = double("center_x").default(0.0)
        val centerY = double("center_y").default(64.0)
        val centerZ = double("center_z").default(0.0)
        val radius = integer("radius").default(100)

        init {
            index(false, warId)
            index(false, status)
        }
    }

    /**
     * Table des participants aux batailles
     */
    object BattleParticipants : IntIdTable("battle_participants") {
        val battleId = integer("battle_id").references(Battles.id)
        val playerId = uuid("player_id")
        val nationId = integer("nation_id")
        val side = enumerationByName("side", 10, WarSide::class)
        val joinedAt = timestamp("joined_at")
        val leftAt = timestamp("left_at").nullable()
        val kills = integer("kills").default(0)
        val deaths = integer("deaths").default(0)
        val damageDealt = double("damage_dealt").default(0.0)
        val damageTaken = double("damage_taken").default(0.0)
        val isAlive = bool("is_alive").default(true)

        init {
            uniqueIndex(battleId, playerId)
        }
    }

    /**
     * Table des sièges (extension de bataille)
     */
    object Sieges : IntIdTable("sieges") {
        val battleId = integer("battle_id").references(Battles.id).uniqueIndex()
        val fortificationLevel = integer("fortification_level").default(0)
        val wallsHealth = integer("walls_health").default(1000)
        val maxWallsHealth = integer("max_walls_health").default(1000)
        val gatesHealth = integer("gates_health").default(500)
        val maxGatesHealth = integer("max_gates_health").default(500)
        val siegeProgress = integer("siege_progress").default(0)
        val siegeEquipment = text("siege_equipment").default("[]")  // JSON array
    }

    /**
     * Table des événements de guerre
     */
    object WarEvents : IntIdTable("war_events") {
        val warId = integer("war_id").references(Wars.id)
        val type = enumerationByName("type", 30, WarEventType::class)
        val nationId = integer("nation_id").nullable()
        val description = text("description")
        val scoreChange = integer("score_change").default(0)
        val timestamp = timestamp("timestamp")

        init {
            index(false, warId)
            index(false, timestamp)
        }
    }

    /**
     * Table des créneaux horaires de bataille par nation
     */
    object BattleTimeSlots : IntIdTable("battle_time_slots") {
        val nationId = integer("nation_id")
        val dayOfWeek = integer("day_of_week")  // 1-7
        val startHour = integer("start_hour")   // 0-23
        val endHour = integer("end_hour")       // 0-23
        val enabled = bool("enabled").default(true)

        init {
            uniqueIndex(nationId, dayOfWeek, startHour)
        }
    }

    /**
     * Table des traités de paix
     */
    object PeaceTreaties : IntIdTable("peace_treaties") {
        val warId = integer("war_id").references(Wars.id)
        val proposedBy = integer("proposed_by")
        val proposedAt = timestamp("proposed_at")
        val status = varchar("status", 20).default("PENDING")  // PENDING, ACCEPTED, REJECTED, EXPIRED
        val expiresAt = timestamp("expires_at")
        val terms = text("terms")  // JSON des termes
        val respondedAt = timestamp("responded_at").nullable()

        init {
            index(false, warId)
        }
    }

    /**
     * Table des truces (périodes de paix forcée)
     */
    object Truces : IntIdTable("truces") {
        val nation1Id = integer("nation1_id")
        val nation2Id = integer("nation2_id")
        val startedAt = timestamp("started_at")
        val expiresAt = timestamp("expires_at")
        val reason = varchar("reason", 128)

        init {
            uniqueIndex(nation1Id, nation2Id)
        }
    }
}
