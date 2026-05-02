package com.methoda.tranquillo.screens.actions

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.data.StoneKind
import com.methoda.tranquillo.ui.theme.Sand
import com.methoda.tranquillo.ui.theme.Sky
import kotlinx.coroutines.delay

private val BreathPhaseLabels = listOf("Inhale", "Hold", "Exhale", "Hold")

/**
 * Box-breathing orb — 4s inhale (grow) / 4s hold / 4s exhale (shrink) / 4s hold.
 * Picker selects total length: 1 / 3 / 5 minutes. Hitting the target awards a
 * Moon stone and fills emotional.pm +0.3.
 */
@Composable
fun BreathScreen(
    viewModel: AppViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chosenMinutes by remember { mutableIntStateOf(3) }
    var running by remember { mutableStateOf(false) }
    // Elapsed seconds this session. Resets to 0 when the user presses Begin.
    var elapsedSec by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    val totalSec = chosenMinutes * 60

    // Simple 1 Hz tick while running — mirrors the prototype's setInterval.
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (running) {
            delay(1000L)
            elapsedSec += 1
            if (elapsedSec >= totalSec) {
                running = false
                finished = true
                viewModel.addStone(StoneKind.Moon, source = "breath")
                break
            }
        }
    }

    // Phase 0..3 derived from elapsed seconds — each phase is 4 s long.
    val phaseIdx by remember {
        derivedStateOf { ((elapsedSec / 4) % 4).coerceIn(0, 3) }
    }
    val phaseLabel = BreathPhaseLabels[phaseIdx]
    // Target scale per phase. Match prototype: 1.25 during inhale + top hold,
    // 0.8 during exhale + bottom hold. When idle, rest at 1.0.
    val targetScale = when {
        !running -> 1.0f
        phaseIdx == 0 || phaseIdx == 1 -> 1.18f
        else -> 0.82f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
        label = "breath-scale"
    )

    val remaining = (totalSec - elapsedSec).coerceAtLeast(0)
    val mm = remaining / 60
    val ss = remaining % 60

    ActionScaffold(
        eyebrow = "box breathing · 4·4·4·4",
        title = "Breath",
        onClose = onClose,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Session-length picker (disabled while running)
            Row(
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 3, 5).forEach { m ->
                    PresetChip(
                        label = "$m min",
                        selected = chosenMinutes == m,
                        onClick = {
                            if (!running) {
                                chosenMinutes = m
                                elapsedSec = 0
                                finished = false
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Orb
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(280.dp)) {
                    val r = size.minDimension * 0.42f
                    drawCircle(
                        color = Sky.copy(alpha = 0.16f),
                        radius = r,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                }
                // Breathing orb
                Canvas(
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.minDimension / 2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE8F1F4),
                                Sky,
                                Color(0xFF3A5166)
                            ),
                            center = Offset(cx * 0.7f, cy * 0.7f),
                            radius = r
                        ),
                        radius = r,
                        center = Offset(cx, cy)
                    )
                }
                // Center count (seconds remaining in the *phase*)
                val phaseSec = 4 - (elapsedSec % 4)
                Text(
                    text = if (running) phaseSec.toString() else "—",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                    color = Color(0xFF0A1220)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = if (running) phaseLabel else if (finished) "done, gently" else "ready when you are",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "%d:%02d remaining · 4 seconds per phase".format(mm, ss),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(26.dp))

            if (finished) {
                PrimaryActionButton(
                    text = "Return",
                    onClick = onClose
                )
            } else if (!running) {
                PrimaryActionButton(
                    text = "Begin",
                    onClick = {
                        elapsedSec = 0
                        running = true
                    }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(Modifier.weight(1f)) {
                        PrimaryActionButton(
                            text = "Pause",
                            onClick = { running = false },
                            variant = PrimaryActionVariant.Ghost,
                            trailing = null
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        PrimaryActionButton(
                            text = "End",
                            onClick = {
                                running = false
                                viewModel.addStone(StoneKind.Moon, source = "breath")
                                onClose()
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
