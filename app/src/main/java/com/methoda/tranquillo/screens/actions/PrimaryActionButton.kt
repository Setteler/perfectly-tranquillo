package com.methoda.tranquillo.screens.actions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.methoda.tranquillo.ui.theme.Ink
import com.methoda.tranquillo.ui.theme.Sand

/**
 * Full-round Sand-bg primary button used across the action screens.
 * Reproduces the prototype's `PrimaryBtn` — 56 dp tall, Caveat-ish 18 sp,
 * dark ink text.
 */
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: PrimaryActionVariant = PrimaryActionVariant.Sand,
    trailing: String? = "◦"
) {
    when (variant) {
        PrimaryActionVariant.Sand -> Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Sand,
                contentColor = Ink,
                disabledContainerColor = Sand.copy(alpha = 0.5f),
                disabledContentColor = Ink.copy(alpha = 0.55f)
            )
        ) {
            ButtonContent(text, trailing)
        }
        PrimaryActionVariant.Ghost -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            ButtonContent(text, trailing)
        }
    }
}

@Composable
private fun ButtonContent(text: String, trailing: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 20.sp,
                color = Color.Unspecified
            )
        )
        if (!trailing.isNullOrBlank()) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = trailing,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 18.sp)
            )
        }
    }
}

enum class PrimaryActionVariant { Sand, Ghost }
