package com.methoda.tranquillo.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Full-round outlined pill showing the current intention. Uses the theme
 * primary so it pops on both dark (sky) and light (deep ocean blue) bgs.
 * When intent is blank we render a hint and a muted dot.
 */
@Composable
fun IntentionPill(
    intent: String,
    modifier: Modifier = Modifier,
    hint: String = "set a gentle intention",
    onClick: (() -> Unit)? = null
) {
    val isEmpty = intent.isBlank()
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(100.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = if (isEmpty) 0.40f else 0.55f)),
        onClick = onClick ?: {},
        enabled = onClick != null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = if (isEmpty) 0.65f else 1.0f))
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "INTENTION",
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (isEmpty) hint else intent,
                style = MaterialTheme.typography.displaySmall,
                color = if (isEmpty) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
