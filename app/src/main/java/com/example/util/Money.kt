package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Utilitaire monétaire manipulant les montants strictement en centimes (Long)
 * pour éviter toute approximation de calcul à virgule flottante.
 */
object Money {

    private val symbols = DecimalFormatSymbols(Locale.FRENCH).apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }

    private val formatterWithDecimals = DecimalFormat("#,##0.00", symbols)
    private val formatterNoDecimals = DecimalFormat("#,##0", symbols)

    /**
     * Convertit une valeur Double (ex: issue d'une sauvegarde v1) en centimes Long.
     */
    fun doubleToCents(value: Double): Long {
        if (value.isNaN() || value.isInfinite()) return 0L
        return (value * 100.0).roundToLong()
    }

    /**
     * Convertit des centimes en Double (pour l'affichage ou rétrocompatibilité).
     */
    fun centsToDouble(cents: Long): Double {
        return cents.toDouble() / 100.0
    }

    /**
     * Analyse une chaîne de saisie utilisateur (tolérant virgule, point, espaces, devises)
     * et la convertit en centimes (Long).
     * Exemples:
     * - "150 000" -> 15000000L
     * - "150000,50" -> 15000050L
     * - "150000.5" -> 15000050L
     * - "12.345" -> 1235L (arrondi au centime)
     */
    fun parseToCents(input: String?): Long {
        if (input.isNullOrBlank()) return 0L

        // Nettoyage: suppression de tout espace insécable ou standard
        var clean = input.trim()
            .replace("\u00A0", "")
            .replace("\u202F", "")
            .replace(" ", "")

        val isNegative = clean.startsWith("-")
        if (isNegative) {
            clean = clean.substring(1)
        }

        // Si la chaîne contient plusieurs séparateurs, normaliser
        // Remplacer la virgule par un point
        clean = clean.replace(',', '.')

        // Ne garder que chiffres et au plus un point
        val parts = clean.split('.')
        if (parts.isEmpty()) return 0L

        val integerPartStr = parts[0].filter { it.isDigit() }
        val integerPart = integerPartStr.toLongOrNull() ?: 0L

        val centsPart = if (parts.size > 1) {
            val decimals = parts[1].filter { it.isDigit() }
            when {
                decimals.isEmpty() -> 0L
                decimals.length == 1 -> (decimals.take(1).toLong() * 10L)
                decimals.length == 2 -> decimals.take(2).toLong()
                else -> {
                    // Arrondi si plus de 2 décimales
                    val firstTwo = decimals.take(2).toLong()
                    val third = decimals[2].digitToIntOrNull() ?: 0
                    if (third >= 5) firstTwo + 1 else firstTwo
                }
            }
        } else {
            0L
        }

        val totalCents = (integerPart * 100L) + centsPart
        return if (isNegative) -totalCents else totalCents
    }

    /**
     * Formate un montant en centimes avec séparateurs de milliers et devise.
     * Exemple: format(15000000L, "DA") -> "150 000,00 DA"
     */
    fun format(cents: Long, currency: String = "DA", forceDecimals: Boolean = true): String {
        val absCents = abs(cents)
        val isNegative = cents < 0
        val prefix = if (isNegative) "- " else ""

        val formattedNumber = if (forceDecimals || (absCents % 100L != 0L)) {
            val dbl = absCents.toDouble() / 100.0
            formatterWithDecimals.format(dbl)
        } else {
            val dbl = (absCents / 100L).toDouble()
            formatterNoDecimals.format(dbl)
        }

        return if (currency.isNotBlank()) {
            "$prefix$formattedNumber $currency"
        } else {
            "$prefix$formattedNumber"
        }
    }

    /**
     * Formate pour affichage compact (sans décimales si rondes).
     */
    fun formatCompact(cents: Long, currency: String = "DA"): String {
        return format(cents, currency, forceDecimals = false)
    }

    /**
     * Formate pour la saisie dans un champ texte (ex: "150000" ou "150000,50").
     */
    fun formatForInput(cents: Long): String {
        if (cents == 0L) return ""
        val absCents = abs(cents)
        val isNegative = cents < 0
        val prefix = if (isNegative) "-" else ""
        return if (absCents % 100L != 0L) {
            val intPart = absCents / 100L
            val centPart = (absCents % 100L).toString().padStart(2, '0')
            "$prefix$intPart,$centPart"
        } else {
            val intPart = absCents / 100L
            "$prefix$intPart"
        }
    }
}
