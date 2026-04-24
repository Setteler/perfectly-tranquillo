package com.methoda.tranquillo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.nav.RootNavHost
import com.methoda.tranquillo.ui.theme.PerfectlyTranquilloTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PerfectlyTranquilloTheme {
                RootNavHost(viewModel = viewModel)
            }
        }
    }
}
