package com.methoda.tranquillo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.methoda.tranquillo.nav.RootNavHost
import com.methoda.tranquillo.ui.theme.PerfectlyTranquilloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PerfectlyTranquilloTheme {
                RootNavHost()
            }
        }
    }
}
