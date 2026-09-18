package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.Embedded

@Entity(tableName = "cash_sessions")
data class CashSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reference: String,
    val dateText: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val establishmentName: String = "Établissement commercial",
    val establishmentSubTitle: String = "Salle pâtisserie & Vente",
    val responsibleName: String = "Responsable de caisse",
    val managerName: String = "Direction / Gérance",
    val initialFund: Double = 150000.0,
    val initialFundTime: String = "19h35",
    val initialFundSource: String = "Dotation de caisse",
    val countedCash: Double? = null,
    val closingTime: String? = null,
    val isClosed: Boolean = false,
    val currency: String = "DA",
    val notes: String = ""
)

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
    indices = [Index("sessionId")]
)
data class CashReplenishment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val orderNumber: Int,
    val time: String,
    val amount: Double,
    val reason: String, // e.g., "Réassort urgent", "Apport supplémentaire", "Vente directe"
    val sourceLocation: String, // e.g., "Coffre-fort principal", "Banque", "Caisse n°2", "Gérance"
    val registeredBy: String = ""
)

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
    indices = [Index("sessionId")]
)
data class Disbursement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val orderNumber: Int,
    val time: String,
    val designation: String, // e.g., "Œufs", "Bassem (Commis salle pâtisserie)", "Poulet"
    val parentCategory: String, // e.g., "Matières premières", "Personnel", "Frais généraux"
    val subCategory: String, // e.g., "Œufs", "Rémunération", "Volailles"
    val amount: Double,
    val recipient: String = ""
) {
    val fullCategory: String
        get() = if (subCategory.isNotBlank()) "$parentCategory / $subCategory" else parentCategory
}

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
    val disbursements: List<Disbursement>
) {
    val totalReplenishments: Double
        get() = replenishments.sumOf { it.amount }

    val totalDisbursements: Double
        get() = disbursements.sumOf { it.amount }

    val totalAvailableFund: Double
        get() = session.initialFund + totalReplenishments

    val theoreticalBalance: Double
        get() = totalAvailableFund - totalDisbursements

    val countedCash: Double
        get() = session.countedCash ?: 0.0

    val discrepancy: Double
        get() = if (session.countedCash != null) session.countedCash - theoreticalBalance else 0.0

    val isBalanced: Boolean
        get() = session.countedCash != null && kotlin.math.abs(discrepancy) < 0.01

    val expensePercentageOfFund: Double
        get() = if (totalAvailableFund > 0) (totalDisbursements / totalAvailableFund) * 100.0 else 0.0

    // Grouping by parent category for ventilation
    val categoryBreakdowns: List<CategoryBreakdown>
        get() {
            if (disbursements.isEmpty()) return emptyList()
            val total = totalDisbursements
            val grouped = disbursements.groupBy { it.parentCategory }
            return grouped.map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                val subItems = list.map { it.subCategory }.filter { it.isNotBlank() }.distinct()
                val subLabel = if (subItems.isNotEmpty()) " (${subItems.joinToString(" & ")})" else ""
                CategoryBreakdown(
                    name = "$cat$subLabel",
                    parentCategory = cat,
                    amount = sum,
                    percentage = if (total > 0) (sum / total) * 100.0 else 0.0
                )
            }.sortedByDescending { it.amount }
        }
}

data class CategoryBreakdown(
    val name: String,
    val parentCategory: String,
    val amount: Double,
    val percentage: Double
)
