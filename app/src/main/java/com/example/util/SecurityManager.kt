package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import java.nio.ByteBuffer
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import kotlin.math.max

/**
 * Gestionnaire de sécurité SYMPHONIX :
 * - Hachage PBKDF2WithHmacSHA256 (120 000 itérations, sel 16 octets aléatoire, clé 256 bits)
 * - Comparaison en temps constant via MessageDigest.isEqual
 * - Chiffrement AES-GCM (256 bits) via l'Android Keystore
 * - Protection anti brute-force progressive (30s, 1m, 5m, 15m) persistante
 * - Détection et migration forcée des anciens hashs < 6 chiffres ou SHA-256
 * - Support BiometricPrompt avec repli PIN
 * - Délai d'auto-verrouillage en arrière-plan configurable
 * - Contrôle FLAG_SECURE
 */
object SecurityManager {

    private const val PREFS_NAME = "symphonix_security_prefs"

    // Clés de persistance
    private const val KEY_PIN_ENABLED = "key_pin_enabled"
    private const val KEY_LEGACY_PIN_HASH = "key_pin_hash"
    private const val KEY_ENCRYPTED_AUTH_DATA = "key_encrypted_auth_data"
    private const val KEY_FAILED_ATTEMPTS = "key_failed_attempts"
    private const val KEY_LOCKED_UNTIL_MS = "key_locked_until_ms"
    private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
    private const val KEY_AUTO_LOCK_DELAY_MS = "key_auto_lock_delay_ms"
    private const val KEY_FLAG_SECURE_ENABLED = "key_flag_secure_enabled"
    private const val KEY_LAST_BACKGROUND_MS = "key_last_background_ms"

    // Paramètres cryptographiques PBKDF2
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val PBKDF2_ITERATIONS = 120_000
    private const val SALT_SIZE_BYTES = 16
    private const val KEY_LENGTH_BITS = 256
    private const val HASH_SIZE_BYTES = 32

    // Paramètres Android Keystore AES-GCM
    private const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEYSTORE_ALIAS = "symphonix_aes_master_key_v2"
    private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128

    // Contraintes métier
    const val MIN_PIN_LENGTH = 6
    const val DEFAULT_AUTO_LOCK_DELAY_MS = 60_000L // 1 minute par défaut

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ==========================================
    // GESTION DU STATUT DU PIN
    // ==========================================

    fun isSecurityEnabled(context: Context): Boolean {
        val prefs = getPrefs(context)
        val isEnabled = prefs.getBoolean(KEY_PIN_ENABLED, false)
        return isEnabled && (isPinConfigured(context) || isLegacyHashDetected(context))
    }

    fun isPinConfigured(context: Context): Boolean {
        val prefs = getPrefs(context)
        return !prefs.getString(KEY_ENCRYPTED_AUTH_DATA, null).isNullOrBlank()
    }

    fun isLegacyHashDetected(context: Context): Boolean {
        val prefs = getPrefs(context)
        val legacyHash = prefs.getString(KEY_LEGACY_PIN_HASH, null)
        val newAuthData = prefs.getString(KEY_ENCRYPTED_AUTH_DATA, null)
        return !legacyHash.isNullOrBlank() && newAuthData.isNullOrBlank()
    }

    fun setSecurityEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PIN_ENABLED, enabled).apply()
    }

    // ==========================================
    // SAUVEGARDE & VÉRIFICATION DU PIN
    // ==========================================

    fun savePin(context: Context, pin: String): Boolean {
        if (pin.length < MIN_PIN_LENGTH || !pin.all { it.isDigit() }) {
            return false
        }

        try {
            // 1. Génération d'un sel aléatoire de 16 octets
            val salt = ByteArray(SALT_SIZE_BYTES)
            SecureRandom().nextBytes(salt)

            // 2. Dérivation de clé PBKDF2WithHmacSHA256 (120 000 itérations)
            val hash = derivePbkdf2Hash(pin, salt)

            // 3. Empaqueter sel + hash (16 + 32 = 48 octets)
            val payload = ByteBuffer.allocate(SALT_SIZE_BYTES + HASH_SIZE_BYTES)
                .put(salt)
                .put(hash)
                .array()

            // 4. Chiffrement avec clé AES-GCM de l'Android Keystore
            val encryptedAuthString = encryptWithKeyStore(payload)

            // 5. Stockage persistant et nettoyage des anciens hashs non sécurisés
            getPrefs(context).edit()
                .putString(KEY_ENCRYPTED_AUTH_DATA, encryptedAuthString)
                .remove(KEY_LEGACY_PIN_HASH) // Supprime l'ancien hash SHA-256
                .putBoolean(KEY_PIN_ENABLED, true)
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKED_UNTIL_MS, 0L)
                .apply()

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun verifyPin(context: Context, inputPin: String): Boolean {
        // Si verrouillé par l'anti brute-force, rejette immédiatement
        if (isLockedOut(context)) {
            return false
        }

        // Si ancien hash détecté, refus de vérification (force la recréation)
        if (isLegacyHashDetected(context)) {
            return false
        }

        val encryptedAuthString = getPrefs(context).getString(KEY_ENCRYPTED_AUTH_DATA, null) ?: return false

        try {
            // 1. Déchiffrement via Android Keystore
            val decryptedPayload = decryptWithKeyStore(encryptedAuthString) ?: return false
            if (decryptedPayload.size != (SALT_SIZE_BYTES + HASH_SIZE_BYTES)) {
                return false
            }

            val buffer = ByteBuffer.wrap(decryptedPayload)
            val salt = ByteArray(SALT_SIZE_BYTES)
            val expectedHash = ByteArray(HASH_SIZE_BYTES)
            buffer.get(salt)
            buffer.get(expectedHash)

            // 2. Calcul du hash PBKDF2 avec le sel stocké
            val computedHash = derivePbkdf2Hash(inputPin, salt)

            // 3. Comparaison en temps constant (constant-time)
            val matches = MessageDigest.isEqual(expectedHash, computedHash)

            if (matches) {
                // Réinitialise les compteurs d'échec
                resetFailedAttempts(context)
                return true
            } else {
                // Enregistre l'échec et applique un éventuel verrouillage progressif
                recordFailedAttempt(context)
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun removePin(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_ENCRYPTED_AUTH_DATA)
            .remove(KEY_LEGACY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKED_UNTIL_MS, 0L)
            .apply()
    }

    // ==========================================
    // CRYPTOGRAPHIE PBKDF2
    // ==========================================

    private fun derivePbkdf2Hash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val keyFactory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return keyFactory.generateSecret(spec).encoded
    }

    // ==========================================
    // ANDROID KEYSTORE (AES-GCM 256 bits)
    // ==========================================

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply { load(null) }
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE_PROVIDER)
            val keyGenSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()

            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }

        val entry = keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    private fun encryptWithKeyStore(data: ByteArray): String {
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateMasterKey())
        val iv = cipher.iv // 12 octets pour AES-GCM
        val ciphertext = cipher.doFinal(data)

        val encodedIv = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encodedCiphertext = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        return "$encodedIv:$encodedCiphertext"
    }

    private fun decryptWithKeyStore(encryptedString: String): ByteArray? {
        return try {
            val parts = encryptedString.split(":")
            if (parts.size != 2) return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)

            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateMasterKey(), spec)
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ==========================================
    // PROTECTION ANTI BRUTE-FORCE PROGRESSIVE
    // ==========================================

    fun isLockedOut(context: Context): Boolean {
        val lockedUntil = getPrefs(context).getLong(KEY_LOCKED_UNTIL_MS, 0L)
        return System.currentTimeMillis() < lockedUntil
    }

    fun getRemainingLockTimeSeconds(context: Context): Long {
        val lockedUntil = getPrefs(context).getLong(KEY_LOCKED_UNTIL_MS, 0L)
        val diff = lockedUntil - System.currentTimeMillis()
        return if (diff > 0) max(1L, (diff + 999) / 1000) else 0L
    }

    fun getFailedAttempts(context: Context): Int {
        return getPrefs(context).getInt(KEY_FAILED_ATTEMPTS, 0)
    }

    private fun recordFailedAttempt(context: Context) {
        val prefs = getPrefs(context)
        val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        var lockDurationMs = 0L

        // Verrouillage progressif : après 5 échecs (30s, 1 min, 5 min, 15 min...)
        if (attempts >= 8) {
            lockDurationMs = 15 * 60 * 1000L // 15 minutes
        } else if (attempts == 7) {
            lockDurationMs = 5 * 60 * 1000L  // 5 minutes
        } else if (attempts == 6) {
            lockDurationMs = 60 * 1000L      // 1 minute
        } else if (attempts == 5) {
            lockDurationMs = 30 * 1000L      // 30 secondes
        }

        val lockedUntil = if (lockDurationMs > 0L) System.currentTimeMillis() + lockDurationMs else 0L

        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, attempts)
            .putLong(KEY_LOCKED_UNTIL_MS, lockedUntil)
            .apply()
    }

    private fun resetFailedAttempts(context: Context) {
        getPrefs(context).edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKED_UNTIL_MS, 0L)
            .apply()
    }

    // ==========================================
    // BIOMÉTRIE (BiometricPrompt)
    // ==========================================

    fun isBiometricSupported(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun isBiometricEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun isBiometricAvailableAndEnabled(context: Context): Boolean {
        return isSecurityEnabled(context) && isBiometricEnabled(context) && isBiometricSupported(context)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    // ==========================================
    // VERROUILLAGE ARRIÈRE-PLAN (Auto-Lock)
    // ==========================================

    fun getAutoLockDelayMs(context: Context): Long {
        return getPrefs(context).getLong(KEY_AUTO_LOCK_DELAY_MS, DEFAULT_AUTO_LOCK_DELAY_MS)
    }

    fun setAutoLockDelayMs(context: Context, delayMs: Long) {
        getPrefs(context).edit().putLong(KEY_AUTO_LOCK_DELAY_MS, delayMs).apply()
    }

    fun recordAppBackgrounded(context: Context) {
        getPrefs(context).edit().putLong(KEY_LAST_BACKGROUND_MS, System.currentTimeMillis()).apply()
    }

    fun recordAppForegrounded(context: Context) {
        getPrefs(context).edit().putLong(KEY_LAST_BACKGROUND_MS, 0L).apply()
    }

    fun shouldReLockOnForeground(context: Context): Boolean {
        if (!isSecurityEnabled(context)) return false
        val lastBg = getPrefs(context).getLong(KEY_LAST_BACKGROUND_MS, 0L)
        if (lastBg <= 0L) return false

        val delay = getAutoLockDelayMs(context)
        val elapsed = System.currentTimeMillis() - lastBg
        return elapsed >= delay
    }

    // ==========================================
    // FLAG_SECURE (anti captures d'écran et aperçu)
    // ==========================================

    fun isFlagSecureEnabled(context: Context): Boolean {
        // Désactivé par défaut pour éviter l'écran noir dans l'émulateur et sur les flux de streaming
        return getPrefs(context).getBoolean(KEY_FLAG_SECURE_ENABLED, false)
    }

    fun setFlagSecureEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_FLAG_SECURE_ENABLED, enabled).apply()
    }
}
