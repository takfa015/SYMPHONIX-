package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SessionWithDetails
import com.example.ui.components.WaterDropCard
import com.example.ui.theme.GlassBorderTop
import com.example.ui.theme.GlassCoralRed
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
import com.example.util.BackupData
import com.example.util.CashBackupManager
import com.example.util.RestoreMode
import com.example.util.RestoreResult
import com.example.util.SecurityManager

@Composable
fun ParametresScreen(
    sessionDetails: SessionWithDetails?,
    onSaveSettings: (
        establishmentName: String,
        establishmentSubTitle: String,
        responsibleName: String,
        managerName: String,
        currency: String,
        initialFund: Double
    ) -> Unit,
    onExportPdf: () -> Unit = {},
    onExportBackup: ((onReady: (String, android.content.Intent) -> Unit) -> Unit)? = null,
    onParseBackup: ((String) -> BackupData?)? = null,
    onRestoreBackup: ((BackupData, RestoreMode, (RestoreResult) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentSession = sessionDetails?.session
    val context = LocalContext.current

    var establishmentName by remember(currentSession) {
        mutableStateOf(currentSession?.establishmentName ?: "SYMPHONIX")
    }
    var establishmentSubTitle by remember(currentSession) {
        mutableStateOf(currentSession?.establishmentSubTitle ?: "Finance & Gestion de Caisse")
    }
    var responsibleName by remember(currentSession) {
        mutableStateOf(currentSession?.responsibleName ?: "Responsable de Caisse")
    }
    var managerName by remember(currentSession) {
        mutableStateOf(currentSession?.managerName ?: "Direction Générale")
    }
    var currency by remember(currentSession) {
        mutableStateOf(currentSession?.currency ?: "DA")
    }
    var initialFundText by remember(currentSession) {
        mutableStateOf((currentSession?.initialFund ?: 150000.0).toLong().toString())
    }

    // Advanced configurable options
    var printSignatures by remember { mutableStateOf(true) }
    var highPrecisionCents by remember { mutableStateOf(true) }
    var autoWatermark by remember { mutableStateOf(true) }
    var selectedReportLanguage by remember { mutableStateOf("Français (Officiel)") }
    var soundFeedback by remember { mutableStateOf(true) }
    var showResetConfirmation by remember { mutableStateOf(false) }

    // PIN Security state
    var isPinSecurityActive by remember { mutableStateOf(SecurityManager.isSecurityEnabled(context)) }
    var hasConfiguredPin by remember { mutableStateOf(SecurityManager.isPinConfigured(context)) }
    var showPinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var pinDialogError by remember { mutableStateOf<String?>(null) }

    // Backup & Restore state
    var showRestoreDialog by remember { mutableStateOf(false) }
    var pendingBackupData by remember { mutableStateOf<BackupData?>(null) }
    var restoreMode by remember { mutableStateOf(RestoreMode.REPLACE) }
    var lastExportedJson by remember { mutableStateOf<String?>(null) }
    var showUpdateHelpDialog by remember { mutableStateOf(false) }

    // File pickers
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val jsonText = CashBackupManager.readFromUri(context, uri)
            if (!jsonText.isNullOrBlank()) {
                val parsed = onParseBackup?.invoke(jsonText) ?: try {
                    CashBackupManager.parseBackup(jsonText)
                } catch (e: Exception) {
                    null
                }
                if (parsed != null) {
                    pendingBackupData = parsed
                    showRestoreDialog = true
                } else {
                    Toast.makeText(context, "Fichier de sauvegarde invalide ou corrompu", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Impossible de lire le fichier sélectionné", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && lastExportedJson != null) {
            val success = CashBackupManager.writeToUri(context, uri, lastExportedJson!!)
            if (success) {
                Toast.makeText(context, "Sauvegarde enregistrée avec succès !", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val currencyPresets = listOf("DA", "€", "$", "DZD", "CHF", "MAD")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header Droplet with Symphonix Fox Brand
            WaterDropCard(
                accentGlow = SymphonixBlue,
                containerColor = Color(0x99FFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .border(1.2.dp, SymphonixBlue.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_symphonix_fox),
                                contentDescription = "Symphonix Mascot",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Paramètres & Configuration",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SymphonixDeepBlue
                            )
                            Text(
                                text = "Identité, impression PDF & système SYMPHONIX",
                                style = MaterialTheme.typography.bodySmall,
                                color = SymphonixBlue
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SymphonixLightBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = SymphonixBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Section 1: Identité Commerciale & Établissement
        item {
            WaterDropCard(
                containerColor = Color(0x80FFFFFF)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = SymphonixBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Identité Commerciale & Établissement",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GlassTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A),
                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.90f),
                    focusedBorderColor = SymphonixBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedLabelColor = SymphonixBlue,
                    unfocusedLabelColor = Color(0xFF64748B)
                )

                OutlinedTextField(
                    value = establishmentName,
                    onValueChange = { establishmentName = it },
                    label = { Text("Nom de l'établissement") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = establishmentSubTitle,
                    onValueChange = { establishmentSubTitle = it },
                    label = { Text("Sous-titre / Salle / Rayon") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = responsibleName,
                        onValueChange = { responsibleName = it },
                        label = { Text("Responsable caisse") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = managerName,
                        onValueChange = { managerName = it },
                        label = { Text("Direction / Gérant") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Devise principale de caisse :",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = GlassTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Quick Currency Selector Pills (Horizontally scrollable to prevent wrapping)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currencyPresets.forEach { preset ->
                        val isSelected = currency.equals(preset, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { currency = preset },
                            label = { Text(preset, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SymphonixLightBlue,
                                selectedLabelColor = SymphonixBlue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = initialFundText,
                    onValueChange = { initialFundText = it },
                    label = { Text("Fond de roulement initial ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val fund = initialFundText.toDoubleOrNull() ?: (currentSession?.initialFund ?: 150000.0)
                        onSaveSettings(
                            establishmentName,
                            establishmentSubTitle,
                            responsibleName,
                            managerName,
                            currency,
                            fund
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SymphonixBlue),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enregistrer les modifications", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 2: Options de Génération & Impression PDF
        item {
            WaterDropCard(
                accentGlow = GlassWaterBlue,
                containerColor = Color(0x80FFFFFF)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = SymphonixBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Options d'Impression & Format PDF",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GlassTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle 1: Cadre des signatures
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cadres signatures & cachet officiel", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = GlassTextPrimary)
                        Text("Inclut les zones 'Établi par' et 'Vérifié par' sur l'A4", style = MaterialTheme.typography.bodySmall, color = GlassTextSecondary)
                    }
                    Switch(
                        checked = printSignatures,
                        onCheckedChange = { printSignatures = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SymphonixBlue, checkedTrackColor = SymphonixLightBlue)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0x1F000000))

                // Toggle 2: Centimes & Précision
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Affichage haute précision (0,00)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = GlassTextPrimary)
                        Text("Formate tous les montants et pourcentages avec deux décimales", style = MaterialTheme.typography.bodySmall, color = GlassTextSecondary)
                    }
                    Switch(
                        checked = highPrecisionCents,
                        onCheckedChange = { highPrecisionCents = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SymphonixBlue, checkedTrackColor = SymphonixLightBlue)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0x1F000000))

                // Toggle 3: Filigrane de sécurité SYMPHONIX
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("En-tête & filigrane institutionnel", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = GlassTextPrimary)
                        Text("Badge de conformité comptable et identifiant de session", style = MaterialTheme.typography.bodySmall, color = GlassTextSecondary)
                    }
                    Switch(
                        checked = autoWatermark,
                        onCheckedChange = { autoWatermark = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SymphonixBlue, checkedTrackColor = SymphonixLightBlue)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = { onExportPdf() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = SymphonixBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tester la génération du PDF", color = SymphonixBlue, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Section 3: Préférences de l'Application & Ergonomie
        item {
            WaterDropCard(
                accentGlow = GlassEmeraldGreen,
                containerColor = Color(0x80FFFFFF)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = GlassEmeraldGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ergonomie & Expérience Utilisateur",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GlassTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Retour haptique & confirmations", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = GlassTextPrimary)
                        Text("Vibrations douces et sons discrets lors de l'enregistrement", style = MaterialTheme.typography.bodySmall, color = GlassTextSecondary)
                    }
                    Switch(
                        checked = soundFeedback,
                        onCheckedChange = { soundFeedback = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GlassEmeraldGreen, checkedTrackColor = GlassEmeraldGreenBg)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Langue du rapport officiel :",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = GlassTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Français (Officiel)", "Arabe (Bilingue)").forEach { lang ->
                        val isSelected = selectedReportLanguage == lang
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedReportLanguage = lang
                                Toast.makeText(context, "Langue sélectionnée: $lang", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text(lang) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GlassEmeraldGreenBg,
                                selectedLabelColor = GlassEmeraldGreen
                            )
                        )
                    }
                }
            }
        }

        // Section 4: Sécurité & Code d'Accès
        item {
            WaterDropCard(
                accentGlow = SymphonixBlue,
                containerColor = Color(0x90FFFFFF)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = SymphonixBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sécurité & Contrôle d'Accès",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = SymphonixDeepBlue
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Verrouillage par code PIN",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = GlassTextPrimary
                        )
                        Text(
                            text = if (hasConfiguredPin) "Code PIN configuré et actif au démarrage" else "Exige un mot de passe ou code PIN pour ouvrir l'application",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassTextSecondary
                        )
                    }
                    Switch(
                        checked = isPinSecurityActive,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (!hasConfiguredPin) {
                                    showPinDialog = true
                                } else {
                                    SecurityManager.setSecurityEnabled(context, true)
                                    isPinSecurityActive = true
                                    Toast.makeText(context, "Verrouillage par code PIN activé", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                SecurityManager.setSecurityEnabled(context, false)
                                isPinSecurityActive = false
                                Toast.makeText(context, "Verrouillage désactivé", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SymphonixBlue,
                            checkedTrackColor = SymphonixLightBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            newPinInput = ""
                            confirmPinInput = ""
                            pinDialogError = null
                            showPinDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = SymphonixBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasConfiguredPin) "Modifier le code PIN" else "Définir un code PIN",
                            color = SymphonixBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (hasConfiguredPin) {
                        TextButton(
                            onClick = {
                                SecurityManager.removePin(context)
                                hasConfiguredPin = false
                                isPinSecurityActive = false
                                Toast.makeText(context, "Code PIN supprimé", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = "Supprimer",
                                color = GlassCoralRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Section 5: Sauvegarde & Restauration (Anti-Perte de données lors des mises à jour)
        item {
            WaterDropCard(
                accentGlow = GlassEmeraldGreen,
                containerColor = Color(0x95FFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = GlassEmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sauvegarde & Restauration (JSON)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = SymphonixDeepBlue
                        )
                    }

                    TextButton(
                        onClick = { showUpdateHelpDialog = true }
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Aide", tint = SymphonixBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pourquoi ?", fontSize = 11.sp, color = SymphonixBlue)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Protégez vos données de caisse avant chaque mise à jour ou changement d'appareil. L'export génère un fichier JSON complet et réimportable.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                    color = GlassTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Export Button
                    Button(
                        onClick = {
                            if (onExportBackup != null) {
                                onExportBackup { jsonString, shareIntent ->
                                    lastExportedJson = jsonString
                                    try {
                                        context.startActivity(shareIntent)
                                    } catch (_: Exception) {
                                        // Fallback to direct document creation
                                        val dateFmt = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault()).format(java.util.Date())
                                        createDocumentLauncher.launch("symphonix_backup_$dateFmt.json")
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Préparation de la sauvegarde...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SymphonixBlue)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sauvegarder", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Restore Button
                    OutlinedButton(
                        onClick = {
                            openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, GlassEmeraldGreen)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = GlassEmeraldGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restaurer", color = GlassEmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 6: Charte Graphique Officielle SYMPHONIX
        item {
            WaterDropCard(
                accentGlow = SymphonixBlue,
                containerColor = Color(0x80FFFFFF)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = SymphonixBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Charte Graphique Officielle SYMPHONIX",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = SymphonixDeepBlue
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• Slogan : « Maîtrisez. Optimisez. Évoluez. »\n" +
                           "• Couleurs : Symphonix Blue (#146BFF), Deep Blue (#0B2E73), Light Blue (#EAF2FF)\n" +
                           "• Emblème : Renard géométrique origami à facettes vectorielles vives\n" +
                           "• Typographie : Space Grotesk (Titres) & Inter (Interface / Données)\n" +
                           "• Style : Fluide, Goutte d'eau translucide, Performant et Précis",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 19.sp),
                    color = GlassTextSecondary
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    // PIN Configuration Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = SymphonixBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasConfiguredPin) "Modifier le code PIN" else "Nouveau code PIN",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choisissez un code secret de 4 à 6 chiffres pour sécuriser l'accès à la caisse :",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassTextSecondary
                    )

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPinInput = it },
                        label = { Text("Code PIN (4-6 chiffres)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SymphonixBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) confirmPinInput = it },
                        label = { Text("Confirmer le code PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SymphonixBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    if (pinDialogError != null) {
                        Text(
                            text = pinDialogError!!,
                            color = GlassCoralRed,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length < 4) {
                            pinDialogError = "Le code doit comporter au moins 4 chiffres."
                        } else if (newPinInput != confirmPinInput) {
                            pinDialogError = "Les deux codes ne correspondent pas."
                        } else {
                            SecurityManager.savePin(context, newPinInput)
                            hasConfiguredPin = true
                            isPinSecurityActive = true
                            showPinDialog = false
                            Toast.makeText(context, "Code PIN enregistré et activé !", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SymphonixBlue)
                ) {
                    Text("Valider", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Restore Confirmation Dialog
    if (showRestoreDialog && pendingBackupData != null) {
        val data = pendingBackupData!!
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = GlassEmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmer la restauration", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Aperçu de la sauvegarde sélectionnée :", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("• Date d'export : ${data.summary.exportedAt}", fontSize = 12.sp, color = GlassTextSecondary)
                    Text("• Sessions de caisse : ${data.summary.sessionsCount}", fontSize = 12.sp, color = GlassTextSecondary)
                    Text("• Décaissements : ${data.summary.disbursementsCount}", fontSize = 12.sp, color = GlassTextSecondary)
                    Text("• Total des dépenses : ${data.summary.totalDisbursed} DA", fontSize = 12.sp, color = GlassTextSecondary)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("Mode de restauration :", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { restoreMode = RestoreMode.REPLACE }
                    ) {
                        RadioButton(
                            selected = restoreMode == RestoreMode.REPLACE,
                            onClick = { restoreMode = RestoreMode.REPLACE }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Remplacer tout (Recommandé)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Écrase la base actuelle et remet exactement cette sauvegarde", fontSize = 11.sp, color = GlassTextSecondary)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { restoreMode = RestoreMode.MERGE }
                    ) {
                        RadioButton(
                            selected = restoreMode == RestoreMode.MERGE,
                            onClick = { restoreMode = RestoreMode.MERGE }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Fusionner les données", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Ajoute les sessions sans supprimer vos sessions locales", fontSize = 11.sp, color = GlassTextSecondary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreBackup?.invoke(data, restoreMode) { result ->
                            showRestoreDialog = false
                            pendingBackupData = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassEmeraldGreen)
                ) {
                    Text("Restaurer maintenant", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Update & Data Loss Info Dialog
    if (showUpdateHelpDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = SymphonixBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mises à jour & Sécurité des Données", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pourquoi Android demande parfois de désinstaller l'ancienne version ?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SymphonixDeepBlue
                    )
                    Text(
                        text = "1. Signature APK : Si deux APK ont été compilés avec des clés différentes, Android bloque la mise à jour pour des raisons de sécurité du système.\n\n" +
                               "2. Numéro de version (versionCode) : Chaque nouvelle mise à jour doit avoir un versionCode supérieur à l'actuel.\n\n" +
                               "3. Solution infaillible : En utilisant le bouton « Sauvegarder », vous enregistrez un fichier JSON (sur Google Drive, WhatsApp ou vos Fichiers). En cas de mise à jour ou réinstallation, cliquez simplement sur « Restaurer » pour tout récupérer en 1 seconde !",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = GlassTextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showUpdateHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SymphonixBlue)
                ) {
                    Text("Compris", color = Color.White)
                }
            }
        )
    }
}
