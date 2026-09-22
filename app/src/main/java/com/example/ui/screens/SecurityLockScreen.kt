package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.ui.theme.GlassCoralRed
import com.example.ui.theme.GlassEmeraldGreen
import com.example.ui.theme.GlassPureWhite
import com.example.ui.theme.SymphonixBlue
import com.example.ui.theme.SymphonixDeepBlue
import com.example.ui.theme.SymphonixLightBlue
import com.example.util.BiometricHelper
import com.example.util.SecurityManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SecurityLockScreen(
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val coroutineScope = rememberCoroutineScope()

    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val shakeOffset = remember { Animatable(0f) }

    // Détection d'ancien hash SHA-256 ou PIN < 6 chiffres nécessitant migration forcée
    var isLegacyMigrationMode by remember {
        mutableStateOf(SecurityManager.isLegacyHashDetected(context))
    }
    var newMigrationPin by remember { mutableStateOf("") }
    var confirmMigrationPin by remember { mutableStateOf("") }
    var migrationStep by remember { mutableStateOf(1) } // 1: nouveau PIN, 2: confirmation

    // Gestion du compte à rebours anti brute-force
    var remainingLockSeconds by remember {
        mutableLongStateOf(SecurityManager.getRemainingLockTimeSeconds(context))
    }

    LaunchedEffect(remainingLockSeconds) {
        if (remainingLockSeconds > 0) {
            delay(1000L)
            remainingLockSeconds = SecurityManager.getRemainingLockTimeSeconds(context)
        }
    }

    // Déclenchement automatique de BiometricPrompt si activé et disponible
    LaunchedEffect(Unit) {
        if (!isLegacyMigrationMode && remainingLockSeconds == 0L && activity != null && SecurityManager.isBiometricAvailableAndEnabled(context)) {
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = "Déverrouillage SYMPHONIX",
                subtitle = "Authentifiez-vous par empreinte digitale pour accéder à la caisse",
                negativeButtonText = "Utiliser le PIN",
                onSuccess = { onUnlocked() },
                onError = { _, _ -> /* Repli vers saisie PIN */ },
                onFailed = { /* Feedback biométrique standard */ }
            )
        }
    }

    fun triggerBiometricManual() {
        if (activity != null && SecurityManager.isBiometricAvailableAndEnabled(context)) {
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = "Déverrouillage SYMPHONIX",
                subtitle = "Authentifiez-vous par empreinte digitale pour accéder à la caisse",
                negativeButtonText = "Utiliser le PIN",
                onSuccess = { onUnlocked() },
                onError = { _, _ -> },
                onFailed = { }
            )
        }
    }

    fun onDigitPress(digit: String) {
        if (remainingLockSeconds > 0) return

        if (isLegacyMigrationMode) {
            if (migrationStep == 1) {
                if (newMigrationPin.length < 8) {
                    newMigrationPin += digit
                    errorMessage = null
                }
            } else {
                if (confirmMigrationPin.length < 8) {
                    confirmMigrationPin += digit
                    errorMessage = null
                }
            }
            return
        }

        if (enteredPin.length < 10) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            errorMessage = null

            // Vérification automatique si longueur minimum atteinte
            if (newPin.length >= SecurityManager.MIN_PIN_LENGTH) {
                if (SecurityManager.verifyPin(context, newPin)) {
                    onUnlocked()
                } else if (newPin.length >= 8) {
                    coroutineScope.launch {
                        remainingLockSeconds = SecurityManager.getRemainingLockTimeSeconds(context)
                        if (remainingLockSeconds > 0) {
                            errorMessage = "Trop d'échecs. Caisse verrouillée pendant $remainingLockSeconds s."
                        } else {
                            val fails = SecurityManager.getFailedAttempts(context)
                            errorMessage = "Code PIN incorrect (échec $fails/5)."
                        }
                        shakeOffset.animateTo(20f, tween(50))
                        shakeOffset.animateTo(-20f, tween(50))
                        shakeOffset.animateTo(10f, tween(50))
                        shakeOffset.animateTo(-10f, tween(50))
                        shakeOffset.animateTo(0f, tween(50))
                        enteredPin = ""
                    }
                }
            }
        }
    }

    fun onDeletePress() {
        if (isLegacyMigrationMode) {
            if (migrationStep == 1 && newMigrationPin.isNotEmpty()) {
                newMigrationPin = newMigrationPin.dropLast(1)
                errorMessage = null
            } else if (migrationStep == 2 && confirmMigrationPin.isNotEmpty()) {
                confirmMigrationPin = confirmMigrationPin.dropLast(1)
                errorMessage = null
            }
            return
        }

        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    fun onValidateManual() {
        if (remainingLockSeconds > 0) return

        if (isLegacyMigrationMode) {
            if (migrationStep == 1) {
                if (newMigrationPin.length < SecurityManager.MIN_PIN_LENGTH) {
                    errorMessage = "Le nouveau code doit comporter au moins ${SecurityManager.MIN_PIN_LENGTH} chiffres."
                    return
                }
                migrationStep = 2
                errorMessage = null
            } else {
                if (newMigrationPin != confirmMigrationPin) {
                    errorMessage = "Les deux codes ne correspondent pas. Réessayez."
                    confirmMigrationPin = ""
                    return
                }
                val saved = SecurityManager.savePin(context, newMigrationPin)
                if (saved) {
                    isLegacyMigrationMode = false
                    onUnlocked()
                } else {
                    errorMessage = "Erreur lors du chiffrement du PIN dans le Keystore."
                }
            }
            return
        }

        if (enteredPin.length < SecurityManager.MIN_PIN_LENGTH) {
            errorMessage = "Le code PIN doit comporter au moins ${SecurityManager.MIN_PIN_LENGTH} chiffres."
            return
        }

        if (SecurityManager.verifyPin(context, enteredPin)) {
            onUnlocked()
        } else {
            coroutineScope.launch {
                remainingLockSeconds = SecurityManager.getRemainingLockTimeSeconds(context)
                if (remainingLockSeconds > 0) {
                    errorMessage = "Trop d'échecs. Caisse bloquée pendant $remainingLockSeconds s."
                } else {
                    val fails = SecurityManager.getFailedAttempts(context)
                    errorMessage = "Code PIN incorrect (échec $fails/5)."
                }
                shakeOffset.animateTo(20f, tween(50))
                shakeOffset.animateTo(-20f, tween(50))
                shakeOffset.animateTo(10f, tween(50))
                shakeOffset.animateTo(-10f, tween(50))
                shakeOffset.animateTo(0f, tween(50))
                enteredPin = ""
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF7FAFD),
                        Color(0xFFEDF5FC)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // SYMPHONIX Brand Icon
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(GlassPureWhite)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(SymphonixBlue, SymphonixLightBlue, Color.White)
                        ),
                        shape = RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_symphonix_fox),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = SymphonixBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SYMPHONIX SÉCURITÉ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        fontSize = 15.sp
                    ),
                    color = SymphonixDeepBlue
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isLegacyMigrationMode) {
                Text(
                    text = if (migrationStep == 1) "Mise à niveau : Nouveau code PIN (6 chiffres min)" else "Confirmez votre nouveau code PIN",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SymphonixBlue
                )
                Text(
                    text = "Chiffrement matériel Keystore & PBKDF2 obligatoire",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            } else {
                Text(
                    text = "Entrez votre code d'accès à 6 chiffres",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Compte à rebours anti brute-force si verrouillé
            if (remainingLockSeconds > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tentatives excessives dépassées",
                            fontWeight = FontWeight.Bold,
                            color = GlassCoralRed,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Accès verrouillé pendant : ${remainingLockSeconds}s",
                            fontWeight = FontWeight.ExtraBold,
                            color = GlassCoralRed,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                // Indicateurs visuels du code PIN (6 ronds minimum)
                val currentPin = if (isLegacyMigrationMode) {
                    if (migrationStep == 1) newMigrationPin else confirmMigrationPin
                } else enteredPin

                Row(
                    modifier = Modifier
                        .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val displayCount = maxOf(SecurityManager.MIN_PIN_LENGTH, currentPin.length)
                    for (i in 0 until displayCount) {
                        val isFilled = i < currentPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) SymphonixBlue else Color(0xFFCBD5E1)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isFilled) SymphonixDeepBlue else Color(0xFF94A3B8),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            // Message d'erreur
            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = GlassCoralRed,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Pavé numérique
            val biometricEnabled = SecurityManager.isBiometricAvailableAndEnabled(context) && !isLegacyMigrationMode
            val bottomActionKey = if (biometricEnabled) "BIO" else ""

            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf(bottomActionKey, "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in rows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (key in row) {
                            KeypadButton(
                                text = key,
                                enabled = remainingLockSeconds == 0L,
                                onClick = {
                                    when (key) {
                                        "DEL" -> onDeletePress()
                                        "BIO" -> triggerBiometricManual()
                                        "" -> { /* espace vide */ }
                                        else -> onDigitPress(key)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton de validation manuelle
            Button(
                onClick = { onValidateManual() },
                enabled = remainingLockSeconds == 0L && (
                    (isLegacyMigrationMode && ((migrationStep == 1 && newMigrationPin.length >= SecurityManager.MIN_PIN_LENGTH) || (migrationStep == 2 && confirmMigrationPin.length >= SecurityManager.MIN_PIN_LENGTH))) ||
                    (!isLegacyMigrationMode && enteredPin.length >= SecurityManager.MIN_PIN_LENGTH)
                ),
                colors = ButtonDefaults.buttonColors(containerColor = SymphonixBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLegacyMigrationMode && migrationStep == 1) "Suivant" else "Déverrouiller",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    if (text.isEmpty()) {
        Box(modifier = Modifier.size(68.dp))
        return
    }

    val isAction = text == "DEL" || text == "BIO"
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(
                if (!enabled) Color(0xFFF1F5F9)
                else if (isAction) Color(0xFFE2E8F0)
                else GlassPureWhite
            )
            .border(
                width = 1.dp,
                color = if (!enabled) Color(0xFFE2E8F0) else Color(0xFFCBD5E1),
                shape = CircleShape
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text == "DEL") {
            Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Effacer",
                tint = if (enabled) SymphonixDeepBlue else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
        } else if (text == "BIO") {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Biométrie",
                tint = if (enabled) SymphonixBlue else Color.Gray,
                modifier = Modifier.size(26.dp)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = if (enabled) SymphonixDeepBlue else Color.Gray
            )
        }
    }
}
