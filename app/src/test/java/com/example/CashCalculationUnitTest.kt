package com.example

import com.example.data.model.CashReplenishment
import com.example.data.model.CashSession
import com.example.data.model.Disbursement
import com.example.data.model.SessionWithDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CashCalculationUnitTest {

    @Test
    fun testCashCalculationsMatchingDocumentSample() {
        val session = CashSession(
            reference = "REC-20260918-01",
            dateText = "18 Septembre 2026",
            initialFund = 150000.0,
            initialFundTime = "19h35",
            countedCash = 38200.0,
            closingTime = "20h34",
            isClosed = true
        )

        val disbursements = listOf(
            Disbursement(
                sessionId = 1,
                orderNumber = 1,
                time = "19:36",
                designation = "Œufs",
                parentCategory = "Matières premières",
                subCategory = "Œufs",
                amount = 57000.0
            ),
            Disbursement(
                sessionId = 1,
                orderNumber = 2,
                time = "19:36",
                designation = "Bassem (Commis salle pâtisserie)",
                parentCategory = "Personnel",
                subCategory = "Rémunération",
                amount = 12000.0
            ),
            Disbursement(
                sessionId = 1,
                orderNumber = 3,
                time = "19:46",
                designation = "Poulet",
                parentCategory = "Matières premières",
                subCategory = "Volailles",
                amount = 42800.0
            )
        )

        val detailsWithoutReplenishment = SessionWithDetails(
            session = session,
            replenishments = emptyList(),
            disbursements = disbursements
        )

        // Total disbursements = 57000 + 12000 + 42800 = 111800 DA
        assertEquals(111800.0, detailsWithoutReplenishment.totalDisbursements, 0.001)

        // Theoretical balance = 150000 - 111800 = 38200 DA
        assertEquals(38200.0, detailsWithoutReplenishment.theoreticalBalance, 0.001)

        // Discrepancy = 38200 - 38200 = 0 DA (Perfect match 100%)
        assertEquals(0.0, detailsWithoutReplenishment.discrepancy, 0.001)
        assertTrue(detailsWithoutReplenishment.isBalanced)

        // Expense percentage of fund = 111800 / 150000 * 100 = 74.5333%
        assertEquals(74.5333, detailsWithoutReplenishment.expensePercentageOfFund, 0.01)
    }

    @Test
    fun testCashWithInDayReplenishments() {
        val session = CashSession(
            reference = "REC-20260918-02",
            dateText = "18 Septembre 2026",
            initialFund = 150000.0,
            initialFundTime = "10h00",
            countedCash = 58200.0,
            closingTime = "20h00",
            isClosed = true
        )

        val replenishments = listOf(
            CashReplenishment(
                sessionId = 2,
                orderNumber = 1,
                time = "14:15",
                amount = 20000.0,
                reason = "Réassort urgent",
                sourceLocation = "Coffre-fort principal"
            )
        )

        val disbursements = listOf(
            Disbursement(
                sessionId = 2,
                orderNumber = 1,
                time = "15:00",
                designation = "Farine",
                parentCategory = "Matières premières",
                subCategory = "Farine",
                amount = 111800.0
            )
        )

        val details = SessionWithDetails(
            session = session,
            replenishments = replenishments,
            disbursements = disbursements
        )

        // Available Fund = 150000 + 20000 = 170000 DA
        assertEquals(170000.0, details.totalAvailableFund, 0.001)

        // Theoretical balance = 170000 - 111800 = 58200 DA
        assertEquals(58200.0, details.theoreticalBalance, 0.001)

        // Physical counted = 58200 -> Discrepancy = 0 DA
        assertEquals(0.0, details.discrepancy, 0.001)
        assertTrue(details.isBalanced)
    }
}
