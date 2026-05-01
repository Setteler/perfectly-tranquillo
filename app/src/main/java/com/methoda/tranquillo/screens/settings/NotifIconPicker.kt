package com.methoda.tranquillo.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.R

data class NotifIconOption(val id: String, val label: String, val drawableRes: Int)

val NotifIconOptions: List<NotifIconOption> = listOf(
    NotifIconOption("waves", "Waves", R.drawable.ic_stat_tranquillo),
    NotifIconOption("lotus", "Lotus", R.drawable.ic_stat_lotus),
    NotifIconOption("sun",   "Sun",   R.drawable.ic_stat_sun),
    NotifIconOption("moon",  "Moon",  R.drawable.ic_stat_moon),
    NotifIconOption("drop",  "Drop",  R.drawable.ic_stat_drop)
)

@Composable
fun NotifIconRow(
    label: String,
    drawableRes: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Preview chip — primary-tinted disc with the icon, like a real notification.
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = drawableRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = CircleShape,
            color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
            modifier = Modifier.size(20.dp)
        ) {}
    }
}

@Composable
fun NotifIconPickerColumn(
    selectedId: String,
    onSelect: (String) -> Unit
) {
    Column {
        for (option in NotifIconOptions) {
            NotifIconRow(
                label = option.label,
                drawableRes = option.drawableRes,
                selected = option.id == selectedId,
                onClick = { onSelect(option.id) }
            )
        }
    }
}
