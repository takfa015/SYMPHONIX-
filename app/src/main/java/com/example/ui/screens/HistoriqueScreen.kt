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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.SessionWithDetails
import com.example.ui.components.WaterDropCard
import com.example.ui.theme.GlassCoralRed
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
fun HistoriqueScreen(
    allSessions: List<SessionWithDetails>,
    selectedSessionId: Long?,
    onSelectSession: (Long) -> Unit,
    onExportPdf: (SessionWithDetails) -> Unit,
    onDeleteSession: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var sessionToDelete by remember { mutableStateOf<SessionWithDetails?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header Banner Droplet
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
                            text = "Historique & Ré-impression PDF",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GlassTextPrimary
                        )
                        Text(
                            text = "Re-générez et imprimez vos documents à tout moment",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GlassWaterBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = GlassWaterBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (allSessions.isEmpty()) {
            item {
                WaterDropCard(containerColor = Color(0x66FFFFFF)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = GlassTextMuted,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucune session dans l'historique",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GlassTextSecondary
                            )
                        }
                    }
                }
            }
        } else {
            items(allSessions, key = { it.session.id }) { sessionDetails ->
                val session = sessionDetails.session
                val isCurrentSelected = session.id == selectedSessionId
                val isClosed = session.isClosed
                val currency = session.currency

                WaterDropCard(
                    accentGlow = if (isCurrentSelected) GlassWaterBlue else null,
                    containerColor = if (isCurrentSelected) Color(0x99FFFFFF) else Color(0x73FFFFFF)
                ) {
                    // Header Row: Reference + Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = session.reference,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = GlassTextPrimary
                                )
                                if (isCurrentSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GlassWaterBlue)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Actuelle",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GlassPureWhite
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${session.dateText} • ${session.establishmentName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassTextMuted
                            )
                        }

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isClosed) GlassEmeraldGreenBg else GlassWaterBlueBg)
                                .border(
                                    1.dp,
                                    if (isClosed) GlassEmeraldGreen.copy(alpha = 0.4f) else GlassWaterBlue.copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isClosed) "Clôturée (${session.closingTime ?: ""})" else "Ouverte",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isClosed) GlassEmeraldGreen else GlassWaterBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Key Figures Summary Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Fond Init.", fontSize = 10.sp, color = GlassTextMuted)
                            Text(
                                text = CashPdfGenerator.formatAmount(session.initialFund, currency),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GlassTextPrimary
                            )
                        }

                        Column {
                            Text(text = "Dépenses", fontSize = 10.sp, color = GlassTextMuted)
                            Text(
                                text = CashPdfGenerator.formatAmount(sessionDetails.totalDisbursements, currency),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GlassCoralRed
                            )
                        }

                        Column {
                            Text(text = "Reste Constaté", fontSize = 10.sp, color = GlassTextMuted)
                            Text(
                                text = if (session.countedCash != null) {
                                    CashPdfGenerator.formatAmount(sessionDetails.countedCash, currency)
                                } else "--",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (sessionDetails.isBalanced) GlassEmeraldGreen else GlassTextPrimary
                            )
                        }

                        Column {
                            Text(text = "Écart", fontSize = 10.sp, color = GlassTextMuted)
                            Text(
                                text = if (session.countedCash != null) {
                                    if (sessionDetails.isBalanced) "0 DA" else CashPdfGenerator.formatAmount(sessionDetails.discrepancy, currency)
                                } else "--",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sessionDetails.isBalanced) GlassEmeraldGreen else GlassCoralRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Direct Actions: Re-Print PDF & Select
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Re-print PDF Button (prominent requested action)
                        Button(
                            onClick = { onExportPdf(sessionDetails) },
                            colors = ButtonDefaults.buttonColors(containerColor = GlassWaterBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = GlassPureWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Imprimer PDF",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassPureWhite
                            )
                        }

                        // Select / View Button
                        if (!isCurrentSelected) {
                            OutlinedButton(
                                onClick = { onSelectSession(session.id) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(0.7f)
                            ) {
                                Text(
                                    text = "Charger",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GlassTextPrimary
                                )
                            }
                        }

                        // Delete button
                        IconButton(
                            onClick = { sessionToDelete = sessionDetails },
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

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    // Confirmation dialog before deleting a session
    if (sessionToDelete != null) {
        val s = sessionToDelete!!
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Supprimer la session ?") },
            text = {
                Text("Voulez-vous supprimer définitivement la session ${s.session.reference} du ${s.session.dateText} et tous ses mouvements ?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSession(s.session.id)
                        sessionToDelete = null
                    }
                ) {
                    Text("Supprimer", color = GlassCoralRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}
