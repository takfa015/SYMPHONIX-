package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                val context = this
                var screenState by remember { mutableStateOf(AppScreenState.SPLASH) }
                val viewModel: CashViewModel = viewModel()

                when (screenState) {
                    AppScreenState.SPLASH -> {
                        SplashScreen(
                            onSplashFinished = {
                                if (SecurityManager.isSecurityEnabled(context)) {
                                    screenState = AppScreenState.LOCK
                                } else {
                                    screenState = AppScreenState.MAIN
                                }
                            }
                        )
                    }
                    AppScreenState.LOCK -> {
                        SecurityLockScreen(
                            onUnlocked = {
                                screenState = AppScreenState.MAIN
                            }
                        )
                    }
                    AppScreenState.MAIN -> {
                        CashScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
