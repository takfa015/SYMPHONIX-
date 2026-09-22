package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.GlassCoralRed
import com.example.ui.theme.GlassPureWhite
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.SymphonixBlue
import com.example.ui.theme.SymphonixDeepBlue
import com.example.util.BiometricHelper
import com.example.util.SecurityManager
import kotlinx.coroutines.delay

/**
 * Dialogue de ré-authentification obligatoire pour les actions sensibles :
 * - Réouverture de session
 * - Restauration REPLACE de base de données
 * - Exportation de données
 * - Suppression définitive de session
 * - Désactivation ou modification du code PIN
 */
@Composable
fun SensitiveActionPinDialog(
    actionTitle: String,
    actionDescription: String,
    isDestructive: Boolean = false,
    onDismiss: () -> Unit,
    onVerified: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    // Si la sécurité par PIN n'est pas activée, l'action est autorisée immédiatement
    if (!SecurityManager.isSecurityEnabled(context)) {
        LaunchedEffect(Unit) {
            onVerified()
        }
        return
    }

    var pinText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPinVisible by remember { mutableStateOf(false) }

    var remainingLockSeconds by remember {
        mutableLongStateOf(SecurityManager.getRemainingLockTimeSeconds(context))
    }

    // Compte à rebours si verrouillage anti brute-force en cours
    LaunchedEffect(remainingLockSeconds) {
        if (remainingLockSeconds > 0) {
            delay(1000L)
            remainingLockSeconds = SecurityManager.getRemainingLockTimeSeconds(context)
        }
    }

    fun triggerBiometric() {
        if (activity != null && SecurityManager.isBiometricAvailableAndEnabled(context)) {
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = actionTitle,
                subtitle = "Vérification requise pour cette opération sensible",
                negativeButtonText = "Saisir le PIN",
                onSuccess = { onVerified() },
                onError = { _, _ -> /* Repli vers la saisie du PIN */ },
                onFailed = { /* Feedback biométrique standard */ }
            )
        }
    }

    // Tente l'authentification biométrique si disponible
    LaunchedEffect(Unit) {
        triggerBiometric()
    }

    fun attemptVerify() {
        if (remainingLockSeconds > 0) {
            errorMessage = "Action bloquée. Attendez la fin du verrouillage ($remainingLockSeconds s)."
            return
        }

        if (pinText.length < SecurityManager.MIN_PIN_LENGTH) {
            errorMessage = "Le code PIN doit comporter au moins ${SecurityManager.MIN_PIN_LENGTH} chiffres."
            return
        }

        if (SecurityManager.verifyPin(context, pinText)) {
            errorMessage = null
            onVerified()
        } else {
            remainingLockSeconds = SecurityManager.getRemainingLockTimeSeconds(context)
            if (remainingLockSeconds > 0) {
                errorMessage = "Trop d'échecs. Verrouillé pendant $remainingLockSeconds secondes."
            } else {
                val fails = SecurityManager.getFailedAttempts(context)
                errorMessage = "Code PIN incorrect (échec $fails/5 avant verrouillage temporaire)."
            }
            pinText = ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDestructive) Color(0xFFFEE2E2) else Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDestructive) Icons.Default.WarningAmber else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isDestructive) GlassCoralRed else SymphonixBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = actionTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GlassTextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = actionDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (remainingLockSeconds > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sécurité anti-intrusion : réessayez dans $remainingLockSeconds s",
                            color = GlassCoralRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 12) {
                                pinText = input
                                errorMessage = null
                            }
                        },
                        label = { Text("Code PIN (${SecurityManager.MIN_PIN_LENGTH} chiffres min)") },
                        singleLine = true,
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPinVisible) "Masquer" else "Afficher",
                                    tint = GlassTextMuted
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDestructive) GlassCoralRed else SymphonixBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = GlassCoralRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (SecurityManager.isBiometricAvailableAndEnabled(context)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = { triggerBiometric() },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp), tint = SymphonixBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Déverrouiller avec empreinte digitale", color = SymphonixBlue, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { attemptVerify() },
                enabled = remainingLockSeconds == 0L && pinText.length >= SecurityManager.MIN_PIN_LENGTH,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) GlassCoralRed else SymphonixBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = GlassTextSecondary)
            }
        },
        containerColor = GlassPureWhite,
        shape = RoundedCornerShape(18.dp)
    )
}
