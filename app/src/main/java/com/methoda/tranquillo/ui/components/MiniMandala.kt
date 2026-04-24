package com.methoda.tranquillo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.AmPmFill
import com.methoda.tranquillo.data.ResourceKey

/**
 * Tiny mandala for row affordances (week strip in #5, list items, etc).
 * Same drawing routine; labels off, no breathe animation.
 */
@Composable
fun MiniMandala(
    resources: Map<ResourceKey, AmPmFill>,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    SatirMandala(
        resources = resources,
        modifier = modifier,
        size = size,
        showLabels = false,
        animate = false
    )
}
