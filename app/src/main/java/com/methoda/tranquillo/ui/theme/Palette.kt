package com.methoda.tranquillo.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Ocean palette spec — see docs/design/README.md § "Palettes".
 * Each palette carries 3 base gradient stops and two accent highlights.
 * `waves = true` adds a subtle kelp sine overlay drawn by SeaBackground.
 */
data class Palette(
    val id: String,
    val displayName: String,
    val baseTop: Color,
    val baseMid: Color,
    val baseBot: Color,
    val warmHighlight: Color,    // includes alpha
    val coolAccent: Color,        // includes alpha
    val waves: Boolean = false
)

object Palettes {
    val DeepTide = Palette(
        id = "deep_tide",
        displayName = "Deep Tide",
        baseTop = Color(0xFF1A4F50),  // oklch(0.30 0.07 195) approx
        baseMid = Color(0xFF103A3D),  // oklch(0.22 0.06 200) approx
        baseBot = Color(0xFF0A2A2D),  // oklch(0.16 0.05 205) approx
        warmHighlight = Color(0xFFE8C57A).copy(alpha = 0.22f),
        coolAccent = Color(0xFF3F8B92).copy(alpha = 0.30f)
    )
    val Tidepool = Palette(
        id = "tidepool",
        displayName = "Tidepool",
        baseTop = Color(0xFF1B2C4F),
        baseMid = Color(0xFF12203D),
        baseBot = Color(0xFF0A172E),
        warmHighlight = Color(0xFFE8C57A).copy(alpha = 0.18f),
        coolAccent = Color(0xFF4068A8).copy(alpha = 0.28f)
    )
    val SeaGlass = Palette(
        id = "sea_glass",
        displayName = "Sea Glass",
        baseTop = Color(0xFF265760),
        baseMid = Color(0xFF1A3F49),
        baseBot = Color(0xFF132E36),
        warmHighlight = Color(0xFFE8C57A).copy(alpha = 0.22f),
        coolAccent = Color(0xFF4FA0A8).copy(alpha = 0.30f)
    )
    val KelpForest = Palette(
        id = "kelp_forest",
        displayName = "Kelp Forest",
        baseTop = Color(0xFF134645),
        baseMid = Color(0xFF0B302F),
        baseBot = Color(0xFF06201F),
        warmHighlight = Color(0xFFE8C57A).copy(alpha = 0.20f),
        coolAccent = Color(0xFF1F706E).copy(alpha = 0.28f),
        waves = true
    )

    // Light palettes — coolAccent slot carries a warm cream "moon glow" overlay.
    val MoonlitTide = Palette(
        id = "moonlit_tide",
        displayName = "Moonlit Tide",
        baseTop = Color(0xFFB1C9E2),
        baseMid = Color(0xFF94B8D4),
        baseBot = Color(0xFF76A6C6),
        warmHighlight = Color(0xFFFFECC8).copy(alpha = 0.10f),
        coolAccent = Color(0xFFFFECC8).copy(alpha = 0.30f)
    )
    val LagoonGlow = Palette(
        id = "lagoon_glow",
        displayName = "Lagoon Glow",
        baseTop = Color(0xFFB1D6E2),
        baseMid = Color(0xFF94C8D4),
        baseBot = Color(0xFF76BAC6),
        warmHighlight = Color(0xFFFFECC8).copy(alpha = 0.10f),
        coolAccent = Color(0xFFFFECC8).copy(alpha = 0.30f)
    )
    val Stillwater = Palette(
        id = "stillwater",
        displayName = "Stillwater",
        baseTop = Color(0xFF99C2D6),
        baseMid = Color(0xFF84AECD),
        baseBot = Color(0xFF6F99C3),
        warmHighlight = Color(0xFFFFECC8).copy(alpha = 0.06f),
        coolAccent = Color(0xFFFFECC8).copy(alpha = 0.18f)
    )

    val all = listOf(DeepTide, Tidepool, SeaGlass, KelpForest, MoonlitTide, LagoonGlow, Stillwater)
}

val LocalPalette = staticCompositionLocalOf { Palettes.DeepTide }
