package com.methoda.tranquillo.screens.mandala

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.data.EntryPair
import com.methoda.tranquillo.data.Phase
import com.methoda.tranquillo.data.ResourceKey
import com.methoda.tranquillo.ui.components.SatirMandala
import com.methoda.tranquillo.ui.theme.Dimens
import com.methoda.tranquillo.ui.theme.Sand
import com.methoda.tranquillo.ui.theme.Sky
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MandalaScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val today by viewModel.today.collectAsState()
    val phase = today.currentPhase

    var activeKey by remember { mutableStateOf<ResourceKey?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val ordered = ResourceKey.orderedClockwise

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "YOUR INNER LANDSCAPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Your mandala today",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        item {
            PhaseToggle(
                current = phase,
                onChange = { viewModel.setPhase(it) }
            )
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SatirMandala(
                    resources = today.resources,
                    size = 280.dp,
                    highlight = activeKey,
                    onPetalTap = { key -> activeKey = key },
                    animate = true
                )
            }
        }

        items(ordered) { key ->
            PetalRow(
                key = key,
                phase = phase,
                fill = today.resources[key],
                entry = today.entries[key to phase],
                onClick = { activeKey = key }
            )
        }

        item { Spacer(Modifier.height(Dimens.ScreenBottomClearance)) }
    }

    val current = activeKey
    if (current != null) {
        val entry = today.entries[current to phase] ?: EntryPair()
        PetalSheet(
            sheetState = sheetState,
            resourceKey = current,
            phase = phase,
            entry = entry,
            onDismiss = { activeKey = null },
            onSave = { res, chal ->
                viewModel.saveMandalaEntry(current, phase, res, chal)
                scope.launch {
                    sheetState.hide()
                    activeKey = null
                }
            }
        )
    }
}

@Composable
private fun PhaseToggle(
    current: Phase,
    onChange: (Phase) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                TogglePill(
                    label = "Morning",
                    accent = Sky,
                    selected = current == Phase.Am,
                    onClick = { onChange(Phase.Am) }
                )
                Spacer(Modifier.width(4.dp))
                TogglePill(
                    label = "Evening",
                    accent = Sand,
                    selected = current == Phase.Pm,
                    onClick = { onChange(Phase.Pm) }
                )
            }
        }
    }
}

@Composable
private fun TogglePill(
    label: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = if (selected) accent.copy(alpha = 0.18f) else Color.Transparent,
        onClick = onClick
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )
    }
}
