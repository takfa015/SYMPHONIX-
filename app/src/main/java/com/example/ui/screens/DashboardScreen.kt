package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SessionWithDetails
import com.example.ui.components.CashTab
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
import com.example.ui.theme.SymphonixBlue
import com.example.ui.theme.SymphonixDeepBlue
import com.example.ui.theme.SymphonixLightBlue
import com.example.ui.theme.SymphonixNearBlack
import com.example.util.CashPdfGenerator
import kotlin.math.abs

@Composable
fun DashboardScreen(
    sessionDetails: SessionWithDetails?,
    onNavigateTab: (CashTab) -> Unit,
    onOpenReplenishDialog: () -> Unit,
    onOpenDisburseDialog: () -> Unit,
    onOpenCloseDialog: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (sessionDetails == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            WaterDropCard(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Aucune session active",
                    style = MaterialTheme.typography.titleMedium,
                    color = GlassTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onNavigateTab(CashTab.CLOTURE) },
                    colors = ButtonDefaults.buttonColors(containerColor = SymphonixBlue)
                ) {
                    Text("Ouvrir une session de caisse", color = GlassPureWhite)
                }
            }
        }
        return
    }

    val session = sessionDetails.session
    val currency = session.currency

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Symphonix Brand Banner & Session Header
            WaterDropCard(
                accentGlow = SymphonixBlue,
                containerColor = Color(0x99FFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brand Fox Icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .border(1.2.dp, SymphonixBlue.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_symphonix_fox),
                                contentDescription = "Symphonix Mascot",
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = session.establishmentName.ifBlank { "SYMPHONIX" },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = SymphonixDeepBlue
                                )
                            }
                            Text(
                                text = session.establishmentSubTitle.ifBlank { "Maîtrisez. Optimisez. Évoluez." },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = SymphonixBlue
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Reference Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SymphonixLightBlue)
                                        .border(1.dp, SymphonixBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = session.reference,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SymphonixDeepBlue
                                    )
                                }

                                // Status Pill
                                val isClosed = session.isClosed
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isClosed) GlassEmeraldGreenBg else SymphonixLightBlue)
                                        .border(
                                            1.dp,
                                            if (isClosed) GlassEmeraldGreen.copy(alpha = 0.4f) else SymphonixBlue.copy(alpha = 0.4f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isClosed) "Clôturée (${session.closingTime ?: ""})" else "En cours (Ouverte)",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isClosed) GlassEmeraldGreen else SymphonixBlue
                                    )
                                }
                            }
                        }
                    }

                    // Direct PDF Button
                    Button(
                        onClick = onExportPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = SymphonixBlue),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            tint = GlassPureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PDF",
                            fontWeight = FontWeight.Bold,
                            color = GlassPureWhite,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 4 KPI Cards (Water Droplets with Blanc, Vert, Bleu, Rouge)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Fond Total Disponible (Bleu Symphonix)
                    KpiDropletCard(
                        title = "Fond Disponible",
                        amount = CashPdfGenerator.formatAmount(sessionDetails.totalAvailableFund, currency),
                        subtitle = if (sessionDetails.totalReplenishments > 0) {
                            "Init. + ${CashPdfGenerator.formatAmount(sessionDetails.totalReplenishments, currency)}"
                        } else "Dotation : ${session.initialFundSource}",
                        accentColor = SymphonixBlue,
                        icon = Icons.Default.Savings,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(CashTab.ENCAISSEMENT) }
                    )

                    // 2. Total Décaissements (Rouge)
                    KpiDropletCard(
                        title = "Total Décaissements",
                        amount = CashPdfGenerator.formatAmount(sessionDetails.totalDisbursements, currency),
                        subtitle = "${CashPdfGenerator.formatPercent(sessionDetails.expensePercentageOfFund)} du fond",
                        accentColor = GlassCoralRed,
                        icon = Icons.Default.ReceiptLong,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(CashTab.DECAISSEMENT) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 3. Solde Théorique (Bleu profond Symphonix)
                    KpiDropletCard(
                        title = "Solde Théorique",
                        amount = CashPdfGenerator.formatAmount(sessionDetails.theoreticalBalance, currency),
                        subtitle = "${sessionDetails.disbursements.size} dépenses saisies",
                        accentColor = SymphonixDeepBlue,
                        icon = Icons.Default.ArrowDownward,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(CashTab.CLOTURE) }
                    )

                    // 4. Reste Physique & Concordance (Verte / Rouge)
                    val isBalanced = sessionDetails.isBalanced
                    val hasCounted = session.countedCash != null
                    val disc = sessionDetails.discrepancy
                    val kpiColor = when {
                        !hasCounted -> GlassTextMuted
                        isBalanced -> GlassEmeraldGreen
                        else -> GlassCoralRed
                    }

                    KpiDropletCard(
                        title = "Reste Physique",
                        amount = if (hasCounted) CashPdfGenerator.formatAmount(sessionDetails.countedCash, currency) else "-- $currency",
                        subtitle = when {
                            !hasCounted -> "En attente pointage"
                            isBalanced -> "Concordance 100% (0 DA)"
                            disc > 0 -> "Excédent +${CashPdfGenerator.formatAmount(disc, currency)}"
                            else -> "Déficit ${CashPdfGenerator.formatAmount(disc, currency)}"
                        },
                        accentColor = kpiColor,
                        icon = if (isBalanced) Icons.Default.CheckCircle else Icons.Default.Lock,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(CashTab.CLOTURE) }
                    )
                }
            }
        }

        // Fund Usage Gauge Droplet
        item {
            WaterDropCard(
                accentGlow = SymphonixBlue,
                containerColor = Color(0x73FFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Consommation du Fond de Caisse",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = GlassTextPrimary
                    )
                    Text(
                        text = CashPdfGenerator.formatPercent(sessionDetails.expensePercentageOfFund),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (sessionDetails.expensePercentageOfFund > 85.0) GlassCoralRed else SymphonixBlue
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val progress = (sessionDetails.expensePercentageOfFund / 100.0).toFloat().coerceIn(0f, 1f)
                val progressColor = if (progress > 0.85f) GlassCoralRed else SymphonixBlue

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = progressColor,
                    trackColor = SymphonixLightBlue,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Reste disponible : ${CashPdfGenerator.formatAmount(sessionDetails.theoreticalBalance, currency)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = GlassTextSecondary
                    )
                    Text(
                        text = "100% = ${CashPdfGenerator.formatAmount(sessionDetails.totalAvailableFund, currency)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = GlassTextMuted
                    )
                }
            }
        }

        // Quick Actions Row (Eau & Actions)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bouton Alimentation
                Button(
                    onClick = onOpenReplenishDialog,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SymphonixLightBlue),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SymphonixBlue.copy(alpha = 0.4f))
                ) {
                    Icon(
                        Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = SymphonixBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Alimenter",
                        color = SymphonixDeepBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }

                // Bouton Décaissement
                Button(
                    onClick = onOpenDisburseDialog,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassCoralRedBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassCoralRed.copy(alpha = 0.4f))
                ) {
                    Icon(
                        Icons.Default.RemoveCircle,
                        contentDescription = null,
                        tint = GlassCoralRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "- Décaisser",
                        color = GlassCoralRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }

                // Bouton Pointer/Clôturer
                Button(
                    onClick = onOpenCloseDialog,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (session.isClosed) GlassEmeraldGreenBg else Color(0x33CBD5E1)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (session.isClosed) GlassEmeraldGreen.copy(alpha = 0.4f) else Color(0x4094A3B8)
                    )
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (session.isClosed) GlassEmeraldGreen else GlassTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (session.isClosed) "Clôturé" else "Pointer",
                        color = if (session.isClosed) GlassEmeraldGreen else GlassTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }

        // Recent Movements Feed
        item {
            WaterDropCard(
                accentGlow = SymphonixBlue,
                containerColor = Color(0x8CFFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SymphonixLightBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = SymphonixBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Derniers Mouvements de Caisse",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = GlassTextPrimary
                        )
                    }

                    Text(
                        text = "Voir tout",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SymphonixBlue,
                        modifier = Modifier.clickable { onNavigateTab(CashTab.DECAISSEMENT) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (sessionDetails.disbursements.isEmpty() && sessionDetails.replenishments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun mouvement enregistré pour cette session.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassTextMuted
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Show up to 4 recent disbursements
                        sessionDetails.disbursements.takeLast(4).reversed().forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x40FFFFFF))
                                    .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(GlassCoralRedBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${item.orderNumber}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GlassCoralRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = item.designation,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = GlassTextPrimary
                                        )
                                        Text(
                                            text = "${item.time} • ${item.parentCategory}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = GlassTextMuted
                                        )
                                    }
                                }

                                Text(
                                    text = "-${CashPdfGenerator.formatAmount(item.amount, currency)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GlassCoralRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom spacer for floating glass navigation bar
        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun KpiDropletCard(
    title: String,
    amount: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    WaterDropCard(
        modifier = modifier,
        accentGlow = accentColor,
        containerColor = Color(0x8CFFFFFF),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp
                ),
                color = GlassTextSecondary
            )

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            ),
            color = GlassTextPrimary
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            ),
            color = accentColor
        )
    }
}
