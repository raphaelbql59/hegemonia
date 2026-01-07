package com.hegemonia.core.utils

import com.hegemonia.core.HegemoniaCore
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.text.DecimalFormat
import java.time.Duration
import java.time.Instant
import java.util.*

// ========================================
// MESSAGE EXTENSIONS
// ========================================

/**
 * Envoie un message MiniMessage au joueur
 */
fun CommandSender.sendMini(message: String) {
    this.sendMessage(HegemoniaCore.get().parse(message))
}

/**
 * Envoie un message MiniMessage avec placeholders
 */
fun CommandSender.sendMini(message: String, vararg placeholders: Pair<String, String>) {
    this.sendMessage(HegemoniaCore.get().parse(message, *placeholders))
}

/**
 * Envoie un message de succès
 */
fun CommandSender.sendSuccess(message: String) {
    sendMini("<green>✓</green> <gray>$message</gray>")
}

/**
 * Envoie un message d'erreur
 */
fun CommandSender.sendError(message: String) {
    sendMini("<red>✗</red> <gray>$message</gray>")
}

/**
 * Envoie un message d'information
 */
fun CommandSender.sendInfo(message: String) {
    sendMini("<aqua>ℹ</aqua> <gray>$message</gray>")
}

/**
 * Envoie un message d'avertissement
 */
fun CommandSender.sendWarning(message: String) {
    sendMini("<yellow>⚠</yellow> <gray>$message</gray>")
}

// ========================================
// NUMBER FORMATTING
// ========================================

private val currencyFormat = DecimalFormat("#,##0.00")
private val compactFormat = DecimalFormat("#,##0.##")

/**
 * Formate un nombre comme monnaie
 */
fun Double.toCurrency(): String {
    return "${currencyFormat.format(this)} H$"
}

/**
 * Formate un nombre de façon compacte (1K, 1M, etc.)
 */
fun Long.toCompact(): String {
    return when {
        this >= 1_000_000_000 -> "${compactFormat.format(this / 1_000_000_000.0)}B"
        this >= 1_000_000 -> "${compactFormat.format(this / 1_000_000.0)}M"
        this >= 1_000 -> "${compactFormat.format(this / 1_000.0)}K"
        else -> this.toString()
    }
}

/**
 * Formate un nombre avec séparateurs
 */
fun Int.formatted(): String = String.format("%,d", this)
fun Long.formatted(): String = String.format("%,d", this)

// ========================================
// TIME FORMATTING
// ========================================

/**
 * Formate une durée de façon lisible
 */
fun Duration.toReadable(): String {
    val days = toDays()
    val hours = toHours() % 24
    val minutes = toMinutes() % 60
    val seconds = seconds % 60

    return buildString {
        if (days > 0) append("${days}j ")
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}m ")
        if (seconds > 0 || isEmpty()) append("${seconds}s")
    }.trim()
}

/**
 * Calcule le temps écoulé depuis un instant
 */
fun Instant.timeAgo(): String {
    val duration = Duration.between(this, Instant.now())
    return "il y a ${duration.toReadable()}"
}

// ========================================
// UUID EXTENSIONS
// ========================================

/**
 * Convertit une string en UUID de façon sûre
 */
fun String.toUUIDOrNull(): UUID? {
    return try {
        UUID.fromString(this)
    } catch (e: IllegalArgumentException) {
        null
    }
}

// ========================================
// COLLECTION EXTENSIONS
// ========================================

/**
 * Retourne une page d'éléments
 */
fun <T> List<T>.page(page: Int, pageSize: Int = 10): List<T> {
    val start = page * pageSize
    if (start >= size) return emptyList()
    return subList(start, minOf(start + pageSize, size))
}

/**
 * Calcule le nombre total de pages
 */
fun <T> List<T>.totalPages(pageSize: Int = 10): Int {
    return (size + pageSize - 1) / pageSize
}

// ========================================
// PLAYER EXTENSIONS
// ========================================

/**
 * Vérifie si le joueur est en ligne
 */
val UUID.isOnline: Boolean
    get() = org.bukkit.Bukkit.getPlayer(this) != null

/**
 * Récupère le joueur s'il est en ligne
 */
val UUID.player: Player?
    get() = org.bukkit.Bukkit.getPlayer(this)
