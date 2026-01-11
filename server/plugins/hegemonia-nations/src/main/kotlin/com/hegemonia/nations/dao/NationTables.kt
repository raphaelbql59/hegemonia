package com.hegemonia.nations.dao

import com.hegemonia.nations.model.ElectionStatus
import com.hegemonia.nations.model.GovernmentType
import com.hegemonia.nations.model.NationRole
import com.hegemonia.nations.model.RelationType
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Définitions des tables pour le système de nations
 */
object NationTables {

    /**
     * Table des nations
     */
    object Nations : IntIdTable("nations") {
        val uuid = uuid("uuid").uniqueIndex()
        val name = varchar("name", 32).uniqueIndex()
        val tag = varchar("tag", 4).uniqueIndex()
        val description = text("description").default("")
        val governmentType = enumerationByName("government_type", 20, GovernmentType::class)
            .default(GovernmentType.DEMOCRACY)
        val leaderId = uuid("leader_id")
        val capitalRegion = varchar("capital_region", 64).nullable()
        val createdAt = timestamp("created_at")
        val balance = double("balance").default(0.0)
        val power = integer("power").default(100)
        val color = varchar("color", 16).default("#FFFFFF")
        val banner = text("banner").nullable()
        val motto = varchar("motto", 128).nullable()
        val anthem = varchar("anthem", 256).nullable()
        val isOpen = bool("is_open").default(false)
        val taxRate = double("tax_rate").default(10.0)
        val warWeariness = integer("war_weariness").default(0)
        val stability = integer("stability").default(50)
        val reputation = integer("reputation").default(0)

        init {
            index(false, leaderId)
        }
    }

    /**
     * Table des membres de nation
     */
    object NationMembers : IntIdTable("nation_members") {
        val nationId = integer("nation_id").references(Nations.id)
        val playerId = uuid("player_id")
        val role = enumerationByName("role", 20, NationRole::class).default(NationRole.RECRUIT)
        val joinedAt = timestamp("joined_at")
        val contribution = double("contribution").default(0.0)  // Contribution totale au trésor
        val permissions = text("custom_permissions").nullable()  // JSON override

        init {
            uniqueIndex(nationId, playerId)
            index(false, playerId)
        }
    }

    /**
     * Table des rôles personnalisés
     */
    object NationRoles : IntIdTable("nation_roles") {
        val nationId = integer("nation_id").references(Nations.id)
        val name = varchar("name", 32)
        val displayName = varchar("display_name", 48)
        val priority = integer("priority")
        val permissions = text("permissions")  // JSON array
        val color = varchar("color", 16).default("<white>")
        val prefix = varchar("prefix", 16).nullable()

        init {
            uniqueIndex(nationId, name)
        }
    }

    /**
     * Table des relations entre nations
     */
    object NationRelations : IntIdTable("nation_relations") {
        val nationId = integer("nation_id").references(Nations.id)
        val targetNationId = integer("target_nation_id").references(Nations.id)
        val relationType = enumerationByName("relation_type", 20, RelationType::class)
            .default(RelationType.NEUTRAL)
        val since = timestamp("since")
        val expiresAt = timestamp("expires_at").nullable()
        val metadata = text("metadata").nullable()  // JSON data

        init {
            uniqueIndex(nationId, targetNationId)
        }
    }

    /**
     * Table des territoires/claims
     */
    object Territories : IntIdTable("territories") {
        val regionId = varchar("region_id", 64).uniqueIndex()  // ID de la région prédéfinie
        val nationId = integer("nation_id").references(Nations.id).nullable()
        val claimedAt = timestamp("claimed_at").nullable()
        val isCapital = bool("is_capital").default(false)
        val development = integer("development").default(0)  // Niveau de développement
        val fortification = integer("fortification").default(0)  // Niveau de fortification
        val population = integer("population").default(0)
        val resources = text("resources").nullable()  // JSON des ressources

        init {
            index(false, nationId)
        }
    }

    /**
     * Table des joueurs
     */
    object Players : IntIdTable("hegemonia_players") {
        val uuid = uuid("uuid").uniqueIndex()
        val username = varchar("username", 16)
        val nationId = integer("nation_id").references(Nations.id).nullable()
        val role = enumerationByName("role", 20, NationRole::class).nullable()
        val joinedNationAt = timestamp("joined_nation_at").nullable()
        val firstJoin = timestamp("first_join")
        val lastSeen = timestamp("last_seen")
        val playTime = long("play_time").default(0)
        val balance = double("balance").default(0.0)
        val reputation = integer("reputation").default(0)
        val kills = integer("kills").default(0)
        val deaths = integer("deaths").default(0)
        val blocksPlaced = long("blocks_placed").default(0)
        val blocksDestroyed = long("blocks_destroyed").default(0)
        val settings = text("settings").default("{}")  // JSON PlayerSettings

        init {
            index(false, nationId)
            index(false, username)
        }
    }

    /**
     * Table des invitations
     */
    object NationInvites : IntIdTable("nation_invites") {
        val playerId = uuid("player_id")
        val nationId = integer("nation_id").references(Nations.id)
        val invitedBy = uuid("invited_by")
        val invitedAt = timestamp("invited_at")
        val expiresAt = timestamp("expires_at")

        init {
            uniqueIndex(playerId, nationId)
        }
    }

    /**
     * Table des élections
     */
    object Elections : IntIdTable("nation_elections") {
        val nationId = integer("nation_id").references(Nations.id)
        val status = enumerationByName("status", 20, ElectionStatus::class)
            .default(ElectionStatus.REGISTRATION)
        val position = varchar("position", 32).default("LEADER")  // LEADER, MINISTER, etc.
        val startedAt = timestamp("started_at")
        val registrationEndsAt = timestamp("registration_ends_at")
        val votingEndsAt = timestamp("voting_ends_at")
        val winnerId = uuid("winner_id").nullable()
        val totalVotes = integer("total_votes").default(0)

        init {
            index(false, nationId)
            index(false, status)
        }
    }

    /**
     * Table des candidats aux élections
     */
    object ElectionCandidates : IntIdTable("election_candidates") {
        val electionId = integer("election_id").references(Elections.id)
        val playerId = uuid("player_id")
        val registeredAt = timestamp("registered_at")
        val slogan = varchar("slogan", 128).nullable()
        val voteCount = integer("vote_count").default(0)
        val withdrawn = bool("withdrawn").default(false)

        init {
            uniqueIndex(electionId, playerId)
        }
    }

    /**
     * Table des votes
     */
    object ElectionVotes : IntIdTable("election_votes") {
        val electionId = integer("election_id").references(Elections.id)
        val voterId = uuid("voter_id")
        val candidateId = uuid("candidate_id")
        val votedAt = timestamp("voted_at")

        init {
            uniqueIndex(electionId, voterId)
        }
    }

    /**
     * Table d'audit des actions
     */
    object NationAuditLog : IntIdTable("nation_audit_log") {
        val nationId = integer("nation_id").references(Nations.id)
        val actorId = uuid("actor_id")
        val action = varchar("action", 64)
        val target = varchar("target", 128).nullable()
        val details = text("details").nullable()
        val timestamp = timestamp("timestamp")
        val ipAddress = varchar("ip_address", 45).nullable()

        init {
            index(false, nationId)
            index(false, actorId)
            index(false, timestamp)
        }
    }
}
