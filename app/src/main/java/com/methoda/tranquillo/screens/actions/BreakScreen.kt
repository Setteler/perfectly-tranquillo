package com.methoda.tranquillo.screens.actions

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.methoda.tranquillo.ui.components.Stone
import com.methoda.tranquillo.ui.theme.Coral
import com.methoda.tranquillo.ui.theme.Sand
import com.methoda.tranquillo.ui.theme.Sky
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class BreakFilter(val label: String, val kind: BreakKind?) {
    All("All", null),
    Tiny("Tiny", BreakKind.Tiny),
    Sensory("Sensory", BreakKind.Sensory),
    Body("Body", BreakKind.Body),
    World("World", BreakKind.World)
}

/**
 * Take a break — list view with category filter + Surprise-Me hero, plus a
 * countdown sub-view for the chosen break. Finishing a break awards a Jade
 * stone and fills the break's mapped Satir resource (+0.22) on PM.
 */
@Composable
fun BreakScreen(
    viewModel: AppViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filter by remember { mutableStateOf(BreakFilter.All) }
    var chosen by remember { mutableStateOf<BreakDef?>(null) }
    var remaining by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running, chosen) {
        val current = chosen ?: return@LaunchedEffect
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
        chosen = b
        remaining = b.durationSec
        running = true
    }

    val finish: () -> Unit = {
        val c = chosen
        if (c != null) {
            viewModel.addStone(StoneKind.Coral, source = "break")
            viewModel.addResourceFill(c.mappedResource, Phase.Pm, 0.22f)
        }
        onClose()
    }

    val visible = Breaks.filter { filter.kind == null || it.kind == filter.kind }

    if (chosen != null) {
        BreakCountdown(
            breakDef = chosen!!,
            remaining = remaining,
            running = running,
            onComeBack = {
                running = false
                chosen = null
            },
            onFinish = finish,
            onClose = onClose
        )
        return
    }

    ActionScaffold(
        eyebrow = "sixty gentle seconds",
        title = "Take a break",
        onClose = onClose,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Small resets beat heroic ones. Pick one, or let the app pick.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            SurpriseMeCard(
                filterLabel = filter.label.lowercase(),
                onClick = {
                    val pool = if (visible.isNotEmpty()) visible else Breaks
                    start(pool[Random.nextInt(pool.size)])
                }
            )

            Spacer(Modifier.height(16.dp))

            // Filter chips — horizontally scrollable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BreakFilter.values().forEach { f ->
                    val count = if (f.kind == null) Breaks.size else Breaks.count { it.kind == f.kind }
                    FilterChip(
                        label = f.label,
                        count = count,
                        selected = filter == f,
                        onClick = { filter = f }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Break list
            visible.forEach { b ->
                BreakRow(
                    def = b,
                    onClick = { start(b) },
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SurpriseMeCard(
    filterLabel: String,
    onClick: () -> Unit
) {
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Sand.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, Sand.copy(alpha = 0.5f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(52.dp)
                        .graphicsLayer { rotationZ = angle }
                ) {
                    val r = size.minDimension / 2f
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(Sand, Sky, Coral, Sand),
                            center = Offset(r, r)
                        ),
                        radius = r,
                        center = Offset(r, r)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B2A3A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Sand
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
                    text = "a random break from $filterLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Sand
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (selected) Sky.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        border = BorderStroke(
            1.dp,
            if (selected) Sky.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun BreakCountdown(
    breakDef: BreakDef,
    remaining: Int,
    running: Boolean,
    onComeBack: () -> Unit,
    onFinish: () -> Unit,
    onClose: () -> Unit
) {
    val progress = if (breakDef.durationSec > 0) {
        1f - remaining.toFloat() / breakDef.durationSec
    } else 1f

    ActionScaffold(
        eyebrow = "taking a break",
        title = breakDef.title,
        onClose = onClose
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))

            // Progress ring
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(240.dp)) {
                    val stroke = 8.dp.toPx()
                    val inset = stroke / 2f
                    val ringSize = Size(size.width - stroke, size.height - stroke)
                    drawArc(
                        color = Sky.copy(alpha = 0.15f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = ringSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
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
                    Text(
                        text = remaining.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "SECONDS",
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

            if (remaining <= 0) {
                PrimaryActionButton(
                    text = "Done",
                    onClick = onFinish
                )
            } else {
                PrimaryActionButton(
                    text = "I'm back",
                    onClick = onComeBack,
                    variant = PrimaryActionVariant.Ghost,
                    trailing = null
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
