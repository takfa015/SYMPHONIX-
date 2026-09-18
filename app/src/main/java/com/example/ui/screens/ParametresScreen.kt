package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SessionWithDetails
import com.example.ui.components.WaterDropCard
import com.example.ui.theme.GlassEmeraldGreen
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
    modifier: Modifier = Modifier
) {
    val currentSession = sessionDetails?.session

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
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.2.dp, SymphonixBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_symphonix_fox),
                                contentDescription = "Symphonix Mascot",
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Paramètres & Identité",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SymphonixDeepBlue
                            )
                            Text(
                                text = "Charte graphique SYMPHONIX & Données PDF",
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
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = SymphonixBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Form Fields in Water Drop Card
        item {
            WaterDropCard(
                containerColor = Color(0x73FFFFFF)
            ) {
                Text(
                    text = "Identité Commerciale & Établissement",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = GlassTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = establishmentName,
                    onValueChange = { establishmentName = it },
                    label = { Text("Nom de l'établissement") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0x80FFFFFF),
                        unfocusedContainerColor = Color(0x40FFFFFF),
                        focusedBorderColor = SymphonixBlue,
                        unfocusedBorderColor = Color(0x33CBD5E1)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = establishmentSubTitle,
                    onValueChange = { establishmentSubTitle = it },
                    label = { Text("Sous-titre / Salle / Rayon") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0x80FFFFFF),
                        unfocusedContainerColor = Color(0x40FFFFFF),
                        focusedBorderColor = SymphonixBlue,
                        unfocusedBorderColor = Color(0x33CBD5E1)
                    )
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x80FFFFFF),
                            unfocusedContainerColor = Color(0x40FFFFFF),
                            focusedBorderColor = SymphonixBlue,
                            unfocusedBorderColor = Color(0x33CBD5E1)
                        )
                    )

                    OutlinedTextField(
                        value = managerName,
                        onValueChange = { managerName = it },
                        label = { Text("Direction / Gérant") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x80FFFFFF),
                            unfocusedContainerColor = Color(0x40FFFFFF),
                            focusedBorderColor = SymphonixBlue,
                            unfocusedBorderColor = Color(0x33CBD5E1)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("Devise (DA, €, $)") },
                        modifier = Modifier.weight(0.8f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x80FFFFFF),
                            unfocusedContainerColor = Color(0x40FFFFFF),
                            focusedBorderColor = SymphonixBlue,
                            unfocusedBorderColor = Color(0x33CBD5E1)
                        )
                    )

                    OutlinedTextField(
                        value = initialFundText,
                        onValueChange = { initialFundText = it },
                        label = { Text("Fond initial ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x80FFFFFF),
                            unfocusedContainerColor = Color(0x40FFFFFF),
                            focusedBorderColor = SymphonixBlue,
                            unfocusedBorderColor = Color(0x33CBD5E1)
                        )
                    )
                }

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

        // Charte Graphique SYMPHONIX Card
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
                        text = "Charte Graphique SYMPHONIX",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = SymphonixDeepBlue
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• Slogan : « Maîtrisez. Optimisez. Évoluez. »\n" +
                           "• Couleurs : Symphonix Blue (#146BFF), Deep Blue (#0B2E73), Light Blue (#EAF2FF)\n" +
                           "• Typographie : Space Grotesk (Titres) & Inter (Interface / Données)\n" +
                           "• Style : Géométrique, Rapide, Précis et Performant",
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
