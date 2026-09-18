package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CashReplenishment
import com.example.data.model.SessionWithDetails
import com.example.ui.components.WaterDropCard
import com.example.ui.theme.GlassEmeraldGreen
import com.example.ui.theme.GlassEmeraldGreenBg
import com.example.ui.theme.GlassPureWhite
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWaterBlue
import com.example.ui.theme.GlassWaterBlueBg
import com.example.util.CashPdfGenerator

@Composable
fun EncaissementScreen(
    sessionDetails: SessionWithDetails?,
    onOpenAddDialog: () -> Unit,
    onDeleteReplenishment: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sessionDetails == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucune session active", color = GlassTextMuted)
        }
        return
    }

    val session = sessionDetails.session
    val currency = session.currency
    val replenishments = sessionDetails.replenishments
    val isClosed = session.isClosed

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Total Banner Droplet
            WaterDropCard(
                accentGlow = GlassWaterBlue,
                containerColor = Color(0x8CFFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Encaissements & Apports",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GlassTextPrimary
                        )
                        Text(
                            text = "Alimentations de caisse de la journée",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GlassWaterBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = null,
                            tint = GlassWaterBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Total Apports du jour",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassTextMuted
                        )
                        Text(
                            text = "+ ${CashPdfGenerator.formatAmount(sessionDetails.totalReplenishments, currency)}",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = GlassWaterBlue
                        )
                    }

                    if (!isClosed) {
                        Button(
                            onClick = onOpenAddDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = GlassWaterBlue),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ajouter", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Initial Fund Reminder Droplet
        item {
            WaterDropCard(
                containerColor = Color(0x73FFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dotation Initiale (Ouverture)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = GlassTextSecondary
                        )
                        Text(
                            text = "Source : ${session.initialFundSource} à ${session.initialFundTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassTextMuted
                        )
                    }
                    Text(
                        text = CashPdfGenerator.formatAmount(session.initialFund, currency),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GlassTextPrimary
                    )
                }
            }
        }

        item {
            Text(
                text = "Historique des Alimentations (${replenishments.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = GlassTextPrimary,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        if (replenishments.isEmpty()) {
            item {
                WaterDropCard(containerColor = Color(0x66FFFFFF)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Savings,
                                contentDescription = null,
                                tint = GlassTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucune alimentation intermédiaire enregistrée",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GlassTextSecondary
                            )
                            Text(
                                text = "Utilisez le bouton Ajouter pour enregistrer un apport",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassTextMuted
                            )
                        }
                    }
                }
            }
        } else {
            items(replenishments, key = { it.id }) { item ->
                ReplenishmentDropletItem(
                    item = item,
                    currency = currency,
                    canDelete = !isClosed,
                    onDelete = { onDeleteReplenishment(item.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun ReplenishmentDropletItem(
    item: CashReplenishment,
    currency: String,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    WaterDropCard(
        accentGlow = GlassWaterBlue,
        containerColor = Color(0x8CFFFFFF)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GlassWaterBlueBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = GlassWaterBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.reason,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GlassTextPrimary
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Time badge
                        Text(
                            text = item.time,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassTextMuted
                        )

                        Text(text = "•", fontSize = 11.sp, color = GlassTextMuted)

                        // Source Location Pill
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = GlassWaterBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = item.sourceLocation,
                                fontSize = 11.sp,
                                color = GlassWaterBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+ ${CashPdfGenerator.formatAmount(item.amount, currency)}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassWaterBlue
                )

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Supprimer",
                            tint = GlassTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
