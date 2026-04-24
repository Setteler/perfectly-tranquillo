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

private val JetBrainsMono = FontFamily(
    Font(googleFont = GoogleFont("JetBrains Mono"), fontProvider = fontProvider, weight = FontWeight.Normal)
)

val PerfectlyTranquilloTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal,
        fontSize = 34.sp, letterSpacing = (-0.005).em
    ),
    displayMedium = TextStyle(
        fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal,
        fontSize = 28.sp, letterSpacing = (-0.005).em
    ),
    displaySmall = TextStyle(
        fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal,
        fontSize = 22.sp, letterSpacing = (-0.005).em
    ),
    headlineMedium = TextStyle(
        fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Quicksand, fontWeight = FontWeight.SemiBold, fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Quicksand, fontWeight = FontWeight.Normal, fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Quicksand, fontWeight = FontWeight.Normal, fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Quicksand, fontWeight = FontWeight.Medium, fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Quicksand, fontWeight = FontWeight.Medium, fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal, fontSize = 11.sp
    )
)
