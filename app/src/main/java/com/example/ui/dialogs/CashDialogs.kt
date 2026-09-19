package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.GlassBorderBottom
import com.example.ui.theme.GlassBorderTop
import com.example.ui.theme.GlassCoralRed
import com.example.ui.theme.GlassCoralRedBg
import com.example.util.CategoryManager
import com.example.util.ExpenseCategory
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
import com.example.ui.theme.SymphonixGreenSuccess
import com.example.ui.theme.SymphonixLightBlue
import com.example.ui.theme.SymphonixRedExpense
import com.example.util.CashPdfGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Premium Water-Droplet Glassmorphic Modal Dialog Container
 */
@Composable
fun GlassmorphicDialogContainer(
    onDismissRequest: () -> Unit,
    accentColor: Color,
    icon: ImageVector,
    title: String,
    subtitle: String,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            val dialogShape = RoundedCornerShape(26.dp)

            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = dialogShape,
                        ambientColor = accentColor.copy(alpha = 0.2f),
                        spotColor = SymphonixDeepBlue.copy(alpha = 0.25f)
                    )
                    .clip(dialogShape)
                    .background(Color(0xF7FFFFFF))
                    .border(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                GlassPureWhite,
                                accentColor.copy(alpha = 0.35f),
                                Color(0x33146BFF)
                            )
                        ),
                        shape = dialogShape
                    )
            ) {
                // Header Bar with Glowing Icon Badge, Title and Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accentColor.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Glowing Icon Bubble
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.12f))
                                .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.2.sp
                                ),
                                color = SymphonixDeepBlue
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassTextMuted
                            )
                        }

                        // Close Button
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                // Scrollable Form Body
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    content()
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                // Bottom Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        dismissButton()
                    }
                    Box(modifier = Modifier.weight(1.4f)) {
                        confirmButton()
                    }
                }
            }
        }
    }
}

/**
 * Standard styled OutlinedTextField matching Symphonix aesthetic
 */
@Composable
private fun StyledDialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    accentColor: Color = SymphonixBlue,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: ImageVector? = null,
    trailingText: String? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
        placeholder = if (placeholder.isNotBlank()) {
            { Text(placeholder, color = Color(0xFF475569), fontSize = 13.sp) }
        } else null,
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        trailingIcon = trailingText?.let {
            {
                Text(
                    text = it,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF0F172A),
            unfocusedTextColor = Color(0xFF0F172A),
            focusedBorderColor = accentColor,
            unfocusedBorderColor = Color(0xFF94A3B8),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF8FAFC),
            focusedLabelColor = accentColor,
            unfocusedLabelColor = Color(0xFF1E293B),
            focusedPlaceholderColor = Color(0xFF475569),
            unfocusedPlaceholderColor = Color(0xFF475569),
            cursorColor = accentColor
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Dialog 1: Alimenter la Caisse (Replenishment)
 */
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
        "Vente comptant",
        "Apport gérance",
        "Rallonge monnaie"
    )

    val quickSources = listOf(
        "Coffre-fort principal",
        "Banque / Dépôt",
        "Caisse n°2",
        "Gérance"
    )

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val isValid = amount > 0 && reasonText.isNotBlank()

    GlassmorphicDialogContainer(
        onDismissRequest = onDismiss,
        accentColor = SymphonixBlue,
        icon = Icons.Default.Input,
        title = "Alimenter la Caisse",
        subtitle = "Enregistrer un apport de liquidités en cours de journée",
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569)),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1))))
            ) {
                Text("Annuler", fontWeight = FontWeight.Medium)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(amount, reasonText.trim(), sourceText.trim(), timeText.trim())
                    }
                },
                enabled = isValid,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SymphonixBlue,
                    disabledContainerColor = Color(0xFF94A3B8)
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enregistrer", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Amount and Time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StyledDialogTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = "Montant de l'apport",
                    placeholder = "ex: 25000",
                    trailingText = currency,
                    keyboardType = KeyboardType.Number,
                    accentColor = SymphonixBlue,
                    modifier = Modifier.weight(1.4f)
                )

                StyledDialogTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = "Heure",
                    leadingIcon = Icons.Default.AccessTime,
                    accentColor = SymphonixBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            // Reason field
            StyledDialogTextField(
                value = reasonText,
                onValueChange = { reasonText = it },
                label = "Motif / Justification",
                placeholder = "ex: Réassort urgent matières",
                accentColor = SymphonixBlue
            )

            // Quick Reasons Pills
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Motifs fréquents :",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickReasons.forEach { r ->
                        val isSelected = reasonText == r
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SymphonixLightBlue else Color(0xFFF1F5F9))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) SymphonixBlue else Color(0xFF94A3B8),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { reasonText = r }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = r,
                                fontSize = 11.5.sp,
                                color = if (isSelected) SymphonixDeepBlue else Color(0xFF0F172A),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Source Location field
            StyledDialogTextField(
                value = sourceText,
                onValueChange = { sourceText = it },
                label = "Provenance / Emplacement source",
                placeholder = "ex: Coffre-fort principal",
                accentColor = SymphonixBlue
            )

            // Quick Sources Pills
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Sources fréquentes :",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickSources.forEach { s ->
                        val isSelected = sourceText == s
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SymphonixLightBlue else Color(0xFFF1F5F9))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) SymphonixBlue else Color(0xFF94A3B8),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { sourceText = s }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = s,
                                fontSize = 11.5.sp,
                                color = if (isSelected) SymphonixDeepBlue else Color(0xFF0F172A),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog 2: Nouveau Décaissement (Disbursement)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddDisbursementDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, designation: String, parentCategory: String, subCategory: String, recipient: String, time: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val categoriesList = remember { CategoryManager.getCategories(context) }

    var amountText by remember { mutableStateOf("") }
    var designationText by remember { mutableStateOf("") }
    var parentCategory by remember {
        mutableStateOf(categoriesList.firstOrNull()?.name ?: "Matières premières")
    }
    var subCategoryText by remember { mutableStateOf("") }
    var recipientText by remember { mutableStateOf("") }
    var timeText by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val isValid = amount > 0 && designationText.isNotBlank()

    GlassmorphicDialogContainer(
        onDismissRequest = onDismiss,
        accentColor = GlassCoralRed,
        icon = Icons.Default.Output,
        title = "Nouveau Décaissement",
        subtitle = "Enregistrer une dépense ou sortie d'espèces de la caisse",
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A)),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color(0xFF94A3B8), Color(0xFF94A3B8))))
            ) {
                Text("Annuler", fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(
                            amount,
                            designationText.trim(),
                            parentCategory.trim(),
                            subCategoryText.trim(),
                            recipientText.trim(),
                            timeText.trim()
                        )
                    }
                },
                enabled = isValid,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GlassCoralRed,
                    disabledContainerColor = Color(0xFF94A3B8)
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ajouter dépense", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Amount & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StyledDialogTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = "Montant décaissé",
                    placeholder = "ex: 57000",
                    trailingText = currency,
                    keyboardType = KeyboardType.Number,
                    accentColor = GlassCoralRed,
                    modifier = Modifier.weight(1.4f)
                )

                StyledDialogTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = "Heure",
                    leadingIcon = Icons.Default.AccessTime,
                    accentColor = GlassCoralRed,
                    modifier = Modifier.weight(1f)
                )
            }

            // Designation
            StyledDialogTextField(
                value = designationText,
                onValueChange = { designationText = it },
                label = "Désignation / Motif précis",
                placeholder = "ex: Achat Œufs ou Bassem (Commis)",
                accentColor = GlassCoralRed
            )

            // Category Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Catégorie principale :",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A)
                )

                // Horizontally scrollable categorized pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (cat in categoriesList) {
                        val isSelected = parentCategory == cat.name
                        val catIcon = CategoryManager.getIconVector(cat.iconKey)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) GlassCoralRed else Color(0xFFF1F5F9))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) GlassCoralRed else Color(0xFF94A3B8),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    parentCategory = cat.name
                                    subCategoryText = ""
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = catIcon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color(0xFF0F172A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.name,
                                color = if (isSelected) Color.White else Color(0xFF0F172A),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Subcategory Suggestions
            val currentCategoryData = categoriesList.find { it.name == parentCategory }
            val currentSubs = currentCategoryData?.subCategories ?: emptyList()

            if (currentSubs.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Types & suggestions pour \"$parentCategory\" :",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A)
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (sub in currentSubs) {
                            val isSelected = subCategoryText == sub
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GlassCoralRedBg else Color(0xFFF1F5F9))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) GlassCoralRed else Color(0xFF94A3B8),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        subCategoryText = sub
                                        if (designationText.isBlank()) designationText = sub
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = sub,
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) GlassCoralRed else Color(0xFF0F172A),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Subcategory free input
            StyledDialogTextField(
                value = subCategoryText,
                onValueChange = { subCategoryText = it },
                label = "Précision sous-catégorie (optionnel)",
                placeholder = "ex: Volailles fraîches",
                accentColor = GlassCoralRed
            )

            // Recipient
            StyledDialogTextField(
                value = recipientText,
                onValueChange = { recipientText = it },
                label = "Bénéficiaire / Tiers payé (optionnel)",
                placeholder = "ex: Bassem, Fournisseur SARL...",
                accentColor = GlassCoralRed
            )
        }
    }
}

/**
 * Dialog 3: Clôture & Pointage Physique (Close Cash & Count)
 */
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

    // Denominations in DA
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

    GlassmorphicDialogContainer(
        onDismissRequest = onDismiss,
        accentColor = GlassEmeraldGreen,
        icon = Icons.Default.Lock,
        title = "Clôture & Pointage Physique",
        subtitle = "Rapprochement des espèces constatées en caisse",
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569)),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1))))
            ) {
                Text("Annuler", fontWeight = FontWeight.Medium)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(currentCountedAmount, closingTimeText.trim())
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassEmeraldGreen)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Valider la clôture", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Expected Theoretical Balance Highlight Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SymphonixLightBlue)
                    .border(1.dp, SymphonixBlue.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Solde Théorique attendu :",
                            style = MaterialTheme.typography.bodySmall,
                            color = SymphonixDeepBlue
                        )
                        Text(
                            text = CashPdfGenerator.formatAmount(theoreticalBalance, currency),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.3.sp
                            ),
                            color = SymphonixDeepBlue
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "En caisse",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SymphonixBlue
                        )
                    }
                }
            }

            // Closing Time Field
            StyledDialogTextField(
                value = closingTimeText,
                onValueChange = { closingTimeText = it },
                label = "Heure du pointage physique",
                leadingIcon = Icons.Default.AccessTime,
                accentColor = GlassEmeraldGreen
            )

            // Mode Selector Switcher (Direct vs Denomination)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isDenominationMode) Color.White else Color.Transparent)
                        .clickable { isDenominationMode = false }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Saisie directe",
                        color = if (!isDenominationMode) SymphonixDeepBlue else Color(0xFF1E293B),
                        fontWeight = if (!isDenominationMode) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 12.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDenominationMode) Color.White else Color.Transparent)
                        .clickable { isDenominationMode = true }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Comptage coupures",
                        color = if (isDenominationMode) SymphonixDeepBlue else Color(0xFF1E293B),
                        fontWeight = if (isDenominationMode) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 12.5.sp
                    )
                }
            }

            // Direct Mode Input
            if (!isDenominationMode) {
                StyledDialogTextField(
                    value = directAmountText,
                    onValueChange = { directAmountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = "Espèces constatées physiquement",
                    placeholder = "ex: 120000",
                    trailingText = currency,
                    keyboardType = KeyboardType.Number,
                    accentColor = GlassEmeraldGreen
                )
            } else {
                // Denomination Counter Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                color = SymphonixDeepBlue,
                                modifier = Modifier.width(75.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (count > 0) denomCounts[denom] = count - 1
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Diminuer", modifier = Modifier.size(16.dp))
                                }

                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SymphonixDeepBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                IconButton(
                                    onClick = {
                                        denomCounts[denom] = count + 1
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Augmenter", modifier = Modifier.size(16.dp))
                                }
                            }

                            Text(
                                text = CashPdfGenerator.formatAmount((count * denom).toDouble(), currency),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                        }
                    }
                }
            }

            // Live Reconciliation Summary Badge
            val statusBg = if (isBalanced) GlassEmeraldGreenBg else if (discrepancy > 0) Color(0xFFFEF3C7) else Color(0xFFFFE4E6)
            val statusBorder = if (isBalanced) GlassEmeraldGreen else if (discrepancy > 0) Color(0xFFD97706) else SymphonixRedExpense
            val statusTextColor = if (isBalanced) GlassEmeraldGreen else if (discrepancy > 0) Color(0xFFB45309) else SymphonixRedExpense

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(statusBg)
                    .border(1.dp, statusBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total espèces dénombrées :",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = statusTextColor
                    )
                    Text(
                        text = CashPdfGenerator.formatAmount(currentCountedAmount, currency),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = statusTextColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBalanced) "Concordance :" else if (discrepancy > 0) "Excédent de caisse :" else "Manquant de caisse :",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusTextColor
                    )
                    Text(
                        text = if (isBalanced) "0 $currency (100% Exacte)" else CashPdfGenerator.formatAmount(discrepancy, currency),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = statusTextColor
                    )
                }
            }
        }
    }
}

/**
 * Dialog 4: Ouvrir une Nouvelle Session de Caisse (New Session)
 */
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
    var sourceText by remember { mutableStateOf("Dotation de caisse / Report veille") }
    var establishmentText by remember { mutableStateOf("SYMPHONIX") }
    var responsibleText by remember { mutableStateOf("Responsable de caisse") }

    val fund = initialFundText.toDoubleOrNull() ?: -1.0
    val isValid = fund >= 0

    GlassmorphicDialogContainer(
        onDismissRequest = onDismiss,
        accentColor = SymphonixDeepBlue,
        icon = Icons.Default.PlayArrow,
        title = "Ouvrir une Session",
        subtitle = "Initialiser le fond de roulement de caisse pour la journée",
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569)),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1))))
            ) {
                Text("Annuler", fontWeight = FontWeight.Medium)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(fund, sourceText.trim(), establishmentText.trim(), responsibleText.trim())
                    }
                },
                enabled = isValid,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SymphonixDeepBlue,
                    disabledContainerColor = Color(0xFF94A3B8)
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ouvrir la caisse", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StyledDialogTextField(
                value = initialFundText,
                onValueChange = { initialFundText = it.filter { c -> c.isDigit() || c == '.' } },
                label = "Fond de roulement initial",
                placeholder = "ex: 150000",
                trailingText = currency,
                keyboardType = KeyboardType.Number,
                accentColor = SymphonixDeepBlue
            )

            StyledDialogTextField(
                value = sourceText,
                onValueChange = { sourceText = it },
                label = "Source de la dotation",
                placeholder = "ex: Dotation de caisse / Report veille",
                accentColor = SymphonixDeepBlue
            )

            StyledDialogTextField(
                value = establishmentText,
                onValueChange = { establishmentText = it },
                label = "Raison sociale / Établissement",
                placeholder = "ex: SYMPHONIX",
                accentColor = SymphonixDeepBlue
            )

            StyledDialogTextField(
                value = responsibleText,
                onValueChange = { responsibleText = it },
                label = "Nom du responsable de caisse",
                placeholder = "ex: Bassem",
                accentColor = SymphonixDeepBlue
            )
        }
    }
}
