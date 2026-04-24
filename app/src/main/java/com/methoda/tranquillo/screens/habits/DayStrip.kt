package com.methoda.tranquillo.screens.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.ui.theme.Sand

private val DAYS_SHORT = listOf("S", "M", "T", "W", "T", "F", "S")

/**
 * 7-button day-of-week strip. Each button shows a tiny progress ring with
 * `done/total` weekly habits for that day. Today is marked with an amber dot.
 */
@Composable
fun DayStrip(
    selectedDay: Int,
    todayIndex: Int,
    countsByDay: List<Pair<Int, Int>>, // (done, total) for days 0..6
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 0..6) {
            val (done, total) = countsByDay.getOrElse(i) { 0 to 0 }
            val selected = i == selectedDay
            val isToday = i == todayIndex

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (selected) Sand.copy(alpha = 0.22f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                border = BorderStroke(
                    1.dp,
                    if (selected) Sand.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                ),
                onClick = { onDayClick(i) },
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = DAYS_SHORT[i],
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) Sand else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ProgressRingTiny(
                        done = done,
                        total = total
                    )
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                        ) {
                            Canvas(modifier = Modifier.size(4.dp)) {
                                drawCircle(color = Sand)
                            }
                        }
                    } else {
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressRingTiny(
    done: Int,
    total: Int
) {
    val fill = if (total > 0) (done.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    val ringColor = Sand
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    Box(
        modifier = Modifier.size(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val stroke = Stroke(width = 3f)
            val inset = stroke.width / 2f
            // track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke.width, size.height - stroke.width)
            )
            if (fill > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * fill,
                    useCenter = false,
                    style = stroke,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - stroke.width, size.height - stroke.width)
                )
            }
        }
        Text(
            text = if (total > 0) "$total" else "",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = androidx.compose.ui.unit.TextUnit.Unspecified
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
