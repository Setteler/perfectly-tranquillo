package com.methoda.tranquillo.screens.garden

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.MandalaEntryEntity
import com.methoda.tranquillo.data.Phase
import com.methoda.tranquillo.data.ResourceKey
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Card listing past mandala entries grouped by date (newest first), capped
 * at the last 30 days by the caller.
 *
 * Each entry: resource label · AM/PM tag · entry text. Empty entries are
 * filtered out. Days with no entries don't render a header.
 */
@Composable
fun ArchiveCard(
    entries: List<MandalaEntryEntity>,
    todayIso: String,
    modifier: Modifier = Modifier
) {
    val grouped = remember(entries) {
        entries
            .filter { it.text.isNotBlank() }
            .groupBy { it.date }
            .toSortedMap(compareByDescending { it })
    }

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
            if (grouped.isEmpty()) {
                Text(
                    text = "your past entries will appear here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                for ((date, dayEntries) in grouped) {
                    DayHeader(date = date, isToday = date == todayIso)
                    Spacer(Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (entry in dayEntries) {
                            EntryRow(entry)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun DayHeader(date: String, isToday: Boolean) {
    val display = remember(date, isToday) { formatDateLabel(date, isToday) }
    Text(
        text = display.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun EntryRow(entry: MandalaEntryEntity) {
    val key = runCatching { ResourceKey.valueOf(entry.key) }.getOrNull() ?: return
    val phaseLabel = if (Phase.fromTag(entry.phase) == Phase.Am) "AM" else "PM"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "${key.label} · $phaseLabel",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(end = 10.dp)
        )
        Text(
            text = entry.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(
                alpha = if (entry.kind == "challenge") 0.55f else 0.85f
            )
        )
    }
}

private fun formatDateLabel(iso: String, isToday: Boolean): String {
    if (isToday) return "today"
    return runCatching {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getDefault() }
        val display = SimpleDateFormat("EEE · MMM d", Locale.US).apply { timeZone = TimeZone.getDefault() }
        display.format(parser.parse(iso) ?: return iso)
    }.getOrDefault(iso)
}

