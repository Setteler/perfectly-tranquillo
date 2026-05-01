package com.methoda.tranquillo.screens.garden

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.GoodThingEntity
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * "Ser emot från" — past "Looking forward to" entries, newest first.
 * Lives in the Garden, separate from the Spiritual mandala category.
 */
@Composable
fun SerEmotFranCard(
    entries: List<GoodThingEntity>,
    todayIso: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "SER EMOT FRÅN",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "what you've been looking forward to",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(Modifier.height(12.dp))
            if (entries.isEmpty()) {
                Text(
                    text = "your daily one-liners gather here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (entry in entries) {
                        EntryRow(entry = entry, todayIso = todayIso)
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: GoodThingEntity, todayIso: String) {
    val dateLabel = remember(entry.date, todayIso) { formatDateLabel(entry.date, todayIso) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .width(86.dp)
                .padding(end = 12.dp)
        )
        Text(
            text = entry.text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.90f),
            modifier = Modifier.weight(1f)
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
