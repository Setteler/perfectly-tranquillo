package com.methoda.tranquillo.screens.actions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.data.Phase
import com.methoda.tranquillo.data.ResourceKey
import com.methoda.tranquillo.data.StoneKind
import com.methoda.tranquillo.ui.theme.Sand
import com.methoda.tranquillo.ui.theme.Sky

/**
 * Evening reflection — a single screen with:
 *   • a soft nudge toward the Mandala tab ("tend your evening petals"),
 *   • an optional free-form note field.
 * On save: eveningDone=true, a Sand stone is awarded, spiritual.pm bumps +0.4.
 */
@Composable
fun EveningScreen(
    viewModel: AppViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today by viewModel.today.collectAsState()
    var note by remember { mutableStateOf("") }
    val doneAlready = today.eveningDone

    ActionScaffold(
        eyebrow = "your evening",
        title = "Tend the day",
        onClose = onClose,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "A slower moment before you put the day down.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            // Nudge card — Mandala reminder
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Sky.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, Sky.copy(alpha = 0.30f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NightsStay,
                            contentDescription = null,
                            tint = Sky
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Tend your evening petals",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Fill the PM ring of any petal that carried you today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "One line about today?",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "A small noticing, a kindness, a tired truth. Optional.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                BasicTextField(
                    value = note,
                    onValueChange = { note = it },
                    textStyle = MaterialTheme.typography.displaySmall.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                        .wrapContentHeight(),
                    decorationBox = { inner ->
                        if (note.isEmpty()) {
                            Text(
                                text = "tonight I noticed…",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                            )
                        }
                        inner()
                    }
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = if (doneAlready) "evening already tended" else "you'll collect a sand stone for this evening",
                style = MaterialTheme.typography.bodyMedium,
                color = Sand,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            PrimaryActionButton(
                text = if (doneAlready) "Close" else "Set the evening",
                onClick = {
                    if (!doneAlready) {
                        viewModel.setEveningDone(true)
                        viewModel.addStone(StoneKind.Sand, source = "evening")
                        viewModel.addResourceFill(ResourceKey.Spiritual, Phase.Pm, 0.4f)
                        // Persist the optional one-liner as a Spirit · PM mandala
                        // entry so it surfaces in the Garden archive later.
                        if (note.isNotBlank()) {
                            viewModel.saveMandalaEntry(
                                key = ResourceKey.Spiritual,
                                phase = Phase.Pm,
                                resource = note.trim(),
                                challenge = ""
                            )
                        }
                    }
                    onClose()
                }
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}
