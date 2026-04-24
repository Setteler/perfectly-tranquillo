package com.methoda.tranquillo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SeaColorScheme = darkColorScheme(
    primary = Sky,
    secondary = Sand,
    tertiary = Coral,
    background = Ink,
    surface = Ink2,
    onPrimary = Ink,
    onSecondary = Ink,
    onTertiary = Ink,
    onBackground = Foam,
    onSurface = Foam
)

@Composable
fun PerfectlyTranquilloTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SeaColorScheme,
        content = content
    )
}
