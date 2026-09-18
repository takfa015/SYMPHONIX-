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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SessionWithDetails
import com.example.ui.theme.DarkTealHeader
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.MintLight
import com.example.ui.theme.RedExpense
import com.example.ui.theme.RedLight
import com.example.util.CashPdfGenerator

@Composable
fun RapprochementSection(
    details: SessionWithDetails,
    onCloseCashClick: () -> Unit,
    onReopenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session = details.session
    val isClosed = session.isClosed
    val currency = session.currency

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MintLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = DarkTealHeader,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Rapprochement & Ventilation",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "Contrôle physique et conformité comptable",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                if (isClosed) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GreenLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = GreenSuccess,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clôturée",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GreenSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ouverte",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFD97706),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pointage calculation box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CALCUL DU SOLDE THÉORIQUE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        letterSpacing = 0.5.sp
                    )
                )

                RapprochementItem(
                    label = "(+) Fond de roulement initial (${session.initialFundTime})",
                    value = CashPdfGenerator.formatAmount(session.initialFund, currency)
                )

                if (details.replenishments.isNotEmpty()) {
                    RapprochementItem(
                        label = "(+) Alimentations en cours de journée (${details.replenishments.size})",
                        value = "+ " + CashPdfGenerator.formatAmount(details.totalReplenishments, currency),
                        textColor = Color(0xFF2563EB)
                    )
                }

                RapprochementItem(
                    label = "(-) Décaissements enregistrés (${details.disbursements.size})",
                    value = "- " + CashPdfGenerator.formatAmount(details.totalDisbursements, currency),
                    textColor = Color(0xFFB45309)
                )

                HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 1.dp)

                RapprochementItem(
                    label = "(=) Solde Théorique Calculé",
                    value = CashPdfGenerator.formatAmount(details.theoreticalBalance, currency),
                    isBold = true,
                    fontSize = 14.sp
                )

                if (isClosed && session.countedCash != null) {
                    HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 0.5.dp)

                    RapprochementItem(
                        label = "Espèces Physiques Constatées (${session.closingTime ?: ""})",
                        value = CashPdfGenerator.formatAmount(session.countedCash, currency),
                        isBold = true,
                        textColor = GreenSuccess
                    )

                    val isBalanced = details.isBalanced
                    val ecartBg = if (isBalanced) GreenLight else RedLight
                    val ecartColor = if (isBalanced) GreenSuccess else RedExpense

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ecartBg)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = ecartColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBalanced) "Concordance exacte" else "Écart de pointage :",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ecartColor
                                )
                            )
                        }

                        Text(
                            text = if (isBalanced) "0 $currency (100%)" else CashPdfGenerator.formatAmount(details.discrepancy, currency),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = ecartColor
                            )
                        )
                    }
                }
            }

            // Ventilation by Category
            if (details.categoryBreakdowns.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Ventilation par Catégorie de Dépense",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    details.categoryBreakdowns.forEach { cat ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = "${CashPdfGenerator.formatAmount(cat.amount, currency)} (${CashPdfGenerator.formatPercent(cat.percentage)})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF475569),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { (cat.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = DarkTealHeader,
                                trackColor = MintLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action button to Close or Reopen
            if (!isClosed) {
                Button(
                    onClick = onCloseCashClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkTealHeader,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clôturer & Constater le Reste Physique",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onReopenClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rouvrir la session pour modification", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun RapprochementItem(
    label: String,
    value: String,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.5.sp,
    textColor: Color = Color(0xFF0F172A)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = fontSize,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = fontSize,
                fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = textColor
            )
        )
    }
}
