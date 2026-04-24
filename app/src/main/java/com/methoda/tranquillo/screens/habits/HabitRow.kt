package com.methoda.tranquillo.screens.habits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.methoda.tranquillo.ui.components.Stone
import com.methoda.tranquillo.ui.components.StoneKind
import com.methoda.tranquillo.ui.theme.Sand

/**
 * One habit row — stone toggle on the left, label/hint in the middle,
 * streak chip + bell + remove on the right. Tapping the bell expands an
 * inline time picker (preset buttons, no native dialog).
 */
@Composable
fun HabitRow(
    label: String,
    hint: String,
    streak: Int,
    remindAt: String?,
    done: Boolean,
    stoneKind: StoneKind,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    onSetReminder: (String?) -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = label.hashCode()
) {
    var timePickerOpen by remember { mutableStateOf(false) }

    val containerColor = if (done)
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
    val borderColor = if (done)
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Stone — tap to toggle
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    onClick = onToggle
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Stone(
                            kind = stoneKind,
                            size = 30.dp,
                            dim = !done,
                            seed = seed
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 18.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (hint.isNotBlank()) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                // streak chip
                if (streak > 0) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = Sand.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, Sand.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "🌱 $streak",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Sand,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                }

                // bell (remind) toggle
                Surface(
                    shape = CircleShape,
                    color = if (remindAt != null) Sand.copy(alpha = 0.14f) else Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (remindAt != null) Sand.copy(alpha = 0.28f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    onClick = { timePickerOpen = !timePickerOpen }
                ) {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (remindAt != null) Icons.Outlined.Notifications
                            else Icons.Outlined.NotificationsOff,
                            contentDescription = if (remindAt != null) "Reminder at $remindAt"
                            else "Set reminder",
                            tint = if (remindAt != null) Sand
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.width(4.dp))

                // remove ×
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                    ),
                    onClick = onRemove
                ) {
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (remindAt != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "you'll get a soft reminder: \"it's time for $label in the garden\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }

            AnimatedVisibility(
                visible = timePickerOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    TimePresetRow(
                        current = remindAt,
                        onPick = { picked ->
                            onSetReminder(picked)
                            timePickerOpen = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimePresetRow(
    current: String?,
    onPick: (String?) -> Unit
) {
    val presets = listOf("07:00", "09:00", "12:00", "18:00", "21:00")
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        presets.forEach { t ->
            PresetChip(label = t, selected = current == t, onClick = { onPick(t) })
        }
        PresetChip(label = "clear", selected = false, onClick = { onPick(null) })
    }
}

@Composable
private fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = if (selected) Sand.copy(alpha = 0.22f)
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        border = BorderStroke(
            1.dp,
            if (selected) Sand.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        ),
        onClick = onClick
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Sand else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
