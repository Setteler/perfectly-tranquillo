package com.methoda.tranquillo.ui.placeholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.AmPmFill
import com.methoda.tranquillo.data.ResourceKey
import com.methoda.tranquillo.ui.components.SatirMandala
import com.methoda.tranquillo.ui.components.Stone
import com.methoda.tranquillo.ui.components.StoneKind
import com.methoda.tranquillo.ui.theme.Dimens

@Composable
fun PlaceholderScreen(
    title: String,
    eyebrow: String,
    subprojectNumber: Int,
    modifier: Modifier = Modifier
) {
    val sampleFills = remember_sampleFills()
    val stoneKinds = listOf(
        StoneKind.Moon, StoneKind.Jade, StoneKind.Shell, StoneKind.Coral, StoneKind.Sand
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = Dimens.ScreenHorizontalPadding,
                vertical = Dimens.ScreenVerticalPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = eyebrow.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "coming soon · sub-project #$subprojectNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )

        // --- Proof-of-components preview (removed once real screens ship in #2) ---
        SatirMandala(
            resources = sampleFills,
            size = 140.dp,
            modifier = Modifier.padding(top = 28.dp)
        )

        Row(
            modifier = Modifier.padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for ((idx, k) in stoneKinds.withIndex()) {
                Stone(kind = k, size = 28.dp, seed = idx + 1)
            }
        }

        Text(
            text = "components preview · removed in #2",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun remember_sampleFills(): Map<ResourceKey, AmPmFill> =
    androidx.compose.runtime.remember {
        mapOf(
            ResourceKey.Physical to AmPmFill(am = 0.7f, pm = 0f),
            ResourceKey.Intellectual to AmPmFill(am = 0.5f, pm = 0.5f),
            ResourceKey.Emotional to AmPmFill(am = 0f, pm = 0.8f)
        )
    }
