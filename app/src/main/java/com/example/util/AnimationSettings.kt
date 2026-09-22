package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

enum class AnimationMode(val label: String) {
    AUTO("Automatique (selon le système)"),
    ENABLED("Toujours activées"),
    DISABLED("Désactivées (économie)")
}

object AnimationSettings {
    private const val PREFS_NAME = "symphonix_animation_prefs"
    private const val KEY_ANIMATION_MODE = "key_animation_mode"

    fun getAnimationMode(context: Context): AnimationMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val modeStr = prefs.getString(KEY_ANIMATION_MODE, AnimationMode.AUTO.name) ?: AnimationMode.AUTO.name
        return try {
            AnimationMode.valueOf(modeStr)
        } catch (_: Exception) {
            AnimationMode.AUTO
        }
    }

    fun setAnimationMode(context: Context, mode: AnimationMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ANIMATION_MODE, mode.name).apply()
    }

    fun areAnimationsEnabled(context: Context): Boolean {
        return when (getAnimationMode(context)) {
            AnimationMode.ENABLED -> true
            AnimationMode.DISABLED -> false
            AnimationMode.AUTO -> !isSystemAnimationConstrained(context)
        }
    }

    fun isSystemAnimationConstrained(context: Context): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager?.isPowerSaveMode == true) {
                return true
            }

            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (activityManager?.isLowRamDevice == true) {
                return true
            }

            val animatorScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            if (animatorScale <= 0f) {
                return true
            }

            false
        } catch (_: Exception) {
            false
        }
    }
}
