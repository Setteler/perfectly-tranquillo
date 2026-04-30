package com.methoda.tranquillo.screens.actions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.data.Phase
import com.methoda.tranquillo.data.ResourceKey
import com.methoda.tranquillo.data.StoneKind
import com.methoda.tranquillo.ui.theme.Sand
import com.methoda.tranquillo.ui.theme.Sky

private enum class Mood(val label: String, val icon: ImageVector, val accent: Color) {
    Bright("bright",   Icons.Outlined.WbSunny,      Sand),
    Cloudy("cloudy",   Icons.Outlined.Cloud,        Sky),
    Heavy("heavy",     Icons.Outlined.Thunderstorm, Color(0xFF7C909A)),
    Curious("curious", Icons.Outlined.HelpOutline,  Color(0xFFC8A98A))
}

private val IntentionPresets = listOf(
    "Gentle focus",
    "Stay curious",
    "Slow down",
    "Be kind to yourself",
    "Breathe through it",
    "One thing at a time",
    "Soft today",
    "Notice the small good"
)

/**
 * Morning intention flow. 3 steps:
 *   0 — mood (2x2 grid)
 *   1 — intention (chips + free text)
 *   2 — optional "one good thing ahead" + set button
 * On finish: mood/intent/goodThing saved to VM, morningDone=true, a Moon stone
 * is awarded, and intellectual.am is bumped +0.4 (capped by the merge).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MorningScreen(
    viewModel: AppViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(0) }
    var mood by remember { mutableStateOf<Mood?>(null) }
    var intent by remember { mutableStateOf("") }
    var goodThing by remember { mutableStateOf("") }

    ActionScaffold(
        eyebrow = "your morning",
        title = "Begin gently",
        onClose = onClose,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            when (step) {
                0 -> MoodStep(
                    selected = mood,
                    onPick = { mood = it },
                    onNext = { if (mood != null) step = 1 }
                )
                1 -> IntentStep(
                    intent = intent,
                    onIntent = { intent = it },
                    onBack = { step = 0 },
                    onNext = { step = 2 }
                )
                else -> GoodThingStep(
                    goodThing = goodThing,
                    onGoodThing = { goodThing = it },
                    onBack = { step = 1 },
                    onFinish = {
                        mood?.let { viewModel.setMorningMood(it.label) }
                        if (intent.isNotBlank()) viewModel.setIntent(intent.trim())
                        if (goodThing.isNotBlank()) viewModel.setGoodThing(goodThing.trim())
                        viewModel.setMorningDone(true)
                        viewModel.addStone(StoneKind.Moon, source = "morning")
                        viewModel.addResourceFill(ResourceKey.Intellectual, Phase.Am, 0.4f)
                        onClose()
                    }
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun MoodStep(
    selected: Mood?,
    onPick: (Mood) -> Unit,
    onNext: () -> Unit
) {
    Column {
        Text(
            text = "How does the day feel, right now?",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "No wrong answer. Pick what sits closest.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
        )

        val rows = Mood.values().toList().chunked(2)
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { m ->
                    MoodCard(
                        mood = m,
                        selected = selected == m,
                        onClick = { onPick(m) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        PrimaryActionButton(
            text = if (selected == null) "Pick one" else "Continue",
            onClick = onNext,
            enabled = selected != null
        )
    }
}

@Composable
private fun MoodCard(
    mood: Mood,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(126.dp),
        shape = RoundedCornerShape(22.dp),
        color = if (selected) mood.accent.copy(alpha = 0.22f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        border = BorderStroke(
            1.dp,
            if (selected) mood.accent.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = mood.icon,
                contentDescription = null,
                tint = mood.accent,
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = mood.label,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntentStep(
    intent: String,
    onIntent: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column {
        Text(
            text = "What would help today?",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "A short phrase, just for today. Tap one or write your own.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IntentionPresets.forEach { preset ->
                PresetChip(
                    label = preset,
                    selected = intent.trim().equals(preset, ignoreCase = true),
                    onClick = { onIntent(preset) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {
            BasicTextField(
                value = intent,
                onValueChange = onIntent,
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .wrapContentHeight(),
                decorationBox = { inner ->
                    if (intent.isEmpty()) {
                        Text(
                            text = "write your own…",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    }
                    inner()
                }
            )
        }

        Spacer(Modifier.height(18.dp))
        PrimaryActionButton(
            text = if (intent.isBlank()) "Skip for now" else "Continue",
            onClick = onNext,
            variant = if (intent.isBlank()) PrimaryActionVariant.Ghost else PrimaryActionVariant.Sand
        )
        Spacer(Modifier.height(10.dp))
        PrimaryActionButton(
            text = "Back",
            onClick = onBack,
            variant = PrimaryActionVariant.Ghost,
            trailing = null
        )
    }
}

@Composable
private fun GoodThingStep(
    goodThing: String,
    onGoodThing: (String) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    Column {
        Text(
            text = "One small good thing ahead?",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Even a cup of tea counts. Skip if you like.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {
            BasicTextField(
                value = goodThing,
                onValueChange = onGoodThing,
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .wrapContentHeight(),
                decorationBox = { inner ->
                    if (goodThing.isEmpty()) {
                        Text(
                            text = "today I'm looking forward to…",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    }
                    inner()
                }
            )
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = "you'll collect a moon stone for this morning",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        PrimaryActionButton(
            text = if (goodThing.isBlank()) "Begin the day" else "Set it",
            onClick = onFinish
        )
        Spacer(Modifier.height(10.dp))
        PrimaryActionButton(
            text = "Back",
            onClick = onBack,
            variant = PrimaryActionVariant.Ghost,
            trailing = null
        )
    }
}

@Composable
internal fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (selected) Sky.copy(alpha = 0.22f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        border = BorderStroke(
            1.dp,
            if (selected) Sky.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
        ),
        onClick = onClick
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
