package com.methoda.tranquillo.screens.actions

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.data.Phase
import com.methoda.tranquillo.data.ResourceKey
import com.methoda.tranquillo.data.StoneKind
import com.methoda.tranquillo.ui.theme.Sand
import com.methoda.tranquillo.ui.theme.Sky
import kotlinx.coroutines.delay

/**
 * Focus countdown timer — 4 chip presets (10 / 15 / 25 / 45 minutes).
 * Big 280 dp circular progress ring with MM:SS mono text in the center and
 * Play/Pause/End controls. Reaching zero (or ending early) awards a Deep
 * stone and fills physical.pm +0.3, intellectual.pm +0.2.
 */
@Composable
fun FocusScreen(
    viewModel: AppViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chosenMinutes by remember { mutableIntStateOf(25) }
    var running by remember { mutableStateOf(false) }
    var remainingSec by remember { mutableIntStateOf(25 * 60) }
    var finished by remember { mutableStateOf(false) }

    // Reset remaining when minutes change while idle.
    LaunchedEffect(chosenMinutes, running) {
        if (!running && !finished) remainingSec = chosenMinutes * 60
    }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (running && remainingSec > 0) {
            delay(1000L)
            remainingSec -= 1
        }
        if (remainingSec <= 0 && running) {
            running = false
            finished = true
            viewModel.addStone(StoneKind.Deep, source = "focus")
            viewModel.addResourceFill(ResourceKey.Physical, Phase.Pm, 0.3f)
            viewModel.addResourceFill(ResourceKey.Intellectual, Phase.Pm, 0.2f)
        }
    }

    val totalSec = chosenMinutes * 60
    val progress = if (totalSec > 0) 1f - remainingSec.toFloat() / totalSec else 0f
    val mm = String.format("%02d", remainingSec / 60)
    val ss = String.format("%02d", remainingSec % 60)

    ActionScaffold(
        eyebrow = "focus session",
        title = "One thing, quietly",
        onClose = onClose,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))

            // Progress ring
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(280.dp)) {
                    val stroke = 10.dp.toPx()
                    val inset = stroke / 2f
                    val ringSize = Size(size.width - stroke, size.height - stroke)
                    // Track
                    drawArc(
                        color = Sky.copy(alpha = 0.18f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = ringSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    // Progress
                    drawArc(
                        color = Sand,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = ringSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mm,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 56.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 56.sp),
                            color = Sky,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Text(
                            text = ss,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 56.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = when {
                            finished -> "tended"
                            running -> "tending"
                            else -> "$chosenMinutes minute session"
                        }.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            if (!running && !finished) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 15, 25, 45).forEach { m ->
                        PresetChip(
                            label = "$m min",
                            selected = chosenMinutes == m,
                            onClick = { chosenMinutes = m }
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            when {
                finished -> {
                    PrimaryActionButton(
                        text = "Return",
                        onClick = onClose
                    )
                }
                !running -> {
                    PrimaryActionButton(
                        text = "Begin focus",
                        onClick = { running = true }
                    )
                }
                else -> {
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
                                text = "End early",
                                onClick = {
                                    running = false
                                    viewModel.addStone(StoneKind.Deep, source = "focus")
                                    viewModel.addResourceFill(ResourceKey.Physical, Phase.Pm, 0.3f * progress.coerceAtLeast(0.4f))
                                    viewModel.addResourceFill(ResourceKey.Intellectual, Phase.Pm, 0.2f * progress.coerceAtLeast(0.4f))
                                    onClose()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "phone silenced · notifications held · mandala tended",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}
