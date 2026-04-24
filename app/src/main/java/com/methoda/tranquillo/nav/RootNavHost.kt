package com.methoda.tranquillo.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.methoda.tranquillo.ui.chrome.BottomTabBar
import com.methoda.tranquillo.ui.chrome.SeaBackground
import com.methoda.tranquillo.ui.placeholder.PlaceholderScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val tabRoutes = setOf(
        Route.Home.path, Route.Habits.path, Route.Mandala.path, Route.Garden.path
    )

    SeaBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                if (currentRoute in tabRoutes) {
                    CenterAlignedTopAppBar(
                        title = {},
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
                    PlaceholderScreen(
                        title = "Home",
                        eyebrow = "today's mandala",
                        subprojectNumber = 2
                    )
                }
                composable(Route.Habits.path) {
                    PlaceholderScreen(
                        title = "Habits",
                        eyebrow = "small things, often",
                        subprojectNumber = 3
                    )
                }
                composable(Route.Mandala.path) {
                    PlaceholderScreen(
                        title = "Mandala",
                        eyebrow = "your inner landscape",
                        subprojectNumber = 2
                    )
                }
                composable(Route.Garden.path) {
                    PlaceholderScreen(
                        title = "Garden",
                        eyebrow = "seven days of stones",
                        subprojectNumber = 5
                    )
                }
                composable(Route.Settings.path) {
                    PlaceholderScreen(
                        title = "Settings",
                        eyebrow = "tune the sea",
                        subprojectNumber = 7
                    )
                }
            }
        }
    }
}
