package com.methoda.tranquillo.screens.garden

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.methoda.tranquillo.data.GoodThingEntity

/**
 * "Ser emot från" — past "Looking forward to" entries, newest first.
 * Lives in the Garden, separate from the Spiritual mandala category.
 * Collapsed by default; tap to expand.
 */
@Composable
fun SerEmotFranCard(
    entries: List<GoodThingEntity>,
    todayIso: String,
    modifier: Modifier = Modifier
) {
    CollapsibleNotesCard(
        eyebrow = "SER EMOT FRÅN",
        subtitle = "what you've been looking forward to",
        emptyHint = "your daily one-liners gather here",
        entries = entries.map { DatedNote(date = it.date, text = it.text) },
        todayIso = todayIso,
        expandedKey = "ser-emot-fran",
        modifier = modifier
    )
}
