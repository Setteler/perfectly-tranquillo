package com.methoda.tranquillo.screens.mandala

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.AmPmFill
import com.methoda.tranquillo.ui.theme.Sand
import com.methoda.tranquillo.ui.theme.Sky

/**
 * Small disc reflecting the AM/PM fill state of a single resource.
 * 4 visual states: empty, AM only, PM only, both.
 */
@Composable
fun PetalDot(
    fill: AmPmFill?,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp
) {
    val amLit = (fill?.am ?: 0f) > 0f
    val pmLit = (fill?.pm ?: 0f) > 0f

    val bg: Brush = when {
        amLit && pmLit -> Brush.horizontalGradient(listOf(Sky.copy(alpha = 0.85f), Sand.copy(alpha = 0.85f)))
        amLit          -> Brush.horizontalGradient(listOf(Sky.copy(alpha = 0.85f), Sky.copy(alpha = 0.85f)))
        pmLit          -> Brush.horizontalGradient(listOf(Sand.copy(alpha = 0.85f), Sand.copy(alpha = 0.85f)))
        else           -> Brush.horizontalGradient(listOf(Color(0x1AF5F1E8), Color(0x1AF5F1E8)))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
    )
}
