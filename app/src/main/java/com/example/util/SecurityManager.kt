package com.example.util

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

object SecurityManager {

    private const val PREFS_NAME = "symphonix_security_prefs"
    private const val KEY_PIN_ENABLED = "key_pin_enabled"
    private const val KEY_PIN_HASH = "key_pin_hash"
    private const val SALT = "SYMPHONIX_CAISSE_SECURE_SALT_v1#"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isSecurityEnabled(context: Context): Boolean {
        val prefs = getPrefs(context)
        val isEnabled = prefs.getBoolean(KEY_PIN_ENABLED, false)
        val hash = prefs.getString(KEY_PIN_HASH, null)
        return isEnabled && !hash.isNullOrBlank()
    }

    fun isPinConfigured(context: Context): Boolean {
        val prefs = getPrefs(context)
        return !prefs.getString(KEY_PIN_HASH, null).isNullOrBlank()
    }

    fun setSecurityEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PIN_ENABLED, enabled).apply()
    }

    fun savePin(context: Context, pin: String) {
        val hash = hashPin(pin)
        getPrefs(context).edit()
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
    }

    fun removePin(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .apply()
    }

    fun verifyPin(context: Context, inputPin: String): Boolean {
        val storedHash = getPrefs(context).getString(KEY_PIN_HASH, null) ?: return false
        val inputHash = hashPin(inputPin)
        return storedHash == inputHash
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest((SALT + pin).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
