package com.methoda.tranquillo.screens.actions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * "Scratch off" card — a foil-like cover painted over the [content] that the
 * user erases by dragging. Once enough has been revealed (~25% of pixels),
 * [onRevealed] fires once and the cover gracefully fades out.
 */
@Composable
fun ScratchCard(
    modifier: Modifier = Modifier,
    foilColor: Color = MaterialTheme.colorScheme.primary,
    coverHint: String = "scratch to reveal today's quote",
    onRevealed: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val strokes = remember { mutableStateListOf<Offset>() }
    var revealed by remember { mutableStateOf(false) }
    var hintVisible by remember { mutableStateOf(true) }

    LaunchedEffect(strokes.size) {
        if (revealed) return@LaunchedEffect
        // Heuristic: each stroke point covers ~60dp circle. After ~40 points
        // across a 320dp-wide card we've cleared enough.
        if (strokes.size >= 40) {
            revealed = true
            onRevealed()
        }
        if (strokes.isNotEmpty()) hintVisible = false
    }

    // Once revealed, hold the cover for a beat, then fade out.
    LaunchedEffect(revealed) {
        if (revealed) {
            delay(400L)
        }
    }

    Box(
        modifier = modifier
            .pointerInput(revealed) {
                if (revealed) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        for (change in event.changes) {
                            if (change.pressed) {
                                strokes.add(change.position)
                            }
                        }
                    }
                }
            }
    ) {
        // The revealed content sits behind the cover.
        Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            content()
        }

        // Cover layer — a foil rectangle over which we erase circles.
        AnimatedVisibility(
            visible = !revealed,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            ) {
                drawFoil(foilColor)
                val brushRadius = 60f
                for (p in strokes) {
                    drawCircle(
                        color = Color.Transparent,
                        center = p,
                        radius = brushRadius,
                        blendMode = BlendMode.Clear
                    )
                }
            }
        }

        // Hint label — visible until first stroke.
        AnimatedVisibility(
            visible = hintVisible && !revealed,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val onFoil = if (foilColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.55f)
                         else Color.White.copy(alpha = 0.85f)
            Text(
                text = coverHint,
                style = MaterialTheme.typography.titleLarge,
                color = onFoil,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}

private fun DrawScope.drawFoil(base: Color) {
    // Fully opaque base + a subtle highlight band on top for a metallic feel.
    drawRect(color = base)
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.10f)
            ),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
    )
}

