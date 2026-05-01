package com.methoda.tranquillo.screens.garden

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.ui.components.ShellColor
import com.methoda.tranquillo.ui.components.StoneKind
import com.methoda.tranquillo.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Garden / Progress screen — jar of seashells (one per ritual) + 7-day
 * resource averages.
 *
 * Stone kind → shell color mapping mirrors the prototype:
 *   Moon   → Pearl   (mornings + breath both award Moon)
 *   Sand   → Butter  (evenings)
 *   Jade   → Moss    (habits — when wired)
 *   Coral  → Coral   (breaks)
 *   Deep   → Sky     (focus)
 *   Shell  → Sand    (fallback / future use)
 */
@Composable
fun GardenScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val stones by viewModel.stones.collectAsState()
    val averages by viewModel.sevenDayAverages.collectAsState()
    val archive by viewModel.archiveEntries.collectAsState()
    val goodThings by viewModel.goodThingsArchive.collectAsState()

    val shells = stones.map { ShellInJar(color = it.toShellColor()) }
    val counts: Map<ShellColor, Int> =
        shells.groupingBy { it.color }.eachCount()
    val daysTending = stones.size
    val todayIso = remember { isoToday() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = Dimens.ScreenHorizontalPadding,
                vertical = Dimens.ScreenVerticalPadding
            )
    ) {
        GardenHeader(daysTending = daysTending)
        Spacer(Modifier.height(14.dp))
        ShellsCard(shells = shells, counts = counts)
        Spacer(Modifier.height(14.dp))
        ResourceAveragesCard(averages = averages)
        Spacer(Modifier.height(14.dp))
        SerEmotFranCard(entries = goodThings, todayIso = todayIso)
        Spacer(Modifier.height(14.dp))
        ArchiveCard(entries = archive, todayIso = todayIso)
        Spacer(Modifier.height(Dimens.ScreenBottomClearance))
    }
}

private fun isoToday(): String {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getDefault() }
    return df.format(Date())
}

@Composable
private fun GardenHeader(daysTending: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (daysTending == 0) "BEGIN GENTLY"
                   else "$daysTending ${if (daysTending == 1) "shell" else "shells"} kept",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Your quiet garden",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ShellsCard(
    shells: List<ShellInJar>,
    counts: Map<ShellColor, Int>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 18.dp)
        ) {
            Text(
                text = "SHELLS COLLECTED",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "one shell for each ritual you kept.",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 6.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                JarOfShells(shells = shells)
            }
            if (shells.isEmpty()) {
                Text(
                    text = "your shells will gather here as you keep your rituals",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Spacer(Modifier.height(8.dp))
            ShellLegendRow(counts = counts)
        }
    }
}

private fun StoneKind.toShellColor(): ShellColor = when (this) {
    StoneKind.Moon  -> ShellColor.Pearl
    StoneKind.Sand  -> ShellColor.Butter
    StoneKind.Jade  -> ShellColor.Moss
    StoneKind.Coral -> ShellColor.Coral
    StoneKind.Deep  -> ShellColor.Sky
    StoneKind.Shell -> ShellColor.Sand
}
