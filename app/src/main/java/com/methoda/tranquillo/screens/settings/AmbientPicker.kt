package com.methoda.tranquillo.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

data class AmbientOption(val id: String, val label: String, val hint: String)

/**
 * Available ambient sounds. The id is the filename in `res/raw/<id>.mp3` —
 * once those files land, the audio engine just loops the matching resource.
 * "none" means no audio.
 */
val AmbientOptions: List<AmbientOption> = listOf(
    AmbientOption("none",   "None",          "silence"),
    AmbientOption("waves",  "Ocean waves",   "long swell, foam, distance"),
    AmbientOption("birds",  "Birds chirping", "morning trees"),
    AmbientOption("bowls",  "Singing bowls", "deep, slow tones"),
    AmbientOption("music",  "Calm music",    "ambient, no lyrics")
)

@Composable
fun AmbientRow(
    label: String,
    hint: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                )
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
