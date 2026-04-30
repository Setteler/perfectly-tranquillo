package com.methoda.tranquillo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val SeaDarkColorScheme = darkColorScheme(
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

// Light scheme — daylight palette per design README open question #1.
// Body text is a soft slate-blue (never pure black) so it sits gently on the
// pale ocean palettes; surfaces stay translucent so the gradient shows through.
private val SeaLightColorScheme = lightColorScheme(
    primary = Color(0xFF2A5780),         // deep ocean blue
    secondary = Color(0xFFB58F4D),       // burnt sand
    tertiary = Color(0xFFA85F47),        // muted coral
    background = Color(0xFFEAF1F4),      // pale foam
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE0EAF0),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF3F576B),    // soft slate-blue body text
    onSurface = Color(0xFF3F576B),
    onSurfaceVariant = Color(0xFF5A7384)
)

@Composable
fun PerfectlyTranquilloTheme(
    palette: Palette = Palettes.DeepTide,
    typography: Typography = PerfectlyTranquilloTypography,
    content: @Composable () -> Unit
) {
    val scheme = if (palette.isLight) SeaLightColorScheme else SeaDarkColorScheme
    CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography = typography,
            shapes = PerfectlyTranquilloShapes,
            content = content
        )
    }
}
