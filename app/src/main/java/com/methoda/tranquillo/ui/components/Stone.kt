package com.methoda.tranquillo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.min

enum class StoneKind { Moon, Jade, Shell, Coral, Sand, Deep }

private data class StonePalette(val a: Color, val b: Color, val c: Color)

private fun paletteFor(kind: StoneKind): StonePalette = when (kind) {
    StoneKind.Moon  -> StonePalette(Color(0xFFF5EFD8), Color(0xFFE0D7B5), Color(0xFF998E66))
    StoneKind.Jade  -> StonePalette(Color(0xFFC8E5D2), Color(0xFF86B097), Color(0xFF4D7563))
    StoneKind.Shell -> StonePalette(Color(0xFFF2E6D5), Color(0xFFD2BFA7), Color(0xFF8C7A65))
    StoneKind.Coral -> StonePalette(Color(0xFFF4C9B5), Color(0xFFD89778), Color(0xFF9D5F47))
    StoneKind.Sand  -> StonePalette(Color(0xFFEAD9B0), Color(0xFFC9B58D), Color(0xFF7E6E50))
    StoneKind.Deep  -> StonePalette(Color(0xFFB5C4CC), Color(0xFF7C909A), Color(0xFF43545C))
}

/**
 * Organic oval pebble shape with asymmetric border radii, analogous to CSS
 * `border-radius: 50% 48% 52% 50% / 50% 52% 48% 50%`.
 *
 * We reproduce that via 4 cubic beziers — corner control points are offset
 * according to the per-corner radii so the silhouette looks asymmetric.
 */
private class PebbleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height

        // Per CSS: horizontal radii 50/48/52/50 (TL TR BR BL) and vertical 50/52/48/50.
        val rhTL = w * 0.50f; val rvTL = h * 0.50f
        val rhTR = w * 0.48f; val rvTR = h * 0.52f
        val rhBR = w * 0.52f; val rvBR = h * 0.48f
        val rhBL = w * 0.50f; val rvBL = h * 0.50f

        // Bezier "circle constant" for approximating an ellipse corner with cubics.
        val k = 0.5522847f

        val path = Path()
        // start at top edge between TL and TR — midpoint top = (w/2, 0)
        path.moveTo(w * 0.5f, 0f)
        // top-right corner: from (w-rhTR, 0) -> (w, rvTR)
        path.cubicTo(
            w * 0.5f + rhTR * k, 0f,
            w, rvTR - rvTR * k,
            w, rvTR
        )
        // right edge midpoint -> bottom-right corner
        path.cubicTo(
            w, rvTR + (h - rvTR - (h - rvBR)) * 0.5f, // not used precisely
            w, h - rvBR + rvBR * k,
            w - rhBR + rhBR, h  // stay at right edge into bottom
        )
        // Simpler 4-arc rebuild for correctness ---------------------------
        // The above gets complex; clear and rebuild using a straightforward
        // "ellipse-by-4-cubics" with per-corner ellipse radii.
        path.reset()

        // Anchor points on axes (midpoints of each side)
        val top = Offset(w * 0.5f, 0f)
        val right = Offset(w, h * 0.5f)
        val bottom = Offset(w * 0.5f, h)
        val left = Offset(0f, h * 0.5f)

        // Per-corner cubic control offsets
        // TL quadrant uses rhTL (horizontal radius from left) and rvTL (vertical radius from top)
        path.moveTo(top.x, top.y)
        // TR corner: top -> right
        path.cubicTo(
            w * 0.5f + rhTR * k, 0f,
            w, h * 0.5f - rvTR * k,
            right.x, right.y
        )
        // BR corner: right -> bottom
        path.cubicTo(
            w, h * 0.5f + rvBR * k,
            w * 0.5f + rhBR * k, h,
            bottom.x, bottom.y
        )
        // BL corner: bottom -> left
        path.cubicTo(
            w * 0.5f - rhBL * k, h,
            0f, h * 0.5f + rvBL * k,
            left.x, left.y
        )
        // TL corner: left -> top
        path.cubicTo(
            0f, h * 0.5f - rvTL * k,
            w * 0.5f - rhTL * k, 0f,
            top.x, top.y
        )
        path.close()
        return Outline.Generic(path)
    }
}

private val PebbleShapeInstance = PebbleShape()

private fun pseudoRandom01(seed: Int): Float {
    val v = ((seed.toLong() * 9301L + 49297L) % 233280L + 233280L) % 233280L
    return v.toFloat() / 233280f
}

@Composable
fun Stone(
    kind: StoneKind,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    dim: Boolean = false,
    seed: Int = 0
) {
    val palette = remember(kind) { paletteFor(kind) }

    val s = remember(seed) { pseudoRandom01(seed) }
    val ratio = remember(seed) { 0.82f + s * 0.14f }           // 0.82 .. 0.96
    val tiltDeg = remember(seed) { (-18 + ((seed * 37) % 36)).toFloat() } // -18 .. +18
    val hlX = remember(seed) { (26 + ((seed * 13) % 18)) / 100f }          // 0.26 .. 0.44
    val hlY = remember(seed) { (22 + ((seed * 7) % 14)) / 100f }           // 0.22 .. 0.36

    val widthDp = size
    val heightDp = size * ratio

    val showSpeckles = size >= 20.dp

    Box(
        modifier = modifier
            .size(width = widthDp, height = heightDp)
            .graphicsLayer {
                rotationZ = tiltDeg
                alpha = if (dim) 0.3f else 1f
            }
            .shadow(
                elevation = 4.dp,
                shape = PebbleShapeInstance,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.28f),
                spotColor = Color.Black.copy(alpha = 0.28f)
            )
    ) {
        Canvas(modifier = Modifier.size(widthDp, heightDp)) {
            val w = this.size.width
            val h = this.size.height
            val path = Path().apply {
                val rhTL = w * 0.50f; val rvTL = h * 0.50f
                val rhTR = w * 0.48f; val rvTR = h * 0.52f
                val rhBR = w * 0.52f; val rvBR = h * 0.48f
                val rhBL = w * 0.50f; val rvBL = h * 0.50f
                val k = 0.5522847f
                moveTo(w * 0.5f, 0f)
                cubicTo(
                    w * 0.5f + rhTR * k, 0f,
                    w, h * 0.5f - rvTR * k,
                    w, h * 0.5f
                )
                cubicTo(
                    w, h * 0.5f + rvBR * k,
                    w * 0.5f + rhBR * k, h,
                    w * 0.5f, h
                )
                cubicTo(
                    w * 0.5f - rhBL * k, h,
                    0f, h * 0.5f + rvBL * k,
                    0f, h * 0.5f
                )
                cubicTo(
                    0f, h * 0.5f - rvTL * k,
                    w * 0.5f - rhTL * k, 0f,
                    w * 0.5f, 0f
                )
                close()
            }

            // Layered radial gradient inside the pebble: highlight at top,
            // mid through the body, rim dark.
            val hlCenter = Offset(w * hlX, h * hlY)
            val bodyRadius = min(w, h) * 0.95f
            val brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to palette.a,
                    0.55f to palette.b,
                    1.0f to palette.c
                ),
                center = hlCenter,
                radius = bodyRadius
            )

            // body
            drawPath(path = path, brush = brush)

            // inner shadow rim — a stroke near the lower-right edge to mimic the inset box-shadow.
            drawPath(
                path = path,
                color = Color(0xFF0A1220).copy(alpha = 0.22f),
                style = Stroke(width = (min(w, h) * 0.08f))
            )
            // bright inner stroke at top-left for glossy curve
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.14f),
                style = Stroke(width = (min(w, h) * 0.04f))
            )

            // glossy top highlight spot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(w * (hlX - 0.04f), h * (hlY - 0.03f)),
                    radius = min(w, h) * 0.28f
                ),
                radius = min(w, h) * 0.28f,
                center = Offset(w * (hlX - 0.04f), h * (hlY - 0.03f))
            )

            // speckles
            if (showSpeckles) {
                val dotA = Offset(
                    x = w * ((55 + (seed % 5) * 4) / 100f),
                    y = h * ((60 + (seed % 3) * 6) / 100f)
                )
                val dotB = Offset(
                    x = w * ((30 + (seed % 4) * 3) / 100f),
                    y = h * ((70 + (seed % 2) * 5) / 100f)
                )
                drawCircle(color = palette.c.copy(alpha = 0.35f), radius = 1.2f, center = dotA)
                drawCircle(color = palette.c.copy(alpha = 0.30f), radius = 0.9f, center = dotB)
            }
        }
    }
}
