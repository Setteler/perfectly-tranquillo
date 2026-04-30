package com.methoda.tranquillo.screens.garden

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.MandalaEntryEntity
import com.methoda.tranquillo.data.Phase
import com.methoda.tranquillo.data.ResourceKey
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Archive — one collapsible card per Satir resource (Body, Mind, Heart…).
 * Tap a category to expand it; only one is open at a time. Inside an open
 * category, entries split into "Strengths" (kind=resource) and "Challenges"
 * (kind=challenge), each sorted newest-first.
 */
@Composable
fun ArchiveCard(
    entries: List<MandalaEntryEntity>,
    todayIso: String,
    modifier: Modifier = Modifier
) {
    val byCategory = remember(entries) { groupByCategory(entries) }

    var expandedKey by remember { mutableStateOf<ResourceKey?>(null) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "ARCHIVE · LAST 30 DAYS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            if (entries.none { it.text.isNotBlank() }) {
                Text(
                    text = "your past entries will appear here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (key in ResourceKey.orderedClockwise) {
                        val cat = byCategory[key]
                        CategoryAccordion(
                            key = key,
                            strengths = cat?.strengths.orEmpty(),
                            challenges = cat?.challenges.orEmpty(),
                            todayIso = todayIso,
                            expanded = expandedKey == key,
                            onToggle = {
                                expandedKey = if (expandedKey == key) null else key
                            }
                        )
                    }
                }
            }
        }
    }
}

private data class CategoryEntries(
    val strengths: List<MandalaEntryEntity>,
    val challenges: List<MandalaEntryEntity>
)

private fun groupByCategory(entries: List<MandalaEntryEntity>): Map<ResourceKey, CategoryEntries> {
    val out = mutableMapOf<ResourceKey, CategoryEntries>()
    val byKey = entries
        .filter { it.text.isNotBlank() }
        .groupBy { runCatching { ResourceKey.valueOf(it.key) }.getOrNull() }
    for ((key, rows) in byKey) {
        if (key == null) continue
        val (strengths, challenges) = rows.partition { it.kind == "resource" }
        out[key] = CategoryEntries(
            strengths = strengths.sortedByDescending { it.date },
            challenges = challenges.sortedByDescending { it.date }
        )
    }
    return out
}

@Composable
private fun CategoryAccordion(
    key: ResourceKey,
    strengths: List<MandalaEntryEntity>,
    challenges: List<MandalaEntryEntity>,
    todayIso: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val total = strengths.size + challenges.size
    val isEmpty = total == 0

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "chevronRotation"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
        border = BorderStroke(
            1.dp,
            if (expanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isEmpty) { onToggle() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = key.label,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isEmpty) "no entries yet"
                               else "$total ${if (total == 1) "entry" else "entries"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!isEmpty) {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(220)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(220)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                ) {
                    if (strengths.isNotEmpty()) {
                        SubsectionHeader(label = "STRENGTHS")
                        EntryList(entries = strengths, todayIso = todayIso, dim = false)
                    }
                    if (strengths.isNotEmpty() && challenges.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                    }
                    if (challenges.isNotEmpty()) {
                        SubsectionHeader(label = "CHALLENGES")
                        EntryList(entries = challenges, todayIso = todayIso, dim = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubsectionHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun EntryList(
    entries: List<MandalaEntryEntity>,
    todayIso: String,
    dim: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (entry in entries) {
            EntryRow(entry = entry, todayIso = todayIso, dim = dim)
        }
    }
}

@Composable
private fun EntryRow(
    entry: MandalaEntryEntity,
    todayIso: String,
    dim: Boolean
) {
    val phaseLabel = if (Phase.fromTag(entry.phase) == Phase.Am) "AM" else "PM"
    val dateLabel = remember(entry.date, todayIso) { formatDateLabel(entry.date, todayIso) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(0.32f)) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = phaseLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Text(
            text = entry.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(
                alpha = if (dim) 0.65f else 0.90f
            ),
            modifier = Modifier
                .weight(0.68f)
                .padding(start = 6.dp)
        )
    }
}

private fun formatDateLabel(iso: String, todayIso: String): String {
    if (iso == todayIso) return "today"
    return runCatching {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getDefault() }
        val display = SimpleDateFormat("MMM d", Locale.US).apply { timeZone = TimeZone.getDefault() }
        display.format(parser.parse(iso) ?: return iso)
    }.getOrDefault(iso)
}
