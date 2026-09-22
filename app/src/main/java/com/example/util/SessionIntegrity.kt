package com.example.util

import com.example.data.model.CashReplenishment
import com.example.data.model.CashSession
import com.example.data.model.Disbursement
import java.security.MessageDigest

/**
 * Calcul et vérification de l'empreinte d'intégrité (SHA-256) d'une session de caisse.
 */
object SessionIntegrity {

    fun computeHash(
        session: CashSession,
        activeReplenishments: List<CashReplenishment>,
        activeDisbursements: List<Disbursement>
    ): String {
        val sortedRepl = activeReplenishments.sortedBy { it.orderNumber }
        val sortedDisb = activeDisbursements.sortedBy { it.orderNumber }

        val canonical = buildString {
            append("SESSION:")
            append(session.id).append("|")
            append(session.reference).append("|")
            append(session.dateMillis).append("|")
            append(session.initialFund).append("|")
            append(session.countedCash ?: "NULL").append("|")
            append(session.closingTime ?: "").append("|")
            append(session.currency).append("|")
            append("REPLENISHMENTS:")
            for (r in sortedRepl) {
                append("[")
                append(r.orderNumber).append(",")
                append(r.amount).append(",")
                append(r.time).append(",")
                append(r.reason.trim()).append(",")
                append(r.sourceLocation.trim())
                append("];")
            }
            append("|DISBURSEMENTS:")
            for (d in sortedDisb) {
                append("[")
                append(d.orderNumber).append(",")
                append(d.amount).append(",")
                append(d.time).append(",")
                append(d.designation.trim()).append(",")
                append(d.parentCategory.trim()).append(",")
                append(d.subCategory.trim())
                append("];")
            }
        }

        return sha256(canonical)
    }

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
