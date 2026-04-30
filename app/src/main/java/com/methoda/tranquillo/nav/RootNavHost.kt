package com.methoda.tranquillo.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.screens.actions.BreathScreen
import com.methoda.tranquillo.screens.actions.EveningScreen
import com.methoda.tranquillo.screens.actions.FocusScreen
import com.methoda.tranquillo.screens.actions.MorningScreen
import com.methoda.tranquillo.screens.garden.GardenScreen
import com.methoda.tranquillo.screens.habits.HabitsScreen
import com.methoda.tranquillo.screens.home.HomeScreen
import com.methoda.tranquillo.screens.mandala.MandalaScreen
import com.methoda.tranquillo.screens.settings.SettingsScreen
import com.methoda.tranquillo.screens.takebreak.TakeBreakScreen
import com.methoda.tranquillo.ui.chrome.BottomTabBar
import com.methoda.tranquillo.ui.chrome.SeaBackground
import com.methoda.tranquillo.ui.placeholder.PlaceholderScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootNavHost(
    viewModel: AppViewModel,
    pendingDeepLinkRoute: MutableState<String?> = remember { mutableStateOf(null) }
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val deepLink = pendingDeepLinkRoute.value
    LaunchedEffect(deepLink) {
        val target = deepLink ?: return@LaunchedEffect
        navController.navigate(target) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        pendingDeepLinkRoute.value = null
    }

    // Morning gate — first time the app opens in morning hours and Morning
    // hasn't been completed/dismissed today, route to the Morning screen
    // before the user sees Home. Fires once per app launch.
    var morningGateChecked by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (morningGateChecked) return@LaunchedEffect
        morningGateChecked = true
        // Wait until the gate flow has produced its real value (not the
        // eager `false` default during VM construction).
        kotlinx.coroutines.delay(50)
        if (viewModel.shouldShowMorningGate.value) {
            navController.navigate(Route.Morning.path)
        }
    }

    val tabRoutes = setOf(
        Route.Home.path, Route.Habits.path, Route.Mandala.path, Route.Garden.path, Route.TakeBreak.path
    )

    SeaBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                when (currentRoute) {
                    in tabRoutes -> {
                        val sound by viewModel.soundEnabled.collectAsState()
                        CenterAlignedTopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton(onClick = { viewModel.setSoundEnabled(!sound) }) {
                                    Icon(
                                        imageVector = if (sound) Icons.Outlined.VolumeUp
                                                      else Icons.Outlined.VolumeOff,
                                        contentDescription = if (sound) "Sound on" else "Sound off"
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { navController.navigate(Route.Settings.path) }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = "Settings"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                    Route.Settings.path -> {
                        CenterAlignedTopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }
            },
            bottomBar = {
                if (currentRoute in tabRoutes) {
                    BottomTabBar(navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Route.Home.path) {
                    HomeScreen(
                        viewModel = viewModel,
                        onActionClick = { route -> navController.navigate(route) }
                    )
                }
                composable(Route.Habits.path) {
                    HabitsScreen(viewModel = viewModel)
                }
                composable(Route.Mandala.path) {
                    MandalaScreen(viewModel = viewModel)
                }
                composable(Route.Garden.path) {
                    GardenScreen(viewModel = viewModel)
                }
                composable(Route.TakeBreak.path) {
                    TakeBreakScreen(
                        viewModel = viewModel,
                        onRedirect = { route -> navController.navigate(route) }
                    )
                }
                composable(Route.Settings.path) {
                    SettingsScreen(viewModel = viewModel)
                }
                // Quick-action real screens (#4)
                composable(Route.Morning.path) {
                    MorningScreen(
                        viewModel = viewModel,
                        onClose = { navController.popBackStack() }
                    )
                }
                composable(Route.Evening.path) {
                    EveningScreen(
                        viewModel = viewModel,
                        onClose = { navController.popBackStack() }
                    )
                }
                composable(Route.Breath.path) {
                    BreathScreen(
                        viewModel = viewModel,
                        onClose = { navController.popBackStack() }
                    )
                }
                composable(Route.Focus.path) {
                    FocusScreen(
                        viewModel = viewModel,
                        onClose = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
