package com.methoda.tranquillo.screens.takebreak

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.data.Phase
import com.methoda.tranquillo.data.StoneKind
import com.methoda.tranquillo.screens.actions.BreakDef
import com.methoda.tranquillo.screens.actions.Breaks
import com.methoda.tranquillo.ui.components.Stone
import com.methoda.tranquillo.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * "Pause" tab — Take a break with Surprise Me + browseable list.
 * Tapping any activity (or hitting Surprise Me) opens a popup with the
 * activity's prompt; from the popup the user can Start (inline countdown)
 * or pick another. Some activities redirect to other flows (e.g. the
 * five-minute breath opens the existing box-breathing screen).
 */
@Composable
fun TakeBreakScreen(
    viewModel: AppViewModel,
    onRedirect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var preview by remember { mutableStateOf<BreakDef?>(null) }
    var inProgress by remember { mutableStateOf<BreakDef?>(null) }
    var remaining by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running, inProgress) {
        val current = inProgress ?: return@LaunchedEffect
        if (!running) return@LaunchedEffect
        while (running && remaining > 0) {
            delay(1000L)
            remaining -= 1
        }
        if (remaining <= 0 && running) {
            running = false
        }
    }

    val start: (BreakDef) -> Unit = { b ->
        preview = null
        if (b.redirectRoute != null) {
            onRedirect(b.redirectRoute)
        } else {
            inProgress = b
            remaining = b.durationSec
            running = true
        }
    }

    val finish: () -> Unit = {
        val c = inProgress
        if (c != null) {
            viewModel.addStone(StoneKind.Coral, source = "break")
            viewModel.addResourceFill(c.mappedResource, Phase.Pm, 0.22f)
        }
        inProgress = null
        running = false
    }

    if (inProgress != null) {
        BreakCountdown(
            breakDef = inProgress!!,
            remaining = remaining,
            running = running,
            onComeBack = {
                running = false
                inProgress = null
            },
            onFinish = finish
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = Dimens.ScreenHorizontalPadding,
                vertical = Dimens.ScreenVerticalPadding
            )
    ) {
        TakeBreakHeader()
        Spacer(Modifier.height(14.dp))

        SurpriseMeCard(onClick = {
            preview = Breaks[Random.nextInt(Breaks.size)]
        })
        Spacer(Modifier.height(18.dp))

        Text(
            text = "OR PICK ONE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (b in Breaks) {
                BreakRow(def = b, onClick = { preview = b })
            }
        }

        Spacer(Modifier.height(Dimens.ScreenBottomClearance))
    }

    val previewing = preview
    if (previewing != null) {
        BreakPreviewDialog(
            def = previewing,
            onStart = { start(previewing) },
            onPickAnother = {
                preview = Breaks[Random.nextInt(Breaks.size)]
            },
            onDismiss = { preview = null }
        )
    }
}

@Composable
private fun TakeBreakHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "PAUSE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Take a break",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "time to relax, whatever you like",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun SurpriseMeCard(onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "surprise-spin")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )
    val accent = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = accent.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(56.dp)
                        .graphicsLayer { rotationZ = angle }
                ) {
                    val r = size.minDimension / 2f
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                accent,
                                accent.copy(alpha = 0.6f),
                                accent.copy(alpha = 0.9f),
                                accent
                            ),
                            center = Offset(r, r)
                        ),
                        radius = r,
                        center = Offset(r, r)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = accent
                    )
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Surprise me",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "let the app pick something kind",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = accent
            )
        }
    }
}

@Composable
private fun BreakRow(
    def: BreakDef,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Stone(
                kind = def.stoneColor,
                size = 36.dp,
                seed = def.id.hashCode()
            )
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = def.title,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = def.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BreakPreviewDialog(
    def: BreakDef,
    onStart: () -> Unit,
    onPickAnother: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = def.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = def.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Text(
                text = def.prompt,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onStart) {
                Text(
                    text = if (def.redirectRoute != null) "Open" else "Start",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onPickAnother) { Text("Pick another") }
                TextButton(onClick = onDismiss)     { Text("Close") }
            }
        }
    )
}

@Composable
private fun BreakCountdown(
    breakDef: BreakDef,
    remaining: Int,
    running: Boolean,
    onComeBack: () -> Unit,
    onFinish: () -> Unit
) {
    val progress = if (breakDef.durationSec > 0) {
        1f - remaining.toFloat() / breakDef.durationSec
    } else 1f
    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenHorizontalPadding, vertical = Dimens.ScreenVerticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TAKING A BREAK",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
        Text(
            text = breakDef.title,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val stroke = 8.dp.toPx()
                val inset = stroke / 2f
                val ringSize = Size(size.width - stroke, size.height - stroke)
                drawArc(
                    color = accent.copy(alpha = 0.15f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = ringSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = ringSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val (display, unit) = formatRemaining(remaining)
                Text(
                    text = display,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "“${breakDef.prompt}”",
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 20.sp),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
        )

        Spacer(Modifier.height(28.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(100.dp),
            color = if (remaining <= 0) accent
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
            border = BorderStroke(
                1.dp,
                if (remaining <= 0) accent
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            ),
            onClick = if (remaining <= 0) onFinish else onComeBack
        ) {
            Text(
                text = if (remaining <= 0) "Done" else "I'm back",
                style = MaterialTheme.typography.titleLarge,
                color = if (remaining <= 0) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
            )
        }

        Spacer(Modifier.height(Dimens.ScreenBottomClearance))
    }
}

private fun formatRemaining(seconds: Int): Pair<String, String> {
    return if (seconds >= 60) {
        val min = seconds / 60
        val sec = seconds % 60
        "$min:${sec.toString().padStart(2, '0')}" to "MIN"
    } else {
        seconds.toString() to "SECONDS"
    }
}
