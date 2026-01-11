package com.hegemonia.nations.model

import java.time.Instant
import java.util.UUID

/**
 * Représente une élection dans une nation
 */
data class Election(
    val id: Int,
    val nationId: Int,
    val status: ElectionStatus,
    val position: String,  // LEADER, MINISTER, etc.
    val startedAt: Instant,
    val registrationEndsAt: Instant,
    val votingEndsAt: Instant,
    val winnerId: UUID?,
    val totalVotes: Int
)

/**
 * Représente un candidat à une élection
 */
data class ElectionCandidate(
    val id: Int,
    val electionId: Int,
    val playerId: UUID,
    val registeredAt: Instant,
    val slogan: String?,
    val voteCount: Int,
    val withdrawn: Boolean
)

/**
 * Statut d'une élection
 */
enum class ElectionStatus(val displayName: String) {
    REGISTRATION("Inscriptions"),      // Phase d'inscription des candidats
    VOTING("Vote en cours"),           // Phase de vote
    COUNTING("Dépouillement"),         // Comptage des votes
    COMPLETED("Terminée"),             // Élection terminée
    CANCELLED("Annulée")               // Élection annulée
}

/**
 * Résultat d'une action électorale
 */
enum class ElectionActionResult(val message: String) {
    SUCCESS("Opération réussie"),
    NOT_DEMOCRACY("Votre nation n'a pas de système électoral"),
    NO_ACTIVE_ELECTION("Aucune élection en cours"),
    ELECTION_NOT_IN_REGISTRATION("La période d'inscription est terminée"),
    ELECTION_NOT_IN_VOTING("La période de vote n'est pas active"),
    ALREADY_CANDIDATE("Vous êtes déjà candidat"),
    NOT_A_CANDIDATE("Vous n'êtes pas candidat"),
    ALREADY_VOTED("Vous avez déjà voté"),
    CANDIDATE_NOT_FOUND("Candidat non trouvé"),
    NOT_A_MEMBER("Vous n'êtes pas membre de cette nation"),
    INSUFFICIENT_RANK("Votre rang est insuffisant pour vous présenter"),
    CANNOT_VOTE_SELF("Vous ne pouvez pas voter pour vous-même")
}
