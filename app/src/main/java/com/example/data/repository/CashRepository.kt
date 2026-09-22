package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.db.AppDatabase
import com.example.data.db.CashDao
import com.example.data.model.AuditEntry
import com.example.data.model.CashReplenishment
import com.example.data.model.CashSession
import com.example.data.model.Disbursement
import com.example.data.model.SessionWithDetails
import com.example.util.SessionIntegrity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CashRepository(private val db: AppDatabase) {

    val cashDao: CashDao get() = db.cashDao()
    val allSessions: Flow<List<SessionWithDetails>> = cashDao.getAllSessionsWithDetails()

    suspend fun getAllSessionsDirect(): List<SessionWithDetails> = cashDao.getAllSessionsWithDetailsDirect()

    fun getSessionFlow(sessionId: Long): Flow<SessionWithDetails?> = cashDao.getSessionWithDetailsFlow(sessionId)

    suspend fun getSession(sessionId: Long): SessionWithDetails? = cashDao.getSessionWithDetails(sessionId)

    suspend fun hasAnySessions(): Boolean {
        return cashDao.getLatestSession() != null
    }

    /**
     * Crée une nouvelle session de caisse avec audit log en transaction.
     */
    suspend fun createNewSession(
        initialFundCents: Long,
        initialFundTime: String,
        initialFundSource: String,
        establishmentName: String,
        establishmentSubTitle: String = "",
        responsibleName: String,
        managerName: String = "",
        currency: String = "DA"
    ): Long = db.withTransaction {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
        val refDateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        val now = Date()
        val dateText = dateFormat.format(now)
        val dateRef = refDateFormat.format(now)
        val randomSuffix = (10..99).random()
        val reference = "REC-$dateRef-$randomSuffix"

        val session = CashSession(
            reference = reference,
            dateText = dateText,
            dateMillis = System.currentTimeMillis(),
            establishmentName = establishmentName.trim(),
            establishmentSubTitle = establishmentSubTitle.trim(),
            responsibleName = responsibleName.trim(),
            managerName = managerName.trim(),
            initialFund = initialFundCents,
            initialFundTime = initialFundTime.ifBlank {
                SimpleDateFormat("HH'h'mm", Locale.FRENCH).format(now)
            },
            initialFundSource = initialFundSource.ifBlank { "Dotation de caisse" },
            currency = currency.trim()
        )

        val sessionId = cashDao.insertSession(session)

        cashDao.insertAuditEntry(
            AuditEntry(
                sessionId = sessionId,
                action = "SESSION_CREATED",
                entityType = "SESSION",
                entityId = sessionId,
                detailsJson = """{"reference":"$reference","initialFund":$initialFundCents,"establishment":"$establishmentName"}""",
                reason = "Ouverture de session de caisse"
            )
        )

        sessionId
    }

    /**
     * Ajout d'une alimentation (reassort / apport)
     */
    suspend fun addReplenishment(
        sessionId: Long,
        amountCents: Long,
        time: String,
        reason: String,
        sourceLocation: String,
        registeredBy: String = ""
    ): Long = db.withTransaction {
        val sessionWithDetails = cashDao.getSessionWithDetails(sessionId)
            ?: throw IllegalStateException("Session introuvable")
        if (sessionWithDetails.session.isClosed) {
            throw IllegalStateException("Impossible d'ajouter une ligne sur une session clôturée.")
        }

        val nextOrderNumber = cashDao.getNextReplenishmentOrderNumber(sessionId)
        val replenishment = CashReplenishment(
            sessionId = sessionId,
            orderNumber = nextOrderNumber,
            time = time,
            amount = amountCents,
            reason = reason.trim(),
            sourceLocation = sourceLocation.trim(),
            registeredBy = registeredBy.trim()
        )
        val id = cashDao.insertReplenishment(replenishment)

        cashDao.insertAuditEntry(
            AuditEntry(
                sessionId = sessionId,
                action = "LINE_ADDED",
                entityType = "REPLENISHMENT",
                entityId = id,
                detailsJson = """{"orderNumber":$nextOrderNumber,"amount":$amountCents,"reason":"$reason"}""",
                reason = "Ajout alimentation"
            )
        )

        id
    }

    /**
     * Annulation douce d'une alimentation
     */
    suspend fun cancelReplenishment(
        sessionId: Long,
        id: Long,
        reason: String
    ) = db.withTransaction {
        val sessionWithDetails = cashDao.getSessionWithDetails(sessionId)
            ?: throw IllegalStateException("Session introuvable")
        if (sessionWithDetails.session.isClosed) {
            throw IllegalStateException("Impossible d'annuler une ligne sur une session clôturée.")
        }

        val item = cashDao.getReplenishmentById(id)
            ?: throw IllegalStateException("Alimentation introuvable")

        val now = System.currentTimeMillis()
        cashDao.cancelReplenishment(id, now, reason.trim())

        cashDao.insertAuditEntry(
            AuditEntry(
                sessionId = sessionId,
                timestamp = now,
                action = "LINE_CANCELLED",
                entityType = "REPLENISHMENT",
                entityId = id,
                detailsJson = """{"orderNumber":${item.orderNumber},"amount":${item.amount},"originalReason":"${item.reason}"}""",
                reason = reason.ifBlank { "Annulation par l'utilisateur" }
            )
        )
    }

    /**
     * Ajout d'un décaissement (dépense)
     */
    suspend fun addDisbursement(
        sessionId: Long,
        amountCents: Long,
        time: String,
        designation: String,
        parentCategory: String,
        subCategory: String,
        recipient: String = ""
    ): Long = db.withTransaction {
        val sessionWithDetails = cashDao.getSessionWithDetails(sessionId)
            ?: throw IllegalStateException("Session introuvable")
        if (sessionWithDetails.session.isClosed) {
            throw IllegalStateException("Impossible d'ajouter une ligne sur une session clôturée.")
        }

        val nextOrderNumber = cashDao.getNextDisbursementOrderNumber(sessionId)
        val disbursement = Disbursement(
            sessionId = sessionId,
            orderNumber = nextOrderNumber,
            time = time,
            designation = designation.trim(),
            parentCategory = parentCategory.trim(),
            subCategory = subCategory.trim(),
            amount = amountCents,
            recipient = recipient.trim()
        )
        val id = cashDao.insertDisbursement(disbursement)

        cashDao.insertAuditEntry(
            AuditEntry(
                sessionId = sessionId,
                action = "LINE_ADDED",
                entityType = "DISBURSEMENT",
                entityId = id,
                detailsJson = """{"orderNumber":$nextOrderNumber,"amount":$amountCents,"designation":"$designation","category":"$parentCategory"}""",
                reason = "Ajout décaissement"
            )
        )

        id
    }

    /**
     * Annulation douce d'un décaissement
     */
    suspend fun cancelDisbursement(
        sessionId: Long,
        id: Long,
        reason: String
    ) = db.withTransaction {
        val sessionWithDetails = cashDao.getSessionWithDetails(sessionId)
            ?: throw IllegalStateException("Session introuvable")
        if (sessionWithDetails.session.isClosed) {
            throw IllegalStateException("Impossible d'annuler une ligne sur une session clôturée.")
        }

        val item = cashDao.getDisbursementById(id)
            ?: throw IllegalStateException("Décaissement introuvable")

        val now = System.currentTimeMillis()
        cashDao.cancelDisbursement(id, now, reason.trim())

        cashDao.insertAuditEntry(
            AuditEntry(
                sessionId = sessionId,
                timestamp = now,
                action = "LINE_CANCELLED",
                entityType = "DISBURSEMENT",
                entityId = id,
                detailsJson = """{"orderNumber":${item.orderNumber},"amount":${item.amount},"designation":"${item.designation}"}""",
                reason = reason.ifBlank { "Annulation par l'utilisateur" }
            )
        )
    }

    /**
     * Clôture de la session avec calcul d'empreinte d'intégrité SHA-256
     */
    suspend fun closeSession(
        sessionId: Long,
        countedCashCents: Long,
        closingTime: String
    ) = db.withTransaction {
        val details = cashDao.getSessionWithDetails(sessionId)
            ?: throw IllegalStateException("Session introuvable")

        val sessionToClose = details.session.copy(
            countedCash = countedCashCents,
            closingTime = closingTime,
            isClosed = true
        )

        val hash = SessionIntegrity.computeHash(
            sessionToClose,
            details.activeReplenishments,
            details.activeDisbursements
        )

        val finalSession = sessionToClose.copy(integrityHash = hash)
        cashDao.updateSession(finalSession)

        cashDao.insertAuditEntry(
            AuditEntry(
                sessionId = sessionId,
                action = "SESSION_CLOSED",
                entityType = "SESSION",
                entityId = sessionId,
                detailsJson = """{"countedCash":$countedCashCents,"closingTime":"$closingTime","integrityHash":"$hash"}""",
                reason = "Clôture de session avec empreinte d'intégrité"
            )
        )
    }

    /**
     * Réouverture d'une session clôturée (avec motif obligatoire et incrément du compteur)
     */
    suspend fun reopenSession(
        sessionId: Long,
        reason: String
    ) = db.withTransaction {
        if (reason.isBlank()) {
            throw IllegalArgumentException("Un motif de réouverture est obligatoire.")
        }

        val session = cashDao.getSessionWithDetails(sessionId)?.session
            ?: throw IllegalStateException("Session introuvable")

        val updated = session.copy(
            isClosed = false,
            reopenCount = session.reopenCount + 1,
            integrityHash = null
        )
        cashDao.updateSession(updated)

        cashDao.insertAuditEntry(
            AuditEntry(
                sessionId = sessionId,
                action = "SESSION_REOPENED",
                entityType = "SESSION",
                entityId = sessionId,
                detailsJson = """{"reopenCount":${updated.reopenCount}}""",
                reason = reason.trim()
            )
        )
    }

    /**
     * Modification des paramètres de la session avec audit
     */
    suspend fun updateSessionSettings(
        sessionId: Long,
        establishmentName: String,
        establishmentSubTitle: String,
        responsibleName: String,
        managerName: String,
        currency: String,
        initialFundCents: Long,
        reason: String = "Mise à jour des paramètres"
    ) = db.withTransaction {
        val session = cashDao.getSessionWithDetails(sessionId)?.session
            ?: throw IllegalStateException("Session introuvable")

        if (session.isClosed) {
            throw IllegalStateException("Impossible de modifier les paramètres d'une session clôturée.")
        }

        cashDao.updateSession(
            session.copy(
                establishmentName = establishmentName.trim(),
                establishmentSubTitle = establishmentSubTitle.trim(),
                responsibleName = responsibleName.trim(),
                managerName = managerName.trim(),
                currency = currency.trim(),
                initialFund = initialFundCents
            )
        )

        cashDao.insertAuditEntry(
            AuditEntry(
                sessionId = sessionId,
                action = "SETTINGS_CHANGED",
                entityType = "SESSION",
                entityId = sessionId,
                detailsJson = """{"initialFund":$initialFundCents,"currency":"$currency","establishment":"$establishmentName"}""",
                reason = reason
            )
        )
    }

    suspend fun deleteSession(sessionId: Long) = db.withTransaction {
        cashDao.deleteSession(sessionId)
    }

    /**
     * Chargement des données de démonstration (disponible en mode DEBUG uniquement)
     */
    suspend fun loadDemoData(): Long = db.withTransaction {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
        val now = Date()
        val sessionId = cashDao.insertSession(
            CashSession(
                reference = "REC-DEMO-${(100..999).random()}",
                dateText = dateFormat.format(now),
                dateMillis = System.currentTimeMillis(),
                establishmentName = "SYMPHONIX",
                establishmentSubTitle = "Pâtisserie & Salon de thé",
                responsibleName = "Samir H.",
                managerName = "Direction Générale",
                initialFund = 15000000L, // 150 000,00 DA
                initialFundTime = "08h00",
                initialFundSource = "Dotation de caisse",
                countedCash = null,
                closingTime = null,
                isClosed = false,
                currency = "DA",
                notes = "Session de démonstration."
            )
        )

        cashDao.insertDisbursement(
            Disbursement(
                sessionId = sessionId,
                orderNumber = 1,
                time = "08:45",
                designation = "Plateaux d'œufs frais",
                parentCategory = "Matières premières",
                subCategory = "Œufs",
                amount = 5700000L, // 57 000,00 DA
                recipient = "Fournisseur Avicole"
            )
        )

        cashDao.insertDisbursement(
            Disbursement(
                sessionId = sessionId,
                orderNumber = 2,
                time = "09:30",
                designation = "Acompte commis pâtisserie",
                parentCategory = "Personnel",
                subCategory = "Rémunération",
                amount = 1200000L, // 12 000,00 DA
                recipient = "Bassem"
            )
        )

        cashDao.insertDisbursement(
            Disbursement(
                sessionId = sessionId,
                orderNumber = 3,
                time = "10:15",
                designation = "Filets de poulet",
                parentCategory = "Matières premières",
                subCategory = "Volailles",
                amount = 4280000L, // 42 800,00 DA
                recipient = "Boucherie Centrale"
            )
        )

        cashDao.insertReplenishment(
            CashReplenishment(
                sessionId = sessionId,
                orderNumber = 1,
                time = "11:00",
                amount = 3000000L, // 30 000,00 DA
                reason = "Apport vente comptoir",
                sourceLocation = "Caisse n°2",
                registeredBy = "Samir H."
            )
        )

        cashDao.insertAuditEntry(
            AuditEntry(
                sessionId = sessionId,
                action = "SESSION_CREATED",
                entityType = "SESSION",
                entityId = sessionId,
                detailsJson = """{"demo":true}""",
                reason = "Chargement des données de démonstration"
            )
        )

        sessionId
    }
}
