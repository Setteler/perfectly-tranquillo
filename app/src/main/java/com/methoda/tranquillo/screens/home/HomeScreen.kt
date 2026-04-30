package com.methoda.tranquillo.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.data.ResourceKey
import com.methoda.tranquillo.nav.Route
import com.methoda.tranquillo.ui.components.SatirMandala
import com.methoda.tranquillo.ui.theme.Coral
import com.methoda.tranquillo.ui.theme.Dimens
import com.methoda.tranquillo.ui.theme.Sand
import com.methoda.tranquillo.ui.theme.Sky
import java.util.Calendar

private data class QuickAction(
    val routePath: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val accent: androidx.compose.ui.graphics.Color
)

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val today by viewModel.today.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val daily by viewModel.dailyHabits.collectAsState()
    var showIntentionDialog by remember { mutableStateOf(false) }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour in 5..11 -> "Bloom gently, $userName."
        hour in 12..17 -> "Soften, $userName."
        else -> "Drift on, $userName."
    }

    val petalsFilled = ResourceKey.orderedClockwise.sumOf { k ->
        val f = today.resources[k]
        var c = 0
        if (f != null && f.am > 0f) c += 1
        if (f != null && f.pm > 0f) c += 1
        c
    }

    // Quick actions share a single accent so the grid feels calm. "Take a break"
    // is now its own bottom-nav tab (Pause), so it's removed from this list.
    val accent = MaterialTheme.colorScheme.primary
    val actions = listOf(
        QuickAction(Route.Morning.path, Icons.Outlined.WbSunny,    "Morning", "a gentle start",      accent),
        QuickAction(Route.Evening.path, Icons.Outlined.NightsStay, "Evening", "fill the outer ring", accent),
        QuickAction(Route.Breath.path,  Icons.Outlined.Air,        "Breathe", "4 · 4 · 4 · 4",       accent),
        QuickAction(Route.Focus.path,   Icons.Outlined.Coffee,     "Focus",   "25 min",              accent)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = Dimens.ScreenHorizontalPadding
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${AppViewModel.dayOfWeekShort().uppercase()} · DAY ${AppViewModel.dayOfYear()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        item {
            IntentionPill(
                intent = today.intent,
                onClick = { showIntentionDialog = true }
            )
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SatirMandala(
                    resources = today.resources,
                    size = 240.dp,
                    animate = true
                )
            }
        }

        item {
            SnapshotRow(
                petalsFilled = petalsFilled,
                petalsTotal = 16,
                habitsDone = daily.count { it.done },
                habitsTotal = daily.size,
                streakDays = daily.maxOfOrNull { it.streak } ?: 0
            )
        }

        item {
            GoodThingCard(
                value = today.goodThing,
                onCommit = { viewModel.setGoodThing(it) }
            )
        }

        // Quick-action grid: 2 columns, rendered as Rows of 2.
        val rows = actions.chunked(2)
        items(rows) { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowActions.forEach { action ->
                    QuickActionCard(
                        icon = action.icon,
                        title = action.title,
                        subtitle = action.subtitle,
                        accent = action.accent,
                        onClick = { onActionClick(action.routePath) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowActions.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        item { Spacer(Modifier.height(Dimens.ScreenBottomClearance)) }
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
