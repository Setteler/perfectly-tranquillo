package com.methoda.tranquillo.data

import com.methoda.tranquillo.notifications.HabitReminderScheduler
import com.methoda.tranquillo.screens.habits.HabitMapping
import com.methoda.tranquillo.ui.components.StoneKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * UI-facing habit record — "done today" derived from `lastDoneDate == today`.
 */
data class HabitUi(
    val id: String,
    val label: String,
    val hint: String,
    val streak: Int,
    val remindAt: String?,
    val done: Boolean
)

data class WeeklyHabitUi(
    val id: String,
    val label: String,
    val hint: String,
    val dayOfWeek: Int,
    val streak: Int,
    val remindAt: String?,
    val done: Boolean
)

class HabitsRepository(
    private val habitDao: HabitDao,
    private val weeklyHabitDao: WeeklyHabitDao,
    private val habitFillDao: HabitFillDao,
    private val stonesRepo: StonesRepository? = null,
    private val reminderScheduler: HabitReminderScheduler? = null
) {

    fun dailyHabitsUi(todayProvider: () -> String = { isoEffectiveToday() }): Flow<List<HabitUi>> =
        habitDao.allDaily().map { list ->
            val today = todayProvider()
            list.map { h ->
                HabitUi(
                    id = h.id,
                    label = h.label,
                    hint = h.hint,
                    streak = h.streak,
                    remindAt = h.remindAt,
                    done = h.lastDoneDate == today
                )
            }
        }

    fun weeklyHabitsUi(todayProvider: () -> String = { isoEffectiveToday() }): Flow<List<WeeklyHabitUi>> =
        weeklyHabitDao.allWeekly().map { list ->
            val today = todayProvider()
            list.map { h ->
                WeeklyHabitUi(
                    id = h.id,
                    label = h.label,
                    hint = h.hint,
                    dayOfWeek = h.dayOfWeek,
                    streak = h.streak,
                    remindAt = h.remindAt,
                    done = h.lastDoneDate == today
                )
            }
        }

    /** Total habit-fill contribution per mapped resource for the given date. */
    fun fillTotalsForDate(date: String): Flow<Map<ResourceKey, Float>> =
        habitFillDao.fillsForDate(date).map { rows ->
            val out = mutableMapOf<ResourceKey, Float>()
            for (r in rows) {
                val key = runCatching { ResourceKey.valueOf(r.mappedResource) }.getOrNull() ?: continue
                out[key] = (out[key] ?: 0f) + r.fillContribution
            }
            out
        }

    // ---- actions ---------------------------------------------------------

    suspend fun toggleDaily(id: String, today: String = isoEffectiveToday()) {
        val h = habitDao.findById(id) ?: return
        if (h.lastDoneDate == today) {
            // Untoggle — undo today's completion. Stone stays (committed).
            habitDao.setLastDone(id, null)
            val newStreak = (h.streak - 1).coerceAtLeast(0)
            habitDao.setStreak(id, newStreak)
            habitFillDao.deleteForHabitOnDate(today, id)
        } else {
            val yesterday = isoYesterdayOf(today)
            val newStreak = if (h.lastDoneDate == yesterday) h.streak + 1 else 1
            habitDao.setLastDone(id, today)
            habitDao.setStreak(id, newStreak)
            val mapped = HabitMapping.resourceFor(id)
            if (mapped != null) {
                habitFillDao.deleteForHabitOnDate(today, id)
                habitFillDao.insert(
                    HabitFillEntity(
                        date = today,
                        habitId = id,
                        mappedResource = mapped.name,
                        fillContribution = HabitMapping.FILL_CONTRIBUTION
                    )
                )
            }
            stonesRepo?.addStone(StoneKind.Jade, source = "habit")
        }
    }

    suspend fun toggleWeekly(id: String, today: String = isoEffectiveToday()) {
        val h = weeklyHabitDao.findById(id) ?: return
        if (h.lastDoneDate == today) {
            weeklyHabitDao.setLastDone(id, null)
            val newStreak = (h.streak - 1).coerceAtLeast(0)
            weeklyHabitDao.setStreak(id, newStreak)
            habitFillDao.deleteForHabitOnDate(today, id)
        } else {
            // Streak increments on each completion; no "missed-week" rule yet.
            val newStreak = h.streak + 1
            weeklyHabitDao.setLastDone(id, today)
            weeklyHabitDao.setStreak(id, newStreak)
            val mapped = HabitMapping.resourceFor(id)
            if (mapped != null) {
                habitFillDao.deleteForHabitOnDate(today, id)
                habitFillDao.insert(
                    HabitFillEntity(
                        date = today,
                        habitId = id,
                        mappedResource = mapped.name,
                        fillContribution = HabitMapping.FILL_CONTRIBUTION
                    )
                )
            }
            stonesRepo?.addStone(StoneKind.Jade, source = "habit")
        }
    }

    suspend fun addDailyHabit(label: String, hint: String) {
        val clean = label.trim()
        if (clean.isEmpty()) return
        val id = "d-" + System.currentTimeMillis()
        habitDao.upsert(
            HabitEntity(
                id = id,
                label = clean,
                hint = hint.trim().ifEmpty { "every day" },
                streak = 0,
                remindAt = null,
                lastDoneDate = null,
                position = Int.MAX_VALUE
            )
        )
    }

    suspend fun addWeeklyHabit(label: String, hint: String, day: Int) {
        val clean = label.trim()
        if (clean.isEmpty()) return
        val id = "w-" + System.currentTimeMillis()
        weeklyHabitDao.upsert(
            WeeklyHabitEntity(
                id = id,
                label = clean,
                hint = hint.trim().ifEmpty { dayName(day) + "s" },
                dayOfWeek = day,
                streak = 0,
                remindAt = null,
                lastDoneDate = null,
                position = Int.MAX_VALUE
            )
        )
    }

    suspend fun removeDaily(id: String) {
        habitDao.delete(id)
        habitFillDao.deleteForHabitOnDate(isoEffectiveToday(), id)
        reminderScheduler?.cancel(id)
    }

    suspend fun removeWeekly(id: String) {
        weeklyHabitDao.delete(id)
        habitFillDao.deleteForHabitOnDate(isoEffectiveToday(), id)
        reminderScheduler?.cancel(id)
    }

    suspend fun setDailyReminder(id: String, time: String?) {
        val cleaned = time?.ifBlank { null }
        habitDao.setRemindAt(id, cleaned)
        if (cleaned == null) reminderScheduler?.cancel(id)
        else reminderScheduler?.scheduleDaily(id, cleaned)
    }

    suspend fun setWeeklyReminder(id: String, time: String?) {
        val cleaned = time?.ifBlank { null }
        weeklyHabitDao.setRemindAt(id, cleaned)
        val h = weeklyHabitDao.findById(id) ?: return
        if (cleaned == null) reminderScheduler?.cancel(id)
        else reminderScheduler?.scheduleWeekly(id, cleaned, h.dayOfWeek)
    }

    companion object {
        private val ISO = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }

        /** Day boundary at 05:00 — must match [AppViewModel.DAY_BOUNDARY_HOUR]. */
        private const val DAY_BOUNDARY_HOUR = 5

        fun isoToday(): String = ISO.format(Date())

        /** ISO date treating 00:00..04:59 as part of the previous day. */
        fun isoEffectiveToday(): String {
            val cal = Calendar.getInstance()
            if (cal.get(Calendar.HOUR_OF_DAY) < DAY_BOUNDARY_HOUR) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
            return ISO.format(cal.time)
        }

        fun isoYesterdayOf(today: String): String {
            val cal = Calendar.getInstance()
            runCatching { cal.time = ISO.parse(today) ?: return@runCatching }
            cal.add(Calendar.DAY_OF_MONTH, -1)
            return ISO.format(cal.time)
        }

        fun dayName(day: Int): String = when (day) {
            0 -> "Sunday"
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            else -> "Someday"
        }
    }
}
