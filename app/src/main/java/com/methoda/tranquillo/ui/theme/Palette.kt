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
    val all = listOf(DeepTide, Tidepool, SeaGlass, KelpForest)
}

val LocalPalette = staticCompositionLocalOf { Palettes.DeepTide }
