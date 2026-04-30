package com.methoda.tranquillo.screens.actions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.data.Phase
import com.methoda.tranquillo.data.ResourceKey
import com.methoda.tranquillo.data.StoneKind
import com.methoda.tranquillo.screens.home.IntentionEditDialog
import java.util.Calendar

/**
 * Morning — a soft greeting + today's inspirational quote (revealed by
 * scratching the foil cover) + an optional intention.
 *
 * Completing the morning awards a Moon stone and bumps Intellectual.am.
 */
@Composable
fun MorningScreen(
    viewModel: AppViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today by viewModel.today.collectAsState()
    val name by viewModel.userName.collectAsState()
    val doneAlready = today.morningDone
    var revealed by remember { mutableStateOf(doneAlready) }
    var showIntentionDialog by remember { mutableStateOf(false) }

    val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val quote = remember(dayOfYear) { MorningQuotes.forDayOfYear(dayOfYear) }
    val greeting = "Good morning, $name."

    ActionScaffold(
        eyebrow = "your morning",
        title = "Begin gently",
        onClose = onClose,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Text(
                text = "today's gentle reminder",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            // Scratch card sits in a soft glass surface.
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
            ) {
                ScratchCard(
                    onRevealed = { revealed = true },
                    foilColor = MaterialTheme.colorScheme.primary
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "“$quote”",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(100.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                onClick = { showIntentionDialog = true }
            ) {
                Row(
                    label = if (today.intent.isBlank())
                                "set today's intention (optional)"
                            else "today: ${today.intent}"
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = if (doneAlready) "morning already begun"
                       else if (revealed) "you'll collect a moon stone for this morning"
                       else "scratch the card above to reveal today's quote",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            PrimaryActionButton(
                text = if (doneAlready) "Close" else "Begin the day",
                onClick = {
                    if (!doneAlready) {
                        viewModel.setMorningDone(true)
                        viewModel.addStone(StoneKind.Moon, source = "morning")
                        viewModel.addResourceFill(ResourceKey.Intellectual, Phase.Am, 0.4f)
                    }
                    onClose()
                },
                variant = if (revealed || doneAlready) PrimaryActionVariant.Sand
                          else PrimaryActionVariant.Ghost,
                trailing = null
            )
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showIntentionDialog) {
        IntentionEditDialog(
            initial = today.intent,
            onSave = { text ->
                viewModel.setIntent(text.trim())
                showIntentionDialog = false
            },
            onClear = {
                viewModel.setIntent("")
                showIntentionDialog = false
            },
            onDismiss = { showIntentionDialog = false }
        )
    }
}

@Composable
private fun Row(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 18.dp)
    )
}
