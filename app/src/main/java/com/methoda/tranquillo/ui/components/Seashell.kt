package com.methoda.tranquillo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class ShellColor { Pearl, Coral, Rose, Butter, Sky, Lilac, Moss, Sand }

private data class ShellPalette(val a: Color, val b: Color, val c: Color, val ridge: Color)

private fun paletteFor(color: ShellColor): ShellPalette = when (color) {
    ShellColor.Pearl  -> ShellPalette(Color(0xFFF7F1DC), Color(0xFFE2D6B6), Color(0xFFB39A6E), Color(0x59867858))
    ShellColor.Coral  -> ShellPalette(Color(0xFFF6CCB6), Color(0xFFE0987A), Color(0xFFB6593F), Color(0x66833B25))
    ShellColor.Rose   -> ShellPalette(Color(0xFFF4D0D6), Color(0xFFD8929D), Color(0xFFA85968), Color(0x59823B4A))
    ShellColor.Butter -> ShellPalette(Color(0xFFF7E5B0), Color(0xFFE2BF7A), Color(0xFFB48D44), Color(0x59836424))
    ShellColor.Sky    -> ShellPalette(Color(0xFFD2E3F0), Color(0xFFA1BCD3), Color(0xFF6886A6), Color(0x66465F7B))
    ShellColor.Lilac  -> ShellPalette(Color(0xFFD7CDE2), Color(0xFFAB97C7), Color(0xFF7560A1), Color(0x66523F77))
    ShellColor.Moss   -> ShellPalette(Color(0xFFD7E2C5), Color(0xFFA1BC91), Color(0xFF6E885E), Color(0x66465F40))
    ShellColor.Sand   -> ShellPalette(Color(0xFFEAD9B0), Color(0xFFC9B58D), Color(0xFF7E6E50), Color(0x66523F2A))
}

private fun pseudoRandom01(seed: Int): Float {
    val v = ((seed.toLong() * 9301L + 49297L) % 233280L + 233280L) % 233280L
    return v.toFloat() / 233280f
}

/**
 * Scallop-fan seashell, Compose port of the prototype's `Seashell` SVG.
 *
 * The shell's viewBox is 40×40; we scale to [size]. Orientation is umbo-up;
 * caller may pass [tiltDeg] to rotate. The scallop bottom edge has 12 curls.
 */
@Composable
fun Seashell(
    color: ShellColor,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    seed: Int = 0,
    tiltDeg: Float? = null
) {
    val palette = remember(color) { paletteFor(color) }
    val s = remember(seed) { pseudoRandom01(seed) }
    val tilt = remember(seed, tiltDeg) { tiltDeg ?: (-30f + (s * 60f)) }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = tilt
            }
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawShell(palette, seed)
        }
    }
}

private fun DrawScope.drawShell(palette: ShellPalette, seed: Int) {
    // Map 40x40 viewBox to actual size.
    val w = size.width
    val h = size.height
    val sx = w / 40f
    val sy = h / 40f
    fun x(v: Float) = v * sx
    fun y(v: Float) = v * sy

    // Scallop fan path matches prototype's SVG d= string.
    val path = Path().apply {
        moveTo(x(20f), y(4f))
        cubicTo(x(10f), y(4f), x(3f), y(13f), x(3f), y(22f))
        cubicTo(x(3f), y(27f), x(5f), y(31f), x(7f), y(33f))
        // 12 bottom-edge scalloped curls (Q ... Q ... )
        quadraticTo(x(8f), y(35f), x(9f), y(33f))
        quadraticTo(x(10f), y(36f), x(11f), y(33f))
        quadraticTo(x(12f), y(36f), x(13f), y(34f))
        quadraticTo(x(14f), y(36f), x(15f), y(34f))
        quadraticTo(x(16f), y(37f), x(17f), y(34f))
        quadraticTo(x(18f), y(37f), x(19f), y(34f))
        quadraticTo(x(20f), y(37f), x(21f), y(34f))
        quadraticTo(x(22f), y(37f), x(23f), y(34f))
        quadraticTo(x(24f), y(37f), x(25f), y(34f))
        quadraticTo(x(26f), y(36f), x(27f), y(34f))
        quadraticTo(x(28f), y(36f), x(29f), y(33f))
        quadraticTo(x(30f), y(36f), x(31f), y(33f))
        quadraticTo(x(32f), y(35f), x(33f), y(33f))
        cubicTo(x(35f), y(31f), x(37f), y(27f), x(37f), y(22f))
        cubicTo(x(37f), y(13f), x(30f), y(4f), x(20f), y(4f))
        close()
    }

    // Soft drop shadow approximation: draw the path slightly offset darker.
    val shadow = Path().apply {
        addPath(path, Offset(0f, y(1.5f)))
    }
    drawPath(path = shadow, color = Color.Black.copy(alpha = 0.25f))

    // Body fill — radial gradient: highlight top-center, mid through, dark rim.
    val brush = Brush.radialGradient(
        colorStops = arrayOf(
            0f to palette.a,
            0.55f to palette.b,
            1f to palette.c
        ),
        center = Offset(x(20f), y(7f)),
        radius = (sx + sy) * 16f
    )
    drawPath(path = path, brush = brush)
    drawPath(
        path = path,
        color = palette.ridge.copy(alpha = 0.5f),
        style = Stroke(width = sx * 0.5f)
    )

    // Radial ridges — 7 lines fanning from umbo (20,7) toward rim.
    val ridgeCount = 7
    for (i in 0 until ridgeCount) {
        val t = (i - (ridgeCount - 1) / 2f) / ((ridgeCount - 1) / 2f) // -1..1
        val angleDeg = t * 55f
        val rad = Math.toRadians(angleDeg.toDouble())
        val r = 29f
        val xEnd = 20f + (sin(rad) * r).toFloat()
        val yEnd = 5f + (cos(rad) * r * 0.95f).toFloat()
        val ridgePath = Path().apply {
            moveTo(x(20f), y(7f))
            lineTo(x(xEnd), y(yEnd))
        }
        drawPath(
            path = ridgePath,
            color = palette.ridge.copy(alpha = 0.55f),
            style = Stroke(width = sx * 0.6f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }

    // Umbo highlight (small bright ellipse at top).
    drawOval(
        color = palette.a.copy(alpha = 0.7f),
        topLeft = Offset(x(17f), y(6f)),
        size = androidx.compose.ui.geometry.Size(x(6f), y(4f))
    )
}
