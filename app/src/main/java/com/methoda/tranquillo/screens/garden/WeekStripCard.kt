package com.methoda.tranquillo.screens.garden

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.AmPmFill
import com.methoda.tranquillo.data.ResourceKey
import com.methoda.tranquillo.ui.components.MiniMandala
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Horizontal strip of 7 mini-mandalas — one per day in the rolling 7-day
 * window ending today. Days with no entries render an empty mandala.
 */
@Composable
fun WeekStripCard(
    fillsByDate: Map<String, Map<ResourceKey, AmPmFill>>,
    todayIso: String,
    modifier: Modifier = Modifier
) {
    val days = remember7Days(todayIso)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "PAST 7 DAYS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (day in days) {
                    DayCell(
                        dayLabel = day.label,
                        isToday = day.iso == todayIso,
                        fills = fillsByDate[day.iso] ?: emptyMap()
                    )
                }
            }
        }
    }
}

private data class DayCellData(val iso: String, val label: String)

@androidx.compose.runtime.Composable
private fun remember7Days(todayIso: String): List<DayCellData> = androidx.compose.runtime.remember(todayIso) {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getDefault() }
    val labels = SimpleDateFormat("EEE", Locale.US).apply { timeZone = TimeZone.getDefault() }
    val cal = Calendar.getInstance()
    runCatching { cal.time = df.parse(todayIso) ?: return@runCatching }
    cal.add(Calendar.DAY_OF_YEAR, -6)
    val out = mutableListOf<DayCellData>()
    repeat(7) {
        val iso = df.format(cal.time)
        val label = labels.format(cal.time).uppercase().take(1) // M T W T F S S
        out.add(DayCellData(iso, label))
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    out
}

@Composable
private fun DayCell(
    dayLabel: String,
    isToday: Boolean,
    fills: Map<ResourceKey, AmPmFill>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MiniMandala(resources = fills, size = 32.dp)
        Box(modifier = Modifier.height(6.dp))
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (isToday) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
