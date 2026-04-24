package com.methoda.tranquillo.screens.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.ui.components.StoneKind
import com.methoda.tranquillo.ui.theme.Dimens
import com.methoda.tranquillo.ui.theme.Sand

private val DAYS_FULL = listOf(
    "Sunday", "Monday", "Tuesday", "Wednesday",
    "Thursday", "Friday", "Saturday"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val daily by viewModel.dailyHabits.collectAsState()
    val weekly by viewModel.weeklyHabits.collectAsState()

    var tabIndex by remember { mutableIntStateOf(0) }

    val dailyDone = daily.count { it.done }
    val weeklyDone = weekly.count { it.done }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "TODAY'S TENDING",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Small things, often",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        item {
            SecondaryTabRow(
                selectedTabIndex = tabIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabIndex),
                        color = Sand.copy(alpha = 0.7f),
                        height = 2.dp
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = {
                        TabLabelRow(label = "Daily", done = dailyDone, total = daily.size)
                    }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = {
                        TabLabelRow(label = "Weekly", done = weeklyDone, total = weekly.size)
                    }
                )
            }
        }

        if (tabIndex == 0) {
            dailyContent(
                daily = daily,
                dailyDone = dailyDone,
                onToggle = { viewModel.toggleDailyHabit(it) },
                onRemove = { viewModel.removeDailyHabit(it) },
                onSetReminder = { id, time -> viewModel.setReminder(id, isWeekly = false, time = time) },
                onAdd = { label -> viewModel.addDailyHabit(label) }
            )
        } else {
            weeklyContent(
                weekly = weekly,
                onToggle = { viewModel.toggleWeeklyHabit(it) },
                onRemove = { viewModel.removeWeeklyHabit(it) },
                onSetReminder = { id, time -> viewModel.setReminder(id, isWeekly = true, time = time) },
                onAdd = { label, day -> viewModel.addWeeklyHabit(label = label, day = day) }
            )
        }

        item { Spacer(Modifier.height(Dimens.ScreenBottomClearance)) }
    }
}

@Composable
private fun TabLabelRow(label: String, done: Int, total: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        ) {
            Text(
                text = "$done/$total",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.dailyContent(
    daily: List<com.methoda.tranquillo.data.HabitUi>,
    dailyDone: Int,
    onToggle: (String) -> Unit,
    onRemove: (String) -> Unit,
    onSetReminder: (String, String?) -> Unit,
    onAdd: (String) -> Unit
) {
    item {
        DailySummaryCard(done = dailyDone, total = daily.size)
    }
    items(items = daily, key = { "d-" + it.id }) { h ->
        HabitRow(
            label = h.label,
            hint = h.hint,
            streak = h.streak,
            remindAt = h.remindAt,
            done = h.done,
            stoneKind = StoneKind.Sand,
            onToggle = { onToggle(h.id) },
            onRemove = { onRemove(h.id) },
            onSetReminder = { t -> onSetReminder(h.id, t) }
        )
    }
    item { Spacer(Modifier.height(6.dp)) }
    item {
        AddHabitInline(
            placeholder = "add a daily habit…",
            onAdd = onAdd
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.weeklyContent(
    weekly: List<com.methoda.tranquillo.data.WeeklyHabitUi>,
    onToggle: (String) -> Unit,
    onRemove: (String) -> Unit,
    onSetReminder: (String, String?) -> Unit,
    onAdd: (String, Int) -> Unit
) {
    val todayIndex = AppViewModel.dayOfWeekIndex()
    val countsByDay = (0..6).map { i ->
        val forDay = weekly.filter { it.dayOfWeek == i }
        forDay.count { it.done } to forDay.size
    }

    item {
        WeeklyDayStripContainer(
            todayIndex = todayIndex,
            countsByDay = countsByDay,
            weekly = weekly,
            onToggle = onToggle,
            onRemove = onRemove,
            onSetReminder = onSetReminder,
            onAdd = onAdd
        )
    }
}

/**
 * Consolidated state container for the weekly view so the selectedDay can be
 * remembered inside a single item without losing state while tab-switching.
 */
@Composable
private fun WeeklyDayStripContainer(
    todayIndex: Int,
    countsByDay: List<Pair<Int, Int>>,
    weekly: List<com.methoda.tranquillo.data.WeeklyHabitUi>,
    onToggle: (String) -> Unit,
    onRemove: (String) -> Unit,
    onSetReminder: (String, String?) -> Unit,
    onAdd: (String, Int) -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(todayIndex) }
    val forDay = weekly.filter { it.dayOfWeek == selectedDay }
    val doneOnDay = forDay.count { it.done }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DayStrip(
            selectedDay = selectedDay,
            todayIndex = todayIndex,
            countsByDay = countsByDay,
            onDayClick = { selectedDay = it }
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = DAYS_FULL[selectedDay],
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (selectedDay == todayIndex) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Sand.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "TODAY",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = Sand,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (forDay.isEmpty())
                        "nothing scheduled · tap + to add"
                    else
                        "$doneOnDay of ${forDay.size} tended · coral stones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (forDay.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "a gentle day — nothing scheduled",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (h in forDay) {
                    HabitRow(
                        label = h.label,
                        hint = h.hint,
                        streak = h.streak,
                        remindAt = h.remindAt,
                        done = h.done,
                        stoneKind = StoneKind.Coral,
                        onToggle = { onToggle(h.id) },
                        onRemove = { onRemove(h.id) },
                        onSetReminder = { t -> onSetReminder(h.id, t) }
                    )
                }
            }
        }

        AddHabitInline(
            placeholder = "add a ${DAYS_FULL[selectedDay]} habit…",
            onAdd = { label -> onAdd(label, selectedDay) }
        )
    }
}

@Composable
private fun DailySummaryCard(done: Int, total: Int) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProgressRing(
                done = done,
                total = total,
                size = 52.dp,
                stroke = 3.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                val headline = when {
                    total == 0 -> "All quiet."
                    done == total -> "All tended."
                    else -> "${total - done} left to tend"
                }
                Text(
                    text = headline,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "every day · soft sand stones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProgressRing(
    done: Int,
    total: Int,
    size: androidx.compose.ui.unit.Dp,
    stroke: androidx.compose.ui.unit.Dp
) {
    val fill = if (total > 0) (done.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val s = Stroke(width = stroke.toPx())
            val inset = s.width / 2f
            drawArc(
                color = Color.White.copy(alpha = 0.12f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = s,
                topLeft = Offset(inset, inset),
                size = Size(this.size.width - s.width, this.size.height - s.width)
            )
            if (fill > 0f) {
                drawArc(
                    color = Sand,
                    startAngle = -90f,
                    sweepAngle = 360f * fill,
                    useCenter = false,
                    style = s,
                    topLeft = Offset(inset, inset),
                    size = Size(this.size.width - s.width, this.size.height - s.width)
                )
            }
        }
        Text(
            text = "$done",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
