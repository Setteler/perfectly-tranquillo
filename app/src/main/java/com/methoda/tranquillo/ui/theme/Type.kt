package com.methoda.tranquillo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.methoda.tranquillo.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// --- Font families -------------------------------------------------------

// New default display font (handwritten casual)
private val Caveat = FontFamily(
    Font(googleFont = GoogleFont("Caveat"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Caveat"), fontProvider = fontProvider, weight = FontWeight.Medium)
)

// New default body font
private val Nunito = FontFamily(
    Font(googleFont = GoogleFont("Nunito"), fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = fontProvider, weight = FontWeight.SemiBold)
)

// Kept wired for the #7 font-picker (not used in default Typography)
@Suppress("unused")
private val Fraunces = FontFamily(
    Font(googleFont = GoogleFont("Fraunces"), fontProvider = fontProvider, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(googleFont = GoogleFont("Fraunces"), fontProvider = fontProvider, weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(googleFont = GoogleFont("Fraunces"), fontProvider = fontProvider, weight = FontWeight.Medium, style = FontStyle.Italic)
)

@Suppress("unused")
private val Quicksand = FontFamily(
    Font(googleFont = GoogleFont("Quicksand"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Quicksand"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Quicksand"), fontProvider = fontProvider, weight = FontWeight.SemiBold)
)

// Mono kept (used later for numeric readouts)
@Suppress("unused")
private val JetBrainsMono = FontFamily(
    Font(googleFont = GoogleFont("JetBrains Mono"), fontProvider = fontProvider, weight = FontWeight.Normal)
)

// --- Typography ----------------------------------------------------------
// Caveat runs visually small, so bump sizes ~1.25x a normal serif.

val PerfectlyTranquilloTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Caveat, fontWeight = FontWeight.Medium,
        fontSize = 36.sp, lineHeight = (36 * 1.05f).sp, letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = Caveat, fontWeight = FontWeight.Medium,
        fontSize = 28.sp, lineHeight = (28 * 1.05f).sp, letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = Caveat, fontWeight = FontWeight.Medium,
        fontSize = 22.sp, lineHeight = (22 * 1.05f).sp, letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Caveat, fontWeight = FontWeight.Medium,
        fontSize = 22.sp, lineHeight = (22 * 1.05f).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 14.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 13.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.Medium, fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
        letterSpacing = 0.1.em
    ),
    // Eyebrow: Nunito 600, 11sp, tracked. All-caps applied at callsite.
    labelSmall = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 11.sp,
        letterSpacing = 0.18.em
    )
)
