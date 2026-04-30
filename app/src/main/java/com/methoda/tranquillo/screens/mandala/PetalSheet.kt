package com.methoda.tranquillo.screens.mandala

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.EntryPair
import com.methoda.tranquillo.data.Phase
import com.methoda.tranquillo.data.ResourceKey
import com.methoda.tranquillo.ui.components.Stone
import com.methoda.tranquillo.ui.components.StoneKind
import com.methoda.tranquillo.ui.theme.Coral
import com.methoda.tranquillo.ui.theme.Sand
import com.methoda.tranquillo.ui.theme.Sky

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetalSheet(
    sheetState: SheetState,
    resourceKey: ResourceKey,
    phase: Phase,
    entry: EntryPair,
    onDismiss: () -> Unit,
    onSave: (resource: String, challenge: String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        var resText by remember(resourceKey, phase) { mutableStateOf(entry.resource) }
        var chalText by remember(resourceKey, phase) { mutableStateOf(entry.challenge) }
        val placeholder = PetalPlaceholders.forKey(resourceKey, phase)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Stone(
                    kind = if (phase == Phase.Am) StoneKind.Moon else StoneKind.Sand,
                    size = 24.dp
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (phase == Phase.Am) "THIS MORNING" else "THIS EVENING",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (phase == Phase.Am) Sky else Sand
                    )
                    Text(
                        text = resourceKey.label,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Text(
                text = PetalPlaceholders.hintFor(resourceKey),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(16.dp))

            FieldBlock(
                accent = if (phase == Phase.Am) Sky else Sand,
                icon = "✿",
                label = "RESOURCE",
                placeholder = placeholder.resource,
                value = resText,
                onChange = { resText = it }
            )

            Spacer(Modifier.height(12.dp))

            FieldBlock(
                accent = Coral,
                icon = "◌",
                label = "CHALLENGE",
                sublabel = "optional",
                placeholder = placeholder.challenge,
                value = chalText,
                onChange = { chalText = it }
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = { onSave(resText, chalText) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Sand,
                    contentColor = Color(0xFF4A3C20)
                )
            ) {
                Text(
                    text = "Save this petal  ✿",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FieldBlock(
    accent: Color,
    icon: String,
    label: String,
    sublabel: String? = null,
    placeholder: String,
    value: String,
    onChange: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
            if (sublabel != null) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "· $sublabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraSmall,
            color = accent.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.30f))
        ) {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(accent),
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    }
                    inner()
                }
            )
        }
    }
}
