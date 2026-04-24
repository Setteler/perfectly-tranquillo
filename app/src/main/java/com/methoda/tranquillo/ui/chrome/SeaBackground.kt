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
import com.methoda.tranquillo.ui.theme.LocalPalette
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun SeaBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val palette = LocalPalette.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Base vertical gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(palette.baseTop, palette.baseMid, palette.baseBot)
                    ),
                    size = Size(size.width, size.height)
                )
                // Warm highlight — bottom-right
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.warmHighlight, Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.95f),
                        radius = size.minDimension * 0.9f
                    )
                )
                // Cool accent — top-left
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.coolAccent, Color.Transparent),
                        center = Offset(size.width * 0.10f, size.height * 0.10f),
                        radius = size.minDimension * 0.75f
                    )
                )

                // Kelp wave overlay — only for palettes that opt in.
                if (palette.waves) {
                    val waveColor = Color(0xFFF5F1E8).copy(alpha = 0.06f)
                    val strokeW = 1.5f
                    val amplitude = size.height * 0.012f
                    val wavelength = size.width * 0.35f
                    // A handful of sine "kelp strands" at staggered Y positions.
                    val ys = listOf(0.22f, 0.41f, 0.58f, 0.74f, 0.88f)
                    for (yFrac in ys) {
                        val yBase = size.height * yFrac
                        val step = 6f
                        var x = 0f
                        var prevX = 0f
                        var prevY = yBase + sin((0f / wavelength) * 2f * PI.toFloat()) * amplitude
                        while (x <= size.width) {
                            val y = yBase + sin((x / wavelength) * 2f * PI.toFloat()) * amplitude
                            drawLine(
                                color = waveColor,
                                start = Offset(prevX, prevY),
                                end = Offset(x, y),
                                strokeWidth = strokeW
                            )
                            prevX = x
                            prevY = y
                            x += step
                        }
                    }
                }
            }
    ) {
        // Re-provide so nested consumers see the same palette this background painted from.
        CompositionLocalProvider(LocalPalette provides palette) {
            content()
        }
    }
}
