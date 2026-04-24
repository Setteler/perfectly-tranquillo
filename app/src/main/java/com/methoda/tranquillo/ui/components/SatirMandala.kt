package com.methoda.tranquillo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.AmPmFill
import com.methoda.tranquillo.data.ResourceKey
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot

private val AmBase = Color(0xFFA5C7DE)  // oklch(0.82 0.07 210) approx
private val PmBase = Color(0xFFE8C57A)  // oklch(0.86 0.09 78) approx
private val RingStroke = Color(0xFFF5F1E8).copy(alpha = 0.28f)
private val CoreColor = Color(0xFFF5F1E8).copy(alpha = 0.20f)

/**
 * Satir mandala — 8 petal wedges with AM / PM half-rings.
 * Geometry spec in docs/design/README.md § "The mandala (custom drawing)".
 */
@Composable
fun SatirMandala(
    resources: Map<ResourceKey, AmPmFill>,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    highlight: ResourceKey? = null,
    onPetalTap: ((ResourceKey) -> Unit)? = null,
    @Suppress("UNUSED_PARAMETER") showLabels: Boolean = false,
    animate: Boolean = true
) {
    val infinite = rememberInfiniteTransition(label = "mandala-breathe")
    val breathe by if (animate) {
        infinite.animateFloat(
            initialValue = 1f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "core-breathe"
        )
    } else {
        rememberConstant(1f)
    }

    val ordered = ResourceKey.orderedClockwise
    val sliceDeg = 360f / 8f
    val padDeg = 2.5f

    Canvas(
        modifier = modifier
            .size(size)
            .then(
                if (onPetalTap != null) Modifier.pointerInput(onPetalTap) {
                    detectTapGestures { offset ->
                        val cx = this.size.width / 2f
                        val cy = this.size.height / 2f
                        val dx = offset.x - cx
                        val dy = offset.y - cy
                        val r = hypot(dx, dy)
                        val outerR = this.size.width * 0.44f
                        val innerR = this.size.width * 0.18f
                        if (r in innerR..outerR) {
                            // angle: 0 at 12 o'clock, clockwise; atan2 has 0 at +x axis (3 o'clock), ccw is +.
                            // Convert: angleFrom12Clockwise = atan2(dx, -dy) in radians, normalised 0..2π.
                            var a = atan2(dx.toDouble(), -dy.toDouble()) * 180.0 / PI
                            if (a < 0) a += 360.0
                            val idx = ((a / 45.0).toInt()).coerceIn(0, 7)
                            onPetalTap(ordered[idx])
                        }
                    }
                } else Modifier
            )
    ) {
        val s = this.size.minDimension
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val outerR = s * 0.44f
        val midR = s * 0.31f
        val innerR = s * 0.18f

        val amBandWidth = midR - innerR
        val pmBandWidth = outerR - midR
        val amRadius = (innerR + midR) / 2f      // centerline for AM stroke arc
        val pmRadius = (midR + outerR) / 2f      // centerline for PM stroke arc

        // Background empty-band tint
        val emptyAmColor = AmBase.copy(alpha = 0.04f)
        val emptyPmColor = PmBase.copy(alpha = 0.04f)

        for ((i, key) in ordered.withIndex()) {
            // Start angle at 12 o'clock, clockwise.
            // Compose drawArc uses 0 = 3 o'clock, CW positive. Map: startFrom12 = i * slice ⇒ startDrawArc = i * slice - 90.
            val centerStart = i * sliceDeg - 90f
            val startA = centerStart + padDeg
            val sweep = sliceDeg - 2f * padDeg

            val fill = resources[key] ?: AmPmFill()
            val isHighlighted = highlight == key

            // Arc top-left rects (for each band's centerline radius)
            val amRect = Rect(center = center, radius = amRadius)
            val pmRect = Rect(center = center, radius = pmRadius)

            // highlight glow (under bands)
            if (isHighlighted) {
                drawArc(
                    color = AmBase.copy(alpha = 0.40f),
                    startAngle = startA - 1f,
                    sweepAngle = sweep + 2f,
                    useCenter = false,
                    topLeft = Offset(amRect.left, amRect.top),
                    size = Size(amRect.width, amRect.height),
                    style = Stroke(width = amBandWidth + 3.dp.toPx())
                )
                drawArc(
                    color = PmBase.copy(alpha = 0.40f),
                    startAngle = startA - 1f,
                    sweepAngle = sweep + 2f,
                    useCenter = false,
                    topLeft = Offset(pmRect.left, pmRect.top),
                    size = Size(pmRect.width, pmRect.height),
                    style = Stroke(width = pmBandWidth + 3.dp.toPx())
                )
            }

            // Empty tint band (AM)
            drawArc(
                color = emptyAmColor,
                startAngle = startA,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(amRect.left, amRect.top),
                size = Size(amRect.width, amRect.height),
                style = Stroke(width = amBandWidth)
            )
            // Empty tint band (PM)
            drawArc(
                color = emptyPmColor,
                startAngle = startA,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(pmRect.left, pmRect.top),
                size = Size(pmRect.width, pmRect.height),
                style = Stroke(width = pmBandWidth)
            )

            // AM fill (interpolated alpha)
            if (fill.am > 0f) {
                drawArc(
                    color = AmBase.copy(alpha = 0.85f * fill.am.coerceIn(0f, 1f)),
                    startAngle = startA,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(amRect.left, amRect.top),
                    size = Size(amRect.width, amRect.height),
                    style = Stroke(width = amBandWidth)
                )
            }
            // PM fill
            if (fill.pm > 0f) {
                drawArc(
                    color = PmBase.copy(alpha = 0.85f * fill.pm.coerceIn(0f, 1f)),
                    startAngle = startA,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(pmRect.left, pmRect.top),
                    size = Size(pmRect.width, pmRect.height),
                    style = Stroke(width = pmBandWidth)
                )
            }
        }

        // Thin ring strokes at inner/mid/outer radii
        val ringStrokePx = 1.dp.toPx()
        for (r in listOf(innerR, midR, outerR)) {
            val rect = Rect(center = center, radius = r)
            drawArc(
                color = RingStroke,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(rect.left, rect.top),
                size = Size(rect.width, rect.height),
                style = Stroke(width = ringStrokePx)
            )
        }

        // Soft pale core (animated scale via `breathe` multiplier)
        val coreR = s * 0.08f * breathe
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFF5EFD8).copy(alpha = 0.95f),
                    Color(0xFFE8C57A).copy(alpha = 0.45f),
                    Color.Transparent
                ),
                center = center,
                radius = coreR * 1.8f
            ),
            radius = coreR * 1.8f,
            center = center
        )
        drawCircle(
            color = CoreColor,
            radius = coreR,
            center = center
        )
    }
}

@Composable
private fun rememberConstant(value: Float): androidx.compose.runtime.State<Float> =
    androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(value) }
