package com.methoda.tranquillo.ui.chrome

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.methoda.tranquillo.ui.theme.Deep
import com.methoda.tranquillo.ui.theme.Ink
import com.methoda.tranquillo.ui.theme.Ink2
import com.methoda.tranquillo.ui.theme.Sand
import com.methoda.tranquillo.ui.theme.Sea
import com.methoda.tranquillo.ui.theme.Sky

@Composable
fun SeaBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Deep, Ink2, Ink)
                    ),
                    size = Size(size.width, size.height)
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Sand.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.95f),
                        radius = size.minDimension * 0.9f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Sky.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width * 0.10f, size.height * 0.10f),
                        radius = size.minDimension * 0.75f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Sea.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width * 0.80f, size.height * 0.30f),
                        radius = size.minDimension * 0.7f
                    )
                )
            }
    ) {
        content()
    }
}
