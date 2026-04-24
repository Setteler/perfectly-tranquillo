package com.methoda.tranquillo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val SeaColorScheme = darkColorScheme(
    primary = Sky,
    secondary = Sand,
    tertiary = Coral,
    background = Ink,
    surface = Ink2,
    surfaceVariant = Ink3,
    onPrimary = Ink,
    onSecondary = Ink,
    onTertiary = Ink,
    onBackground = Foam,
    onSurface = Foam,
    onSurfaceVariant = Mist
)

@Composable
fun PerfectlyTranquilloTheme(
    content: @Composable () -> Unit
) {
    // LocalPalette defaults to Deep Tide; palette switcher arrives in #7.
    CompositionLocalProvider(LocalPalette provides Palettes.DeepTide) {
        MaterialTheme(
            colorScheme = SeaColorScheme,
            typography = PerfectlyTranquilloTypography,
            shapes = PerfectlyTranquilloShapes,
            content = content
        )
    }
}
