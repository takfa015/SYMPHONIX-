package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlueLight
import com.example.ui.theme.BlueReplenish
import com.example.ui.theme.DarkTealHeader
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.MintLight
import com.example.ui.theme.RedExpense
import com.example.ui.theme.RedLight
import com.example.util.CashPdfGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddReplenishmentDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, reason: String, sourceLocation: String, time: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var reasonText by remember { mutableStateOf("") }
    var sourceText by remember { mutableStateOf("Coffre-fort principal") }
    var timeText by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    val quickReasons = listOf(
        "Réassort urgent",
        "Fond d'appoint",
        "Vente directe comptant",
        "Apport gérance",
        "Rallonge imprévue"
    )

    val quickSources = listOf(
        "Coffre-fort principal",
        "Banque / Dépôt",
        "Caisse n°2",
        "Gérance / Direction"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Input, contentDescription = null, tint = BlueReplenish)
        },
        title = {
            Text("Alimenter la Caisse", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Enregistrer un apport de liquidités ou une rallonge en cours de journée.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Montant ($currency)") },
                    placeholder = { Text("ex: 20000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text("Heure") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Motif / Justification") },
                    placeholder = { Text("ex: Réassort urgent matières") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Motifs fréquents :", style = MaterialTheme.typography.labelSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickReasons.forEach { r ->
                        SuggestionChip(
                            onClick = { reasonText = r },
                            label = { Text(r, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = sourceText,
                    onValueChange = { sourceText = it },
                    label = { Text("Provenance / Emplacement source") },
                    placeholder = { Text("ex: Coffre-fort principal") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Emplacements fréquents :", style = MaterialTheme.typography.labelSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickSources.forEach { s ->
                        SuggestionChip(
                            onClick = { sourceText = s },
                            label = { Text(s, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && reasonText.isNotBlank()) {
                        onConfirm(amt, reasonText.trim(), sourceText.trim(), timeText.trim())
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 && reasonText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BlueReplenish)
            ) {
                Text("Enregistrer l'apport")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddDisbursementDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, designation: String, parentCategory: String, subCategory: String, recipient: String, time: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var designationText by remember { mutableStateOf("") }
    var parentCategory by remember { mutableStateOf("Matières premières") }
    var subCategoryText by remember { mutableStateOf("") }
    var recipientText by remember { mutableStateOf("") }
    var timeText by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    val categories = listOf(
        "Matières premières" to listOf("Œufs", "Volailles", "Farine & Levure", "Beurre & Sucre", "Produits laitiers"),
        "Personnel" to listOf("Rémunération", "Avance salaire", "Prime", "Repas"),
        "Frais généraux" to listOf("Emballages", "Électricité / Gaz", "Entretien", "Eau"),
        "Transport" to listOf("Carburant", "Frais livraison", "Péage"),
        "Divers" to listOf("Fournitures", "Autre")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Output, contentDescription = null, tint = DarkTealHeader)
        },
        title = {
            Text("Nouveau Décaissement", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Montant ($currency)") },
                    placeholder = { Text("ex: 57000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text("Heure") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = designationText,
                    onValueChange = { designationText = it },
                    label = { Text("Désignation / Motif") },
                    placeholder = { Text("ex: Œufs ou Bassem (Commis)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Catégorie principale :", style = MaterialTheme.typography.labelSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { (cat, _) ->
                        val isSelected = parentCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DarkTealHeader else Color(0xFFF1F5F9))
                                .clickable {
                                    parentCategory = cat
                                    subCategoryText = ""
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                val currentSubs = categories.find { it.first == parentCategory }?.second ?: emptyList()
                if (currentSubs.isNotEmpty()) {
                    Text("Sous-catégorie :", style = MaterialTheme.typography.labelSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        currentSubs.forEach { sub ->
                            val isSelected = subCategoryText == sub
                            SuggestionChip(
                                onClick = {
                                    subCategoryText = sub
                                    if (designationText.isBlank()) designationText = sub
                                },
                                label = { Text(sub, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = subCategoryText,
                    onValueChange = { subCategoryText = it },
                    label = { Text("Précision catégorie (optionnel)") },
                    placeholder = { Text("ex: Volailles") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = recipientText,
                    onValueChange = { recipientText = it },
                    label = { Text("Bénéficiaire / Tiers (optionnel)") },
                    placeholder = { Text("ex: Bassem, Fournisseur...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && designationText.isNotBlank()) {
                        onConfirm(
                            amt,
                            designationText.trim(),
                            parentCategory.trim(),
                            subCategoryText.trim(),
                            recipientText.trim(),
                            timeText.trim()
                        )
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 && designationText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkTealHeader)
            ) {
                Text("Ajouter la dépense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun CloseCashDialog(
    theoreticalBalance: Double,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (countedCash: Double, closingTime: String) -> Unit
) {
    var isDenominationMode by remember { mutableStateOf(false) }
    var directAmountText by remember {
        mutableStateOf(if (theoreticalBalance > 0) theoreticalBalance.toLong().toString() else "")
    }
    var closingTimeText by remember {
        mutableStateOf(SimpleDateFormat("HH'h'mm", Locale.getDefault()).format(Date()))
    }

    // Billets & pièces DA
    val denominations = listOf(2000, 1000, 500, 200, 100, 50)
    val denomCounts = remember { mutableStateMapOf<Int, Int>() }

    val calculatedDenomTotal: Double = denominations.sumOf { denom ->
        (denomCounts[denom] ?: 0) * denom.toDouble()
    }

    val currentCountedAmount: Double = if (isDenominationMode) {
        calculatedDenomTotal
    } else {
        directAmountText.toDoubleOrNull() ?: 0.0
    }

    val discrepancy = currentCountedAmount - theoreticalBalance
    val isBalanced = kotlin.math.abs(discrepancy) < 0.01

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = GreenSuccess)
        },
        title = {
            Text("Clôture & Pointage Physique", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Dénombrez les espèces réelles en caisse pour établir le rapprochement de fin de journée.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                )

                // Solde théorique reminder
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Solde Théorique attendu :", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = CashPdfGenerator.formatAmount(theoreticalBalance, currency),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedTextField(
                    value = closingTimeText,
                    onValueChange = { closingTimeText = it },
                    label = { Text("Heure de clôture physique") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Mode switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isDenominationMode) DarkTealHeader else Color(0xFFE2E8F0))
                            .clickable { isDenominationMode = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Montant direct",
                            color = if (!isDenominationMode) Color.White else Color(0xFF334155),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDenominationMode) DarkTealHeader else Color(0xFFE2E8F0))
                            .clickable { isDenominationMode = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Comptage coupures",
                            color = if (isDenominationMode) Color.White else Color(0xFF334155),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                if (!isDenominationMode) {
                    OutlinedTextField(
                        value = directAmountText,
                        onValueChange = { directAmountText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Espèces constatées ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Denomination counter list
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        denominations.forEach { denom ->
                            val count = denomCounts[denom] ?: 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$denom $currency",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.width(70.dp)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (count > 0) denomCounts[denom] = count - 1
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }

                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            denomCounts[denom] = count + 1
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Text(
                                    text = CashPdfGenerator.formatAmount((count * denom).toDouble(), currency),
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                                )
                            }
                        }
                    }
                }

                // Instant Discrepancy Preview
                val previewBg = if (isBalanced) GreenLight else if (discrepancy > 0) Color(0xFFFEF3C7) else RedLight
                val previewColor = if (isBalanced) GreenSuccess else if (discrepancy > 0) Color(0xFFD97706) else RedExpense

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(previewBg)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Reste physique constaté :",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = previewColor)
                        )
                        Text(
                            text = CashPdfGenerator.formatAmount(currentCountedAmount, currency),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold, color = previewColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBalanced) "Concordance exacte :" else "Écart de pointage :",
                            style = MaterialTheme.typography.bodySmall.copy(color = previewColor)
                        )
                        Text(
                            text = if (isBalanced) "0 $currency (100%)" else CashPdfGenerator.formatAmount(discrepancy, currency),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = previewColor)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(currentCountedAmount, closingTimeText.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Valider la clôture")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun NewSessionDialog(
    lastTheoreticalBalance: Double,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (initialFund: Double, source: String, establishment: String, responsible: String) -> Unit
) {
    var initialFundText by remember {
        mutableStateOf(if (lastTheoreticalBalance > 0) lastTheoreticalBalance.toLong().toString() else "150000")
    }
    var sourceText by remember { mutableStateOf("Dotation de caisse") }
    var establishmentText by remember { mutableStateOf("Établissement commercial") }
    var responsibleText by remember { mutableStateOf("Responsable de caisse") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ouvrir une Nouvelle Session de Caisse", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Configurez la dotation de fond de roulement de départ pour cette journée.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                )

                OutlinedTextField(
                    value = initialFundText,
                    onValueChange = { initialFundText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Fond de roulement initial ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sourceText,
                    onValueChange = { sourceText = it },
                    label = { Text("Source de la dotation") },
                    placeholder = { Text("ex: Dotation de caisse / Report veille") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = establishmentText,
                    onValueChange = { establishmentText = it },
                    label = { Text("Raison sociale / Établissement") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = responsibleText,
                    onValueChange = { responsibleText = it },
                    label = { Text("Nom du responsable de caisse") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fund = initialFundText.toDoubleOrNull() ?: 0.0
                    if (fund >= 0) {
                        onConfirm(fund, sourceText.trim(), establishmentText.trim(), responsibleText.trim())
                    }
                },
                enabled = (initialFundText.toDoubleOrNull() ?: -1.0) >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = DarkTealHeader)
            ) {
                Text("Ouvrir la caisse")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
