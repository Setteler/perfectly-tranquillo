package com.methoda.tranquillo.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Route(val path: String) {
    data object Home : Route("home")
    data object Habits : Route("habits")
    data object Mandala : Route("mandala")
    data object Garden : Route("garden")
    data object Settings : Route("settings")
}

data class TabDestination(
    val route: Route,
    val label: String,
    val icon: ImageVector
)

val TabDestinations: List<TabDestination> = listOf(
    TabDestination(Route.Home, "Home", Icons.Outlined.Home),
    TabDestination(Route.Habits, "Habits", Icons.Outlined.CheckCircle),
    TabDestination(Route.Mandala, "Mandala", Icons.Outlined.AutoAwesome),
    TabDestination(Route.Garden, "Garden", Icons.Outlined.FilterVintage)
)
