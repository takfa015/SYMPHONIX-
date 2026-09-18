package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SessionWithDetails
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.DarkTealHeader
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedExpense
import com.example.util.CashPdfGenerator

@Composable
fun KpiCardsGrid(
    details: SessionWithDetails,
    modifier: Modifier = Modifier
) {
    val session = details.session
    val percentSpent = details.expensePercentageOfFund

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Fond Initial/Disponible + Total Dépenses
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiSingleCard(
                modifier = Modifier.weight(1f),
                title = if (details.replenishments.isNotEmpty()) "FOND DISPONIBLE" else "FOND INITIAL",
                amount = CashPdfGenerator.formatAmount(details.totalAvailableFund, session.currency),
                subtitle = if (details.replenishments.isNotEmpty()) {
                    "+${details.replenishments.size} apports (${CashPdfGenerator.formatAmount(details.totalReplenishments, session.currency)})"
                } else {
                    "Dotation (${session.initialFundTime})"
                },
                accentColor = DarkTealHeader,
                bgColor = Color(0xFFF1F8F6),
                icon = Icons.Default.AccountBalanceWallet
            )

            KpiSingleCard(
                modifier = Modifier.weight(1f),
                title = "TOTAL DÉPENSES",
                amount = CashPdfGenerator.formatAmount(details.totalDisbursements, session.currency),
                subtitle = "${details.disbursements.size} ops (${CashPdfGenerator.formatPercent(percentSpent)})",
                accentColor = AmberWarning,
                bgColor = Color(0xFFFFFBEB),
                icon = Icons.Default.ReceiptLong
            )
        }

        // Row 2: Reste Physique + Écart de Rapprochement
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val countedStr = if (session.isClosed && session.countedCash != null) {
                CashPdfGenerator.formatAmount(session.countedCash, session.currency)
            } else {
                "En attente"
            }
            val countedSub = if (session.isClosed) {
                "Espèces (${session.closingTime ?: ""})"
            } else {
                "Caisse non clôturée"
            }

            KpiSingleCard(
                modifier = Modifier.weight(1f),
                title = "RESTE CONSTATÉ",
                amount = countedStr,
                subtitle = countedSub,
                accentColor = GreenSuccess,
                bgColor = Color(0xFFF0FDF4),
                icon = Icons.Default.Savings
            )

            val (ecartStr, ecartSub, ecartColor, ecartBg, ecartIcon) = if (!session.isClosed) {
                CardValues("---", "Clôture requise", Color(0xFF64748B), Color(0xFFF8FAFC), Icons.Default.CheckCircle)
            } else if (details.isBalanced) {
                CardValues(
                    "0 ${session.currency}",
                    "Concordance 100%",
                    GreenSuccess,
                    Color(0xFFF0FDF4),
                    Icons.Default.CheckCircle
                )
            } else {
                val sign = if (details.discrepancy > 0) "+" else ""
                CardValues(
                    "$sign${CashPdfGenerator.formatAmount(details.discrepancy, session.currency)}",
                    if (details.discrepancy > 0) "Excédent constaté" else "Déficit constaté",
                    RedExpense,
                    Color(0xFFFEF2F2),
                    Icons.Default.ErrorOutline
                )
            }

            KpiSingleCard(
                modifier = Modifier.weight(1f),
                title = "ÉCART RAPPROCHEMENT",
                amount = ecartStr,
                subtitle = ecartSub,
                accentColor = ecartColor,
                bgColor = ecartBg,
                icon = ecartIcon
            )
        }
    }
}

private data class CardValues(
    val amount: String,
    val sub: String,
    val color: Color,
    val bg: Color,
    val icon: ImageVector
)

@Composable
fun KpiSingleCard(
    title: String,
    amount: String,
    subtitle: String,
    accentColor: Color,
    bgColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 10.sp
                    ),
                    color = accentColor
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .width(16.dp)
                        .height(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                ),
                color = accentColor,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.5.sp
                ),
                color = Color(0xFF64748B),
                maxLines = 1
            )
        }
    }
}
