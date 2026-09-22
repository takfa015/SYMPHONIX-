package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CashScreen
import com.example.ui.CashViewModel
import com.example.ui.screens.SecurityLockScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.RecapCaisseTheme
import com.example.util.SecurityManager

enum class AppScreenState {
    SPLASH,
    LOCK,
    MAIN
}

class MainActivity : FragmentActivity() {

    private val currentScreenState = mutableStateOf(AppScreenState.SPLASH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        applyFlagSecure()

        // Enable maximum display refresh rate (120Hz / 90Hz) supported by the device screen
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val currentDisplay = display
                val modes = currentDisplay?.supportedModes
                val highestRefreshRateMode = modes?.maxByOrNull { it.refreshRate }
                if (highestRefreshRateMode != null) {
                    val lp = window.attributes
                    lp.preferredDisplayModeId = highestRefreshRateMode.modeId
                    window.attributes = lp
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                val modes = windowManager.defaultDisplay?.supportedModes
                val highestRefreshRateMode = modes?.maxByOrNull { it.refreshRate }
                if (highestRefreshRateMode != null) {
                    val lp = window.attributes
                    lp.preferredDisplayModeId = highestRefreshRateMode.modeId
                    window.attributes = lp
                }
            }
        } catch (_: Exception) {
            // Graceful fallback if device display manager does not support mode overrides
        }

        setContent {
            RecapCaisseTheme {
                val screenState by currentScreenState
                val viewModel: CashViewModel = viewModel()

                when (screenState) {
                    AppScreenState.SPLASH -> {
                        SplashScreen(
                            onSplashFinished = {
                                if (SecurityManager.isSecurityEnabled(this)) {
                                    currentScreenState.value = AppScreenState.LOCK
                                } else {
                                    currentScreenState.value = AppScreenState.MAIN
                                }
                            }
                        )
                    }
                    AppScreenState.LOCK -> {
                        SecurityLockScreen(
                            onUnlocked = {
                                currentScreenState.value = AppScreenState.MAIN
                            }
                        )
                    }
                    AppScreenState.MAIN -> {
                        CashScreen(
                            viewModel = viewModel,
                            onFlagSecureChanged = { applyFlagSecure() }
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (currentScreenState.value == AppScreenState.MAIN) {
            SecurityManager.recordAppBackgrounded(this)
        }
    }

    override fun onStart() {
        super.onStart()
        if (currentScreenState.value == AppScreenState.MAIN) {
            if (SecurityManager.shouldReLockOnForeground(this)) {
                currentScreenState.value = AppScreenState.LOCK
            }
            SecurityManager.recordAppForegrounded(this)
        }
    }

    override fun onResume() {
        super.onResume()
        applyFlagSecure()
        if (currentScreenState.value == AppScreenState.MAIN && SecurityManager.shouldReLockOnForeground(this)) {
            currentScreenState.value = AppScreenState.LOCK
            SecurityManager.recordAppForegrounded(this)
        }
    }

    fun applyFlagSecure() {
        // En environnement d'émulateur ou de streaming WebRTC (ex: Google AI Studio),
        // FLAG_SECURE produit un écran 100% noir car Android bloque la capture du flux vidéo.
        // On s'assure donc que FLAG_SECURE n'est pas actif sur émulateur.
        val isEmulator = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("emulator")

        if (SecurityManager.isFlagSecureEnabled(this) && !isEmulator) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
