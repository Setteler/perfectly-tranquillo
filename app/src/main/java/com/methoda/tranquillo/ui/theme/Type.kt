package com.methoda.tranquillo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font as ResFont
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

// Bundled local resource so display always renders — even when Google Fonts
// is slow or unreachable.
private val Caveat = FontFamily(
    ResFont(R.font.caveat, FontWeight.Normal),
    ResFont(R.font.caveat, FontWeight.Medium)
)

private val Nunito = FontFamily(
    Font(googleFont = GoogleFont("Nunito"), fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = fontProvider, weight = FontWeight.SemiBold)
)

private val Fraunces = FontFamily(
    Font(googleFont = GoogleFont("Fraunces"), fontProvider = fontProvider, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(googleFont = GoogleFont("Fraunces"), fontProvider = fontProvider, weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(googleFont = GoogleFont("Fraunces"), fontProvider = fontProvider, weight = FontWeight.Medium, style = FontStyle.Italic)
)

private val Quicksand = FontFamily(
    Font(googleFont = GoogleFont("Quicksand"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Quicksand"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Quicksand"), fontProvider = fontProvider, weight = FontWeight.SemiBold)
)

private val InstrumentSerif = FontFamily(
    Font(googleFont = GoogleFont("Instrument Serif"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Instrument Serif"), fontProvider = fontProvider, weight = FontWeight.Normal, style = FontStyle.Italic)
)

private val Inter = FontFamily(
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.SemiBold)
)

// --- Typographies --------------------------------------------------------
// Each pair tunes the display sizes since e.g. Caveat runs visually small
// while Fraunces/Instrument run larger; multipliers below are hand-tuned.

private fun typographyOf(
    display: FontFamily,
    body: FontFamily,
    displayWeight: FontWeight = FontWeight.Medium,
    displayStyle: FontStyle = FontStyle.Normal,
    sizeMul: Float = 1f
): Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = display, fontWeight = displayWeight, fontStyle = displayStyle,
        fontSize = (36 * sizeMul).sp,
        lineHeight = (36 * sizeMul * 1.05f).sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = display, fontWeight = displayWeight, fontStyle = displayStyle,
        fontSize = (28 * sizeMul).sp,
        lineHeight = (28 * sizeMul * 1.05f).sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = display, fontWeight = displayWeight, fontStyle = displayStyle,
        fontSize = (22 * sizeMul).sp,
        lineHeight = (22 * sizeMul * 1.05f).sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = display, fontWeight = displayWeight, fontStyle = displayStyle,
        fontSize = (22 * sizeMul).sp,
        lineHeight = (22 * sizeMul * 1.05f).sp
    ),
    titleLarge = TextStyle(
        fontFamily = body, fontWeight = FontWeight.SemiBold, fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = body, fontWeight = FontWeight.Normal, fontSize = 14.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = body, fontWeight = FontWeight.Normal, fontSize = 13.sp
    ),
    labelLarge = TextStyle(
        fontFamily = body, fontWeight = FontWeight.Medium, fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = body, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
        letterSpacing = 0.1.em
    ),
    labelSmall = TextStyle(
        fontFamily = body, fontWeight = FontWeight.SemiBold, fontSize = 11.sp,
        letterSpacing = 0.18.em
    )
)

private val CaveatNunito       = typographyOf(Caveat,          Nunito,    sizeMul = 1.0f)
private val FrauncesQuicksand  = typographyOf(Fraunces,        Quicksand, displayStyle = FontStyle.Italic, sizeMul = 0.85f)
private val InstrumentInter    = typographyOf(InstrumentSerif, Inter,     displayStyle = FontStyle.Italic, sizeMul = 0.9f)

/** Default typography. Replaced at runtime by [typographyForFontPair]. */
val PerfectlyTranquilloTypography: Typography = CaveatNunito

/** Map a PrefsStore font_pair string to a Typography. */
fun typographyForFontPair(id: String): Typography = when (id) {
    "fraunces"   -> FrauncesQuicksand
    "instrument" -> InstrumentInter
    "caveat"     -> CaveatNunito
    else         -> CaveatNunito
}

data class FontOption(val id: String, val displayName: String, val sample: String)

val FontOptions: List<FontOption> = listOf(
    FontOption("caveat",     "Caveat + Nunito",     "Drift on, friend."),
    FontOption("fraunces",   "Fraunces + Quicksand", "Drift on, friend."),
    FontOption("instrument", "Instrument + Inter",   "Drift on, friend.")
)
