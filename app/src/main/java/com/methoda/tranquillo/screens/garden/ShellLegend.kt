package com.methoda.tranquillo.screens.garden

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.ui.components.Seashell
import com.methoda.tranquillo.ui.components.ShellColor

/**
 * Single legend chip — small shell + label + optional count.
 * Matches prototype `ShellLegend`.
 */
@Composable
fun ShellLegend(
    color: ShellColor,
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Seashell(
            color = color,
            size = 16.dp,
            seed = label.hashCode(),
            tiltDeg = 0f
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

/** All five legend chips wrapped in a centered FlowRow. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ShellLegendRow(
    counts: Map<ShellColor, Int>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ShellLegend(ShellColor.Pearl,  "mornings", counts[ShellColor.Pearl]  ?: 0)
        ShellLegend(ShellColor.Butter, "evenings", counts[ShellColor.Butter] ?: 0)
        ShellLegend(ShellColor.Moss,   "habits",   counts[ShellColor.Moss]   ?: 0)
        ShellLegend(ShellColor.Coral,  "breaks",   counts[ShellColor.Coral]  ?: 0)
        ShellLegend(ShellColor.Sky,    "focus",    counts[ShellColor.Sky]    ?: 0)
    }
}
