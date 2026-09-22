package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.util.SessionIntegrity

@Entity(tableName = "cash_sessions")
data class CashSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reference: String,
    val dateText: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val establishmentName: String = "",
    val establishmentSubTitle: String = "",
    val responsibleName: String = "",
    val managerName: String = "",
    val initialFund: Long = 0L, // Centimes
    val initialFundTime: String = "",
    val initialFundSource: String = "Dotation de caisse",
    val countedCash: Long? = null, // Centimes
    val closingTime: String? = null,
    val isClosed: Boolean = false,
    val reopenCount: Int = 0,
    val integrityHash: String? = null,
    val currency: String = "DA",
    val notes: String = ""
) {
    val initialFundDouble: Double
        get() = com.example.util.Money.centsToDouble(initialFund)
}

@Entity(
    tableName = "cash_replenishments",
    foreignKeys = [
        ForeignKey(
            entity = CashSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sessionId"),
        Index(value = ["sessionId", "orderNumber"], unique = true)
    ]
)
data class CashReplenishment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val orderNumber: Int,
    val time: String,
    val amount: Long, // Centimes
    val reason: String,
    val sourceLocation: String,
    val registeredBy: String = "",
    val cancelledAt: Long? = null,
    val cancelReason: String? = null
) {
    val isCancelled: Boolean get() = cancelledAt != null
}

@Entity(
    tableName = "disbursements",
    foreignKeys = [
        ForeignKey(
            entity = CashSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sessionId"),
        Index(value = ["sessionId", "orderNumber"], unique = true)
    ]
)
data class Disbursement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val orderNumber: Int,
    val time: String,
    val designation: String,
    val parentCategory: String,
    val subCategory: String,
    val amount: Long, // Centimes
    val recipient: String = "",
    val cancelledAt: Long? = null,
    val cancelReason: String? = null
) {
    val isCancelled: Boolean get() = cancelledAt != null

    val fullCategory: String
        get() = if (subCategory.isNotBlank()) "$parentCategory / $subCategory" else parentCategory
}

@Entity(
    tableName = "audit_entries",
    foreignKeys = [
        ForeignKey(
            entity = CashSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class AuditEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String, // SESSION_CREATED, LINE_ADDED, LINE_CANCELLED, SESSION_CLOSED, SESSION_REOPENED, SETTINGS_CHANGED, RESTORED
    val entityType: String, // SESSION, REPLENISHMENT, DISBURSEMENT, SYSTEM
    val entityId: Long? = null,
    val detailsJson: String = "{}",
    val reason: String = ""
)

data class SessionWithDetails(
    @Embedded val session: CashSession,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val replenishments: List<CashReplenishment>,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val disbursements: List<Disbursement>,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val auditEntries: List<AuditEntry> = emptyList()
) {
    val activeReplenishments: List<CashReplenishment>
        get() = replenishments.filter { it.cancelledAt == null }

    val activeDisbursements: List<Disbursement>
        get() = disbursements.filter { it.cancelledAt == null }

    val cancelledReplenishments: List<CashReplenishment>
        get() = replenishments.filter { it.cancelledAt != null }

    val cancelledDisbursements: List<Disbursement>
        get() = disbursements.filter { it.cancelledAt != null }

    val totalCancelledCount: Int
        get() = cancelledReplenishments.size + cancelledDisbursements.size

    val totalReplenishments: Long
        get() = activeReplenishments.sumOf { it.amount }

    val totalDisbursements: Long
        get() = activeDisbursements.sumOf { it.amount }

    val totalAvailableFund: Long
        get() = session.initialFund + totalReplenishments

    val theoreticalBalance: Long
        get() = totalAvailableFund - totalDisbursements

    val theoreticalBalanceDouble: Double
        get() = com.example.util.Money.centsToDouble(theoreticalBalance)

    val countedCash: Long
        get() = session.countedCash ?: 0L

    val discrepancy: Long
        get() = if (session.countedCash != null) session.countedCash - theoreticalBalance else 0L

    val isBalanced: Boolean
        get() = session.countedCash != null && discrepancy == 0L

    val expensePercentageOfFund: Double
        get() = if (totalAvailableFund > 0L) (totalDisbursements.toDouble() / totalAvailableFund.toDouble()) * 100.0 else 0.0

    fun computeCalculatedIntegrityHash(): String {
        return SessionIntegrity.computeHash(session, activeReplenishments, activeDisbursements)
    }

    val isIntegrityValid: Boolean
        get() {
            if (!session.isClosed || session.integrityHash.isNullOrBlank()) return true
            return session.integrityHash == computeCalculatedIntegrityHash()
        }

    val categoryBreakdowns: List<CategoryBreakdown>
        get() {
            val actives = activeDisbursements
            if (actives.isEmpty()) return emptyList()
            val total = totalDisbursements
            val grouped = actives.groupBy { it.parentCategory }
            return grouped.map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                val subItems = list.map { it.subCategory }.filter { it.isNotBlank() }.distinct()
                val subLabel = if (subItems.isNotEmpty()) " (${subItems.joinToString(" & ")})" else ""
                CategoryBreakdown(
                    name = "$cat$subLabel",
                    parentCategory = cat,
                    amount = sum,
                    percentage = if (total > 0L) (sum.toDouble() / total.toDouble()) * 100.0 else 0.0
                )
            }.sortedByDescending { it.amount }
        }
}

data class CategoryBreakdown(
    val name: String,
    val parentCategory: String,
    val amount: Long, // Centimes
    val percentage: Double
)
