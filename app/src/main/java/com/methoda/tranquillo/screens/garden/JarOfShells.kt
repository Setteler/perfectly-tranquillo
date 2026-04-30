package com.methoda.tranquillo.screens.garden

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.ui.components.Seashell
import com.methoda.tranquillo.ui.components.ShellColor

/** A collected shell ready to draw inside the jar. */
data class ShellInJar(val color: ShellColor)

/**
 * Glass jar holding [shells]. Layout matches the prototype:
 *  • Jar body path with shoulders + neck + rim.
 *  • Sand floor ellipse near the bottom.
 *  • Shells stacked bottom-up in a 5-column staggered grid with jitter.
 *  • Front-side highlights drawn over shells.
 */
@Composable
fun JarOfShells(
    shells: List<ShellInJar>,
    modifier: Modifier = Modifier,
    width: Dp = 300.dp,
    height: Dp = 320.dp
) {
    val density = LocalDensity.current
    val widthPx = with(density) { width.toPx() }
    val heightPx = with(density) { height.toPx() }

    val placed = remember(shells, widthPx, heightPx) {
        layoutShells(shells, widthPx, heightPx)
    }

    Box(modifier = modifier.size(width = width, height = height)) {
        Canvas(Modifier.size(width = width, height = height)) {
            drawJarBody(widthPx, heightPx)
        }

        for (p in placed) {
            val xDp = with(density) { p.x.toDp() }
            val yDp = with(density) { p.y.toDp() }
            Seashell(
                color = p.color,
                size = 38.dp,
                seed = p.seed,
                tiltDeg = p.rot,
                modifier = Modifier.offset(x = xDp, y = yDp)
            )
        }

        Canvas(Modifier.size(width = width, height = height)) {
            drawJarFrontHighlights(widthPx, heightPx)
        }
    }
}

private data class PlacedShell(
    val color: ShellColor,
    val x: Float,
    val y: Float,
    val rot: Float,
    val z: Int,
    val seed: Int
)

private fun layoutShells(shells: List<ShellInJar>, widthPx: Float, heightPx: Float): List<PlacedShell> {
    val innerLeft = 0.18f * widthPx
    val innerRight = 0.82f * widthPx
    val floorY = 0.88f * heightPx
    val innerW = innerRight - innerLeft
    val cols = 5
    val rowH = 30f
    val shellW = 38f

    val out = mutableListOf<PlacedShell>()
    shells.forEachIndexed { i, sh ->
        val row = i / cols
        val colCount = cols - (row % 2)
        val col = i % colCount
        val jitterX = ((i * 53) % 11) - 5
        val jitterY = ((i * 29) % 9) - 4
        val spread = innerW - shellW - 6f
        val x = innerLeft + 3f + (col + 0.5f + (row % 2) * 0.5f) * (spread / colCount) +
            jitterX - shellW / 2f + (spread / colCount) / 2f
        val y = floorY - 20f - row * rowH + jitterY
        val rot = ((i * 47) % 80) - 40
        out.add(PlacedShell(sh.color, x, y, rot.toFloat(), -row, i))
    }
    return out.sortedBy { it.z }
}

private fun DrawScope.drawJarBody(width: Float, height: Float) {
    val w = width
    val h = height

    val body = Path().apply {
        moveTo(0.22f * w, 0.18f * h)
        lineTo(0.22f * w, 0.12f * h)
        quadraticTo(0.22f * w, 0.08f * h, 0.26f * w, 0.08f * h)
        lineTo(0.74f * w, 0.08f * h)
        quadraticTo(0.78f * w, 0.08f * h, 0.78f * w, 0.12f * h)
        lineTo(0.78f * w, 0.18f * h)
        quadraticTo(0.88f * w, 0.22f * h, 0.88f * w, 0.32f * h)
        lineTo(0.88f * w, 0.88f * h)
        quadraticTo(0.88f * w, 0.94f * h, 0.82f * w, 0.94f * h)
        lineTo(0.18f * w, 0.94f * h)
        quadraticTo(0.12f * w, 0.94f * h, 0.12f * w, 0.88f * h)
        lineTo(0.12f * w, 0.32f * h)
        quadraticTo(0.12f * w, 0.22f * h, 0.22f * w, 0.18f * h)
        close()
    }

    val glass = Brush.horizontalGradient(
        colorStops = arrayOf(
            0f    to Color(0x59A8C7D8),
            0.20f to Color(0x14F0F6FA),
            0.50f to Color(0x0AFFFFFF),
            0.80f to Color(0x1FB5D2DD),
            1f    to Color(0x478597B0)
        ),
        startX = 0f,
        endX = w
    )
    drawPath(path = body, brush = glass)
    drawPath(
        path = body,
        color = Color(0xFFB5D6E0).copy(alpha = 0.28f),
        style = Stroke(width = 1.5f)
    )

    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFD9B681).copy(alpha = 0.4f),
                Color(0xFF9C7748).copy(alpha = 0.1f)
            ),
            center = Offset(w / 2f, 0.9f * h),
            radius = 0.36f * w
        ),
        topLeft = Offset(w / 2f - 0.36f * w, 0.9f * h - 0.03f * h),
        size = Size(0.72f * w, 0.06f * h)
    )
}

private fun DrawScope.drawJarFrontHighlights(width: Float, height: Float) {
    val w = width
    val h = height

    val left = Path().apply {
        moveTo(0.18f * w, 0.28f * h)
        quadraticTo(0.16f * w, 0.55f * h, 0.19f * w, 0.82f * h)
    }
    drawPath(
        path = left,
        color = Color.White.copy(alpha = 0.22f),
        style = Stroke(width = 4f, cap = StrokeCap.Round)
    )

    val right = Path().apply {
        moveTo(0.82f * w, 0.30f * h)
        quadraticTo(0.85f * w, 0.60f * h, 0.82f * w, 0.80f * h)
    }
    drawPath(
        path = right,
        color = Color.White.copy(alpha = 0.10f),
        style = Stroke(width = 2f, cap = StrokeCap.Round)
    )

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE0EAF0).copy(alpha = 0.5f),
                Color(0xFFA0BCC8).copy(alpha = 0.2f)
            ),
            startY = 0.08f * h,
            endY = 0.10f * h
        ),
        topLeft = Offset(0.22f * w, 0.08f * h),
        size = Size(0.56f * w, 2f)
    )

    val neck = Path().apply {
        moveTo(0.26f * w, 0.10f * h)
        lineTo(0.74f * w, 0.10f * h)
    }
    drawPath(
        path = neck,
        color = Color.White.copy(alpha = 0.35f),
        style = Stroke(width = 1f)
    )
}
