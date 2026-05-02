package com.methoda.tranquillo.screens.garden

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.methoda.tranquillo.data.EveningNoteEntity

/**
 * "Nightly insights" — past "Tonight I noticed" entries, newest first.
 * Mirrors SerEmotFranCard. Collapsed by default; tap to expand.
 */
@Composable
fun NightlyInsightsCard(
    entries: List<EveningNoteEntity>,
    todayIso: String,
    modifier: Modifier = Modifier
) {
    CollapsibleNotesCard(
        eyebrow = "NIGHTLY INSIGHTS",
        subtitle = "what you noticed at the end of each day",
        emptyHint = "your evening one-liners gather here",
        entries = entries.map { DatedNote(date = it.date, text = it.text) },
        todayIso = todayIso,
        expandedKey = "nightly-insights",
        modifier = modifier
    )
}
