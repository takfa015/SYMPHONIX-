package com.example

import android.os.Bundle
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
        setContent {
            RecapCaisseTheme {
                val viewModel: CashViewModel = viewModel()
                CashScreen(viewModel = viewModel)
            }
        }
    }
}
