package com.hegemonia.nations.service

import com.hegemonia.nations.HegemoniaNations
import com.hegemonia.nations.dao.NationTables
import com.hegemonia.nations.model.*
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Service de gestion des élections
 * Gère les élections pour les gouvernements démocratiques
 */
class ElectionService(private val plugin: HegemoniaNations) {

    private val activeElections = ConcurrentHashMap<Int, Election>() // nationId -> election

    companion object {
        // Configuration des durées
        val REGISTRATION_DURATION: Duration = Duration.ofDays(2)  // 2 jours pour s'inscrire
        val VOTING_DURATION: Duration = Duration.ofDays(3)        // 3 jours pour voter
        val TERM_DURATION: Duration = Duration.ofDays(30)         // Mandat de 30 jours

        const val MIN_CANDIDATES = 2  // Minimum de candidats pour une élection valide
        const val MIN_RANK_TO_CANDIDATE = 20  // NationRole.CITIZEN.priority
    }

    /**
     * Initialise le service et vérifie les élections en cours
     */
    fun initialize() {
        // Charger les élections actives
        transaction {
            NationTables.Elections
                .select {
                    (NationTables.Elections.status eq ElectionStatus.REGISTRATION) or
                    (NationTables.Elections.status eq ElectionStatus.VOTING)
                }
                .forEach { row ->
                    val election = row.toElection()
                    activeElections[election.nationId] = election
                }
        }

        plugin.logger.info("ElectionService initialisé: ${activeElections.size} élections actives")

        // Planifier la vérification des élections
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable { checkElections() }, 20L * 60, 20L * 60) // Toutes les minutes
    }

    /**
     * Vérifie et met à jour le statut des élections
     */
    private fun checkElections() {
        val now = Instant.now()

        activeElections.values.toList().forEach { election ->
            when (election.status) {
                ElectionStatus.REGISTRATION -> {
                    if (now.isAfter(election.registrationEndsAt)) {
                        // Passer à la phase de vote
                        startVotingPhase(election.nationId)
                    }
                }
                ElectionStatus.VOTING -> {
                    if (now.isAfter(election.votingEndsAt)) {
                        // Terminer l'élection et compter les votes
                        concludeElection(election.nationId)
                    }
                }
                else -> {}
            }
        }
    }

    // ========================================================================
    // Gestion des élections
    // ========================================================================

    /**
     * Démarre une nouvelle élection
     */
    fun startElection(nationId: Int, position: String = "LEADER"): Boolean {
        val nation = plugin.nationService.getNation(nationId) ?: return false

        // Vérifier si la nation supporte les élections
        if (!nation.governmentType.features.contains(GovernmentFeature.ELECTIONS) &&
            !nation.governmentType.features.contains(GovernmentFeature.DIRECT_DEMOCRACY)) {
            return false
        }

        // Vérifier s'il n'y a pas déjà une élection en cours
        if (hasActiveElection(nationId)) {
            return false
        }

        val now = Instant.now()
        val registrationEnd = now.plus(REGISTRATION_DURATION)
        val votingEnd = registrationEnd.plus(VOTING_DURATION)

        return transaction {
            val electionId = NationTables.Elections.insertAndGetId {
                it[NationTables.Elections.nationId] = nationId
                it[status] = ElectionStatus.REGISTRATION
                it[NationTables.Elections.position] = position
                it[startedAt] = now
                it[registrationEndsAt] = registrationEnd
                it[votingEndsAt] = votingEnd
                it[winnerId] = null
                it[totalVotes] = 0
            }

            val election = Election(
                id = electionId.value,
                nationId = nationId,
                status = ElectionStatus.REGISTRATION,
                position = position,
                startedAt = now,
                registrationEndsAt = registrationEnd,
                votingEndsAt = votingEnd,
                winnerId = null,
                totalVotes = 0
            )

            activeElections[nationId] = election
            true
        }
    }

    /**
     * Passe à la phase de vote
     */
    private fun startVotingPhase(nationId: Int) {
        val election = activeElections[nationId] ?: return
        val candidates = getCandidates(election.id)

        // Vérifier qu'il y a assez de candidats
        if (candidates.size < MIN_CANDIDATES) {
            // Annuler l'élection
            cancelElection(nationId, "Pas assez de candidats")
            return
        }

        transaction {
            NationTables.Elections.update({ NationTables.Elections.id eq election.id }) {
                it[status] = ElectionStatus.VOTING
            }
        }

        activeElections[nationId] = election.copy(status = ElectionStatus.VOTING)

        // Notifier les membres
        announceToNation(nationId,
            "<gold>⚖ ÉLECTION</gold> La période de vote a commencé! " +
            "Utilisez <yellow>/nation vote <candidat></yellow> pour voter."
        )
    }

    /**
     * Conclut une élection et détermine le gagnant
     */
    private fun concludeElection(nationId: Int) {
        val election = activeElections[nationId] ?: return
        val candidates = getCandidates(election.id).filter { !it.withdrawn }

        if (candidates.isEmpty()) {
            cancelElection(nationId, "Aucun candidat valide")
            return
        }

        val totalVotes = candidates.sumOf { it.voteCount }

        // Si aucun vote, annuler l'élection
        if (totalVotes == 0) {
            cancelElection(nationId, "Aucun vote enregistré")
            return
        }

        // Trouver le maximum de votes
        val maxVotes = candidates.maxOf { it.voteCount }
        val topCandidates = candidates.filter { it.voteCount == maxVotes }

        // En cas d'égalité, le candidat inscrit en premier gagne (ancienneté)
        val winner = topCandidates.minByOrNull { it.registeredAt }!!

        transaction {
            NationTables.Elections.update({ NationTables.Elections.id eq election.id }) {
                it[status] = ElectionStatus.COMPLETED
                it[winnerId] = winner.playerId
                it[NationTables.Elections.totalVotes] = totalVotes
            }
        }

        activeElections.remove(nationId)

        // Transférer le leadership si c'est une élection de leader
        if (election.position == "LEADER") {
            transferLeadership(nationId, winner.playerId)
        }

        val winnerName = Bukkit.getOfflinePlayer(winner.playerId).name ?: "Inconnu"
        val tieMessage = if (topCandidates.size > 1) " (égalité départagée par ancienneté)" else ""
        announceToNation(nationId,
            "<gold>⚖ ÉLECTION TERMINÉE</gold> <yellow>$winnerName</yellow> a remporté " +
            "l'élection avec <white>${winner.voteCount}</white> votes sur <white>$totalVotes</white>!$tieMessage"
        )
    }

    /**
     * Annule une élection
     */
    fun cancelElection(nationId: Int, reason: String = "") {
        val election = activeElections[nationId] ?: return

        transaction {
            NationTables.Elections.update({ NationTables.Elections.id eq election.id }) {
                it[status] = ElectionStatus.CANCELLED
            }
        }

        activeElections.remove(nationId)

        announceToNation(nationId,
            "<red>⚖ ÉLECTION ANNULÉE</red>${if (reason.isNotEmpty()) " - $reason" else ""}"
        )
    }

    /**
     * Transfère le leadership à un nouveau joueur
     */
    private fun transferLeadership(nationId: Int, newLeaderId: UUID) {
        val nation = plugin.nationService.getNation(nationId) ?: return

        // Rétrograder l'ancien leader
        plugin.playerService.setRole(nation.leaderId, NationRole.MINISTER)

        // Promouvoir le nouveau leader
        plugin.playerService.setRole(newLeaderId, NationRole.LEADER)

        // Mettre à jour la nation
        plugin.nationService.updateNation(nation.copy(leaderId = newLeaderId))
    }

    // ========================================================================
    // Gestion des candidats
    // ========================================================================

    /**
     * Inscrit un candidat à une élection
     */
    fun registerCandidate(nationId: Int, playerId: UUID, slogan: String? = null): ElectionActionResult {
        val election = activeElections[nationId] ?: return ElectionActionResult.NO_ACTIVE_ELECTION

        if (election.status != ElectionStatus.REGISTRATION) {
            return ElectionActionResult.ELECTION_NOT_IN_REGISTRATION
        }

        // Vérifier que le joueur est membre de la nation
        val player = plugin.playerService.getPlayer(playerId)
        if (player?.nationId != nationId) {
            return ElectionActionResult.NOT_A_MEMBER
        }

        // Vérifier le rang minimum
        if ((player.role?.priority ?: 0) < MIN_RANK_TO_CANDIDATE) {
            return ElectionActionResult.INSUFFICIENT_RANK
        }

        // Vérifier s'il n'est pas déjà candidat
        if (isCandidate(election.id, playerId)) {
            return ElectionActionResult.ALREADY_CANDIDATE
        }

        transaction {
            NationTables.ElectionCandidates.insert {
                it[electionId] = election.id
                it[NationTables.ElectionCandidates.playerId] = playerId
                it[registeredAt] = Instant.now()
                it[NationTables.ElectionCandidates.slogan] = slogan
                it[voteCount] = 0
                it[withdrawn] = false
            }
        }

        val playerName = Bukkit.getOfflinePlayer(playerId).name ?: "Inconnu"
        announceToNation(nationId,
            "<gold>⚖ CANDIDATURE</gold> <yellow>$playerName</yellow> est candidat(e) aux élections!" +
            (slogan?.let { " - <italic>\"$it\"</italic>" } ?: "")
        )

        return ElectionActionResult.SUCCESS
    }

    /**
     * Retire la candidature d'un joueur
     */
    fun withdrawCandidate(nationId: Int, playerId: UUID): ElectionActionResult {
        val election = activeElections[nationId] ?: return ElectionActionResult.NO_ACTIVE_ELECTION

        if (!isCandidate(election.id, playerId)) {
            return ElectionActionResult.NOT_A_CANDIDATE
        }

        transaction {
            NationTables.ElectionCandidates.update({
                (NationTables.ElectionCandidates.electionId eq election.id) and
                (NationTables.ElectionCandidates.playerId eq playerId)
            }) {
                it[withdrawn] = true
            }
        }

        return ElectionActionResult.SUCCESS
    }

    /**
     * Récupère les candidats d'une élection
     */
    fun getCandidates(electionId: Int): List<ElectionCandidate> {
        return transaction {
            NationTables.ElectionCandidates
                .select { NationTables.ElectionCandidates.electionId eq electionId }
                .map { it.toCandidate() }
        }
    }

    /**
     * Vérifie si un joueur est candidat
     */
    fun isCandidate(electionId: Int, playerId: UUID): Boolean {
        return transaction {
            NationTables.ElectionCandidates
                .select {
                    (NationTables.ElectionCandidates.electionId eq electionId) and
                    (NationTables.ElectionCandidates.playerId eq playerId) and
                    (NationTables.ElectionCandidates.withdrawn eq false)
                }
                .count() > 0
        }
    }

    // ========================================================================
    // Gestion des votes
    // ========================================================================

    /**
     * Enregistre un vote
     */
    fun vote(nationId: Int, voterId: UUID, candidateId: UUID): ElectionActionResult {
        val election = activeElections[nationId] ?: return ElectionActionResult.NO_ACTIVE_ELECTION

        if (election.status != ElectionStatus.VOTING) {
            return ElectionActionResult.ELECTION_NOT_IN_VOTING
        }

        // Vérifier que le votant est membre de la nation
        val voter = plugin.playerService.getPlayer(voterId)
        if (voter?.nationId != nationId) {
            return ElectionActionResult.NOT_A_MEMBER
        }

        // Vérifier que le candidat existe
        if (!isCandidate(election.id, candidateId)) {
            return ElectionActionResult.CANDIDATE_NOT_FOUND
        }

        // Vérifier qu'il n'a pas déjà voté
        if (hasVoted(election.id, voterId)) {
            return ElectionActionResult.ALREADY_VOTED
        }

        transaction {
            // Enregistrer le vote
            NationTables.ElectionVotes.insert {
                it[electionId] = election.id
                it[NationTables.ElectionVotes.voterId] = voterId
                it[NationTables.ElectionVotes.candidateId] = candidateId
                it[votedAt] = Instant.now()
            }

            // Incrémenter le compteur du candidat
            NationTables.ElectionCandidates.update({
                (NationTables.ElectionCandidates.electionId eq election.id) and
                (NationTables.ElectionCandidates.playerId eq candidateId)
            }) {
                it[voteCount] = voteCount + 1
            }
        }

        return ElectionActionResult.SUCCESS
    }

    /**
     * Vérifie si un joueur a déjà voté
     */
    fun hasVoted(electionId: Int, voterId: UUID): Boolean {
        return transaction {
            NationTables.ElectionVotes
                .select {
                    (NationTables.ElectionVotes.electionId eq electionId) and
                    (NationTables.ElectionVotes.voterId eq voterId)
                }
                .count() > 0
        }
    }

    // ========================================================================
    // Queries
    // ========================================================================

    /**
     * Vérifie s'il y a une élection active
     */
    fun hasActiveElection(nationId: Int): Boolean = activeElections.containsKey(nationId)

    /**
     * Récupère l'élection active d'une nation
     */
    fun getActiveElection(nationId: Int): Election? = activeElections[nationId]

    /**
     * Récupère l'historique des élections d'une nation
     */
    fun getElectionHistory(nationId: Int, limit: Int = 10): List<Election> {
        return transaction {
            NationTables.Elections
                .select { NationTables.Elections.nationId eq nationId }
                .orderBy(NationTables.Elections.startedAt, SortOrder.DESC)
                .limit(limit)
                .map { it.toElection() }
        }
    }

    /**
     * Annonce un message à tous les membres d'une nation
     */
    private fun announceToNation(nationId: Int, message: String) {
        val core = com.hegemonia.core.HegemoniaCore.get()
        // Parser le message une seule fois pour l'efficacité
        val parsedMessage = core.parse(message)
        plugin.nationService.getMembers(nationId).forEach { (uuid, _) ->
            Bukkit.getPlayer(uuid)?.sendMessage(parsedMessage)
        }
    }

    // ========================================================================
    // Extensions
    // ========================================================================

    private fun ResultRow.toElection(): Election {
        return Election(
            id = this[NationTables.Elections.id].value,
            nationId = this[NationTables.Elections.nationId],
            status = this[NationTables.Elections.status],
            position = this[NationTables.Elections.position],
            startedAt = this[NationTables.Elections.startedAt],
            registrationEndsAt = this[NationTables.Elections.registrationEndsAt],
            votingEndsAt = this[NationTables.Elections.votingEndsAt],
            winnerId = this[NationTables.Elections.winnerId],
            totalVotes = this[NationTables.Elections.totalVotes]
        )
    }

    private fun ResultRow.toCandidate(): ElectionCandidate {
        return ElectionCandidate(
            id = this[NationTables.ElectionCandidates.id].value,
            electionId = this[NationTables.ElectionCandidates.electionId],
            playerId = this[NationTables.ElectionCandidates.playerId],
            registeredAt = this[NationTables.ElectionCandidates.registeredAt],
            slogan = this[NationTables.ElectionCandidates.slogan],
            voteCount = this[NationTables.ElectionCandidates.voteCount],
            withdrawn = this[NationTables.ElectionCandidates.withdrawn]
        )
    }
}
