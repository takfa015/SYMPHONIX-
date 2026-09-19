package com.example.data.repository

import com.example.data.db.CashDao
import com.example.data.model.CashReplenishment
import com.example.data.model.CashSession
import com.example.data.model.Disbursement
import com.example.data.model.SessionWithDetails
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CashRepository(private val dao: CashDao) {

    val cashDao: CashDao get() = dao
    val allSessions: Flow<List<SessionWithDetails>> = dao.getAllSessionsWithDetails()

    suspend fun getAllSessionsDirect(): List<SessionWithDetails> = dao.getAllSessionsWithDetailsDirect()

    fun getSessionFlow(sessionId: Long): Flow<SessionWithDetails?> = dao.getSessionWithDetailsFlow(sessionId)

    suspend fun getSession(sessionId: Long): SessionWithDetails? = dao.getSessionWithDetails(sessionId)

    suspend fun ensureInitialData() {
        val latest = dao.getLatestSession()
        if (latest == null) {
            // Seed with sample matching user's document
            val sessionId = dao.insertSession(
                CashSession(
                    reference = "REC-20260918-01",
                    dateText = "18 Septembre 2026",
                    dateMillis = System.currentTimeMillis(),
                    establishmentName = "SYMPHONIX",
                    establishmentSubTitle = "Finance & Gestion de Caisse",
                    responsibleName = "Responsable de Caisse",
                    managerName = "Direction Générale",
                    initialFund = 150000.0,
                    initialFundTime = "19h35",
                    initialFundSource = "Dotation de caisse",
                    countedCash = 38200.0,
                    closingTime = "20h34",
                    isClosed = true,
                    currency = "DA",
                    notes = "Session témoin conforme au récapitulatif comptable."
                )
            )

            // Insert initial 3 sample disbursements matching user document
            dao.insertDisbursement(
                Disbursement(
                    sessionId = sessionId,
                    orderNumber = 1,
                    time = "19:36",
                    designation = "Œufs",
                    parentCategory = "Matières premières",
                    subCategory = "Œufs",
                    amount = 57000.0
                )
            )

            dao.insertDisbursement(
                Disbursement(
                    sessionId = sessionId,
                    orderNumber = 2,
                    time = "19:36",
                    designation = "Bassem (Commis salle pâtisserie)",
                    parentCategory = "Personnel",
                    subCategory = "Rémunération",
                    amount = 12000.0,
                    recipient = "Bassem"
                )
            )

            dao.insertDisbursement(
                Disbursement(
                    sessionId = sessionId,
                    orderNumber = 3,
                    time = "19:46",
                    designation = "Poulet",
                    parentCategory = "Matières premières",
                    subCategory = "Volailles",
                    amount = 42800.0
                )
            )
        }
    }

    suspend fun createNewSession(
        initialFund: Double,
        initialFundTime: String,
        initialFundSource: String,
        establishmentName: String,
        responsibleName: String,
        currency: String = "DA"
    ): Long {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
        val refDateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        val now = Date()
        val dateText = dateFormat.format(now)
        val dateRef = refDateFormat.format(now)
        val randomSuffix = (10..99).random()
        val reference = "REC-$dateRef-$randomSuffix"

        return dao.insertSession(
            CashSession(
                reference = reference,
                dateText = dateText,
                dateMillis = System.currentTimeMillis(),
                establishmentName = establishmentName,
                responsibleName = responsibleName,
                initialFund = initialFund,
                initialFundTime = initialFundTime,
                initialFundSource = initialFundSource,
                currency = currency
            )
        )
    }

    suspend fun addReplenishment(
        sessionId: Long,
        amount: Double,
        time: String,
        reason: String,
        sourceLocation: String
    ): Long {
        val count = dao.getReplenishmentCount(sessionId)
        return dao.insertReplenishment(
            CashReplenishment(
                sessionId = sessionId,
                orderNumber = count + 1,
                time = time,
                amount = amount,
                reason = reason,
                sourceLocation = sourceLocation
            )
        )
    }

    suspend fun deleteReplenishment(id: Long) = dao.deleteReplenishment(id)

    suspend fun addDisbursement(
        sessionId: Long,
        amount: Double,
        time: String,
        designation: String,
        parentCategory: String,
        subCategory: String,
        recipient: String = ""
    ): Long {
        val count = dao.getDisbursementCount(sessionId)
        return dao.insertDisbursement(
            Disbursement(
                sessionId = sessionId,
                orderNumber = count + 1,
                time = time,
                designation = designation,
                parentCategory = parentCategory,
                subCategory = subCategory,
                amount = amount,
                recipient = recipient
            )
        )
    }

    suspend fun deleteDisbursement(id: Long) = dao.deleteDisbursement(id)

    suspend fun closeSession(
        sessionId: Long,
        countedCash: Double,
        closingTime: String
    ) {
        val session = dao.getSessionWithDetails(sessionId)?.session ?: return
        dao.updateSession(
            session.copy(
                countedCash = countedCash,
                closingTime = closingTime,
                isClosed = true
            )
        )
    }

    suspend fun reopenSession(sessionId: Long) {
        val session = dao.getSessionWithDetails(sessionId)?.session ?: return
        dao.updateSession(
            session.copy(
                isClosed = false
            )
        )
    }

    suspend fun updateSessionSettings(
        sessionId: Long,
        establishmentName: String,
        establishmentSubTitle: String,
        responsibleName: String,
        managerName: String,
        currency: String,
        initialFund: Double
    ) {
        val session = dao.getSessionWithDetails(sessionId)?.session ?: return
        dao.updateSession(
            session.copy(
                establishmentName = establishmentName,
                establishmentSubTitle = establishmentSubTitle,
                responsibleName = responsibleName,
                managerName = managerName,
                currency = currency,
                initialFund = initialFund
            )
        )
    }

    suspend fun deleteSession(sessionId: Long) = dao.deleteSession(sessionId)
}
