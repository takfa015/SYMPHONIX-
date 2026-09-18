package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
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

        // Section 4: Charte Graphique Officielle SYMPHONIX
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
}
