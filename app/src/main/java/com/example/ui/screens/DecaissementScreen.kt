package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Disbursement
import com.example.data.model.SessionWithDetails
import com.example.ui.components.WaterDropCard
import com.example.ui.theme.GlassCoralRed
import com.example.ui.theme.GlassCoralRedBg
import com.example.ui.theme.GlassPureWhite
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWaterBlue
import com.example.util.CashPdfGenerator

@Composable
fun DecaissementScreen(
    sessionDetails: SessionWithDetails?,
    onOpenAddDialog: () -> Unit,
    onDeleteDisbursement: (Long) -> Unit,
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
    val disbursements = sessionDetails.disbursements
    val isClosed = session.isClosed

    var selectedCategoryFilter by remember { mutableStateOf("Toutes") }

    val categories = remember(disbursements) {
        listOf("Toutes") + disbursements.map { it.parentCategory }.filter { it.isNotBlank() }.distinct()
    }

    val filteredList = remember(disbursements, selectedCategoryFilter) {
        if (selectedCategoryFilter == "Toutes") {
            disbursements
        } else {
            disbursements.filter { it.parentCategory == selectedCategoryFilter }
        }
    }

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
                accentGlow = GlassCoralRed,
                containerColor = Color(0x8CFFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Décaissements & Dépenses",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GlassTextPrimary
                        )
                        Text(
                            text = "Sorties de caisse effectuées",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GlassCoralRedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = GlassCoralRed,
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
                            text = "Total Dépenses du jour",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassTextMuted
                        )
                        Text(
                            text = "- ${CashPdfGenerator.formatAmount(sessionDetails.totalDisbursements, currency)}",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = GlassCoralRed
                        )
                    }

                    if (!isClosed) {
                        Button(
                            onClick = onOpenAddDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = GlassCoralRed),
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

        // Category Filter Chips
        if (categories.size > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == selectedCategoryFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) GlassCoralRed else Color(0x73FFFFFF))
                                .border(
                                    1.dp,
                                    if (isSelected) GlassCoralRed else Color(0x40CBD5E1),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedCategoryFilter = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) GlassPureWhite else GlassTextSecondary
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Journal des Sorties (${filteredList.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = GlassTextPrimary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        if (filteredList.isEmpty()) {
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
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = GlassTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucun décaissement dans cette catégorie",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GlassTextSecondary
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredList, key = { it.id }) { item ->
                DisbursementDropletItem(
                    item = item,
                    currency = currency,
                    canDelete = !isClosed,
                    onDelete = { onDeleteDisbursement(item.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun DisbursementDropletItem(
    item: Disbursement,
    currency: String,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    WaterDropCard(
        accentGlow = GlassCoralRed,
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
                // Order Number Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GlassCoralRedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${item.orderNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GlassCoralRed
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.designation,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GlassTextPrimary
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.time,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassTextMuted
                        )

                        Text(text = "•", fontSize = 11.sp, color = GlassTextMuted)

                        // Category tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1F0F172A))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.fullCategory,
                                fontSize = 10.5.sp,
                                color = GlassTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (item.recipient.isNotBlank()) {
                            Text(text = "•", fontSize = 11.sp, color = GlassTextMuted)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = GlassTextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = item.recipient,
                                    fontSize = 10.5.sp,
                                    color = GlassTextMuted
                                )
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "- ${CashPdfGenerator.formatAmount(item.amount, currency)}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassCoralRed
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
