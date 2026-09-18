package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CashScreen
import com.example.ui.CashViewModel
import com.example.ui.theme.RecapCaisseTheme

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
                val viewModel: CashViewModel = viewModel()
                CashScreen(viewModel = viewModel)
            }
        }
    }
}
