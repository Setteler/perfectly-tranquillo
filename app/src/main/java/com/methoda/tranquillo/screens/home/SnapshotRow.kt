package com.methoda.tranquillo.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.ui.theme.Sand
import com.methoda.tranquillo.ui.theme.Sky

/**
 * 3 small glass pills showing today's numbers:
 * petals filled, habits done, streak days.
 */
@Composable
fun SnapshotRow(
    petalsFilled: Int,
    petalsTotal: Int,
    habitsDone: Int,
    habitsTotal: Int,
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Pill(
            value = "$petalsFilled/$petalsTotal",
            label = "petals",
            accent = Sky,
            modifier = Modifier.weight(1f)
        )
        Pill(
            value = "$habitsDone/$habitsTotal",
            label = "habits",
            accent = Color(0xFF86B097),
            modifier = Modifier.weight(1f)
        )
        Pill(
            value = "$streakDays",
            label = if (streakDays == 1) "day streak" else "day streak",
            accent = Sand,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun Pill(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
