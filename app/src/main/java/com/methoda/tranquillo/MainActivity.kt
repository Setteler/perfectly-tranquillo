package com.methoda.tranquillo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.nav.RootNavHost
import com.methoda.tranquillo.ui.theme.Palettes
import com.methoda.tranquillo.ui.theme.PerfectlyTranquilloTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()
    private val pendingDeepLinkRoute = mutableStateOf<String?>(null)

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* If denied, reminders simply don't fire — user can enable later. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        consumeDeepLink(intent)
        ensureNotificationPermission()
        setContent {
            val paletteId by viewModel.palette.collectAsState()
            val palette = Palettes.all.firstOrNull { it.id == paletteId } ?: Palettes.DeepTide
            PerfectlyTranquilloTheme(palette = palette) {
                RootNavHost(
                    viewModel = viewModel,
                    pendingDeepLinkRoute = pendingDeepLinkRoute
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        consumeDeepLink(intent)
    }

    private fun consumeDeepLink(intent: Intent?) {
        val route = intent?.getStringExtra(EXTRA_DEEP_LINK_ROUTE) ?: return
        if (route.isBlank()) return
        pendingDeepLinkRoute.value = route
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        const val EXTRA_DEEP_LINK_ROUTE = "route"
    }
}
