package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.components.WaterDropCard
import com.example.ui.theme.GlassBorderTop
import com.example.ui.theme.GlassCoralRed
import com.example.ui.theme.GlassCoralRedBg
import com.example.ui.theme.GlassEmeraldGreen
import com.example.ui.theme.GlassEmeraldGreenBg
import com.example.ui.theme.GlassPureWhite
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWaterBlue
import com.example.ui.theme.GlassWaterBlueBg
import com.example.util.CashPdfGenerator
import kotlin.math.abs

@Composable
fun ClotureScreen(
    sessionDetails: SessionWithDetails?,
    onOpenCloseDialog: () -> Unit,
    onReopenSession: () -> Unit,
    onOpenNewSessionDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (sessionDetails == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            WaterDropCard(modifier = Modifier.padding(24.dp)) {
                Text("Aucune session active", color = GlassTextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOpenNewSessionDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = GlassWaterBlue)
                ) {
                    Text("Créer une nouvelle session", color = GlassPureWhite)
                }
            }
        }
        return
    }

    val session = sessionDetails.session
    val currency = session.currency
    val isClosed = session.isClosed
    val isBalanced = sessionDetails.isBalanced
    val hasCounted = session.countedCash != null
    val discrepancy = sessionDetails.discrepancy

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Status Banner Card
            WaterDropCard(
                accentGlow = if (isClosed) GlassEmeraldGreen else GlassWaterBlue,
                containerColor = Color(0x8CFFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isClosed) "Caisse Clôturée" else "Caisse Ouverte & En Cours",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GlassTextPrimary
                        )
                        Text(
                            text = if (isClosed) "Clôturée à ${session.closingTime ?: ""}" else "Pointage en cours",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isClosed) GlassEmeraldGreenBg else GlassWaterBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isClosed) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (isClosed) GlassEmeraldGreen else GlassWaterBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action trigger depending on status
                if (!isClosed) {
                    Button(
                        onClick = onOpenCloseDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = GlassEmeraldGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Effectuer le Pointage & Clôturer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReopenSession,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = com.example.ui.theme.SymphonixDeepBlue
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, com.example.ui.theme.SymphonixBlue.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = com.example.ui.theme.SymphonixDeepBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Rouvrir",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.SymphonixDeepBlue
                            )
                        }

                        Button(
                            onClick = onOpenNewSessionDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = GlassWaterBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nouvelle Caisse", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Theoretical Reconciliation Breakdown Droplet
        item {
            WaterDropCard(
                accentGlow = GlassBorderTop,
                containerColor = Color(0x73FFFFFF)
            ) {
                Text(
                    text = "Détail du Rapprochement Comptable",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = GlassTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Initial fund
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "(+) Fond initial doté", style = MaterialTheme.typography.bodyMedium, color = GlassTextSecondary)
                    Text(
                        text = "+ ${CashPdfGenerator.formatAmount(session.initialFund, currency)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = GlassTextPrimary
                    )
                }

                // Replenishments
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "(+) Alimentations de journée (${sessionDetails.replenishments.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassTextSecondary
                    )
                    Text(
                        text = "+ ${CashPdfGenerator.formatAmount(sessionDetails.totalReplenishments, currency)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = GlassWaterBlue
                    )
                }

                // Total fund
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "(=) Total Fonds Disponibles",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GlassTextPrimary
                    )
                    Text(
                        text = CashPdfGenerator.formatAmount(sessionDetails.totalAvailableFund, currency),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GlassWaterBlue
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x33CBD5E1)))
                Spacer(modifier = Modifier.height(6.dp))

                // Disbursements
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "(-) Total Décaissements (${sessionDetails.disbursements.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassTextSecondary
                    )
                    Text(
                        text = "- ${CashPdfGenerator.formatAmount(sessionDetails.totalDisbursements, currency)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GlassCoralRed
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x33CBD5E1)))
                Spacer(modifier = Modifier.height(6.dp))

                // Theoretical Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "(=) Solde Théorique Attendu",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = GlassTextPrimary
                    )
                    Text(
                        text = CashPdfGenerator.formatAmount(sessionDetails.theoreticalBalance, currency),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = GlassTextPrimary
                    )
                }
            }
        }

        // Counted Cash & Discrepancy Status Droplet
        item {
            WaterDropCard(
                accentGlow = when {
                    !hasCounted -> GlassTextMuted
                    isBalanced -> GlassEmeraldGreen
                    else -> GlassCoralRed
                },
                containerColor = Color(0x8CFFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pointage Physique & Écart",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = GlassTextPrimary
                        )
                        Text(
                            text = if (hasCounted) "Espèces réelles dans le tiroir-caisse" else "En attente du comptage de clôture",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassTextSecondary
                        )
                    }

                    if (hasCounted) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isBalanced) GlassEmeraldGreenBg else GlassCoralRedBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isBalanced) GlassEmeraldGreen else GlassCoralRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Reste Constaté", fontSize = 11.sp, color = GlassTextMuted)
                        Text(
                            text = if (hasCounted) CashPdfGenerator.formatAmount(sessionDetails.countedCash, currency) else "-- $currency",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (isBalanced) GlassEmeraldGreen else GlassTextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Écart de Caisse", fontSize = 11.sp, color = GlassTextMuted)
                        Text(
                            text = when {
                                !hasCounted -> "-- $currency"
                                isBalanced -> "0 DA (Parfait)"
                                discrepancy > 0 -> "+ ${CashPdfGenerator.formatAmount(discrepancy, currency)} (Excédent)"
                                else -> "${CashPdfGenerator.formatAmount(discrepancy, currency)} (Déficit)"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (isBalanced) GlassEmeraldGreen else GlassCoralRed
                        )
                    }
                }

                if (hasCounted && isBalanced) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GlassEmeraldGreenBg)
                            .border(1.dp, GlassEmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✓ Rapprochement 100% conforme. Aucun écart constaté.",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassEmeraldGreen
                        )
                    }
                }
            }
        }

        // New Session Trigger button
        item {
            WaterDropCard(
                accentGlow = GlassWaterBlue,
                containerColor = Color(0x66FFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Nouvelle Journée / Session",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = GlassTextPrimary
                        )
                        Text(
                            text = "Ouvrez une nouvelle session avec un nouveau fond",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassTextMuted
                        )
                    }

                    Button(
                        onClick = onOpenNewSessionDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = GlassWaterBlue),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ouvrir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}
