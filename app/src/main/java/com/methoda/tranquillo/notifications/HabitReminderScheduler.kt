package com.methoda.tranquillo.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.methoda.tranquillo.PerfectlyTranquilloApp
import com.methoda.tranquillo.data.HabitEntity
import com.methoda.tranquillo.data.WeeklyHabitEntity
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Schedules + cancels habit reminder Workers.
 *
 * Daily reminders use a [PeriodicWorkRequest] (24h interval). Weekly reminders
 * use a [OneTimeWorkRequest] for the next occurrence; the worker re-enqueues
 * itself on fire.
 */
class HabitReminderScheduler(private val context: Context) {

    private val wm: WorkManager get() = WorkManager.getInstance(context)

    fun scheduleDaily(habitId: String, remindAt: String) {
        val (hour, minute) = parseHHMM(remindAt) ?: run {
            cancel(habitId); return
        }
        val initialDelayMs = computeNextOccurrenceMs(hour, minute, dayOfWeek = null)

        val req = PeriodicWorkRequestBuilder<HabitReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putString(HabitReminderWorker.KEY_HABIT_ID, habitId)
                    .putBoolean(HabitReminderWorker.KEY_IS_WEEKLY, false)
                    .build()
            )
            .addTag(TAG_PREFIX + habitId)
            .build()

        wm.enqueueUniquePeriodicWork(
            uniqueName(habitId),
            ExistingPeriodicWorkPolicy.UPDATE,
            req
        )
    }

    fun scheduleWeekly(habitId: String, remindAt: String, dayOfWeek: Int) {
        val (hour, minute) = parseHHMM(remindAt) ?: run {
            cancel(habitId); return
        }
        val initialDelayMs = computeNextOccurrenceMs(hour, minute, dayOfWeek)

        val req = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putString(HabitReminderWorker.KEY_HABIT_ID, habitId)
                    .putBoolean(HabitReminderWorker.KEY_IS_WEEKLY, true)
                    .build()
            )
            .addTag(TAG_PREFIX + habitId)
            .build()

        wm.enqueueUniqueWork(
            uniqueName(habitId),
            ExistingWorkPolicy.REPLACE,
            req
        )
    }

    fun cancel(habitId: String) {
        wm.cancelUniqueWork(uniqueName(habitId))
    }

    /**
     * Re-enqueue work for every habit with a reminder. Run as a one-shot from
     * the app (or after permission is granted) to recover after install /
     * permission flip.
     */
    suspend fun rescheduleAll() {
        val app = context.applicationContext as? PerfectlyTranquilloApp ?: return
        val daily = app.db.habitDao().allDaily().first()
        val weekly = app.db.weeklyHabitDao().allWeekly().first()
        for (h in daily) {
            val time = h.remindAt ?: continue
            scheduleDaily(h.id, time)
        }
        for (h in weekly) {
            val time = h.remindAt ?: continue
            scheduleWeekly(h.id, time, h.dayOfWeek)
        }
    }

    suspend fun reschedule(habit: HabitEntity) {
        val time = habit.remindAt
        if (time.isNullOrBlank()) cancel(habit.id) else scheduleDaily(habit.id, time)
    }

    suspend fun reschedule(habit: WeeklyHabitEntity) {
        val time = habit.remindAt
        if (time.isNullOrBlank()) cancel(habit.id) else scheduleWeekly(habit.id, time, habit.dayOfWeek)
    }

    companion object {
        private const val TAG_PREFIX = "habit-reminder-"
        private fun uniqueName(habitId: String) = "habit-reminder-$habitId"

        /** "HH:MM" → (hour, minute). Returns null on malformed input. */
        fun parseHHMM(s: String): Pair<Int, Int>? {
            val parts = s.split(":")
            if (parts.size != 2) return null
            val h = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            if (h !in 0..23 || m !in 0..59) return null
            return h to m
        }

        /**
         * Milliseconds from now until the next occurrence of (hour, minute) on
         * the given dayOfWeek (Calendar.SUNDAY = 1, etc.). If [dayOfWeek] is
         * null, the next occurrence is today (or tomorrow if past).
         */
        fun computeNextOccurrenceMs(
            hour: Int,
            minute: Int,
            dayOfWeek: Int?,
            now: Calendar = Calendar.getInstance()
        ): Long {
            val target = (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (dayOfWeek == null) {
                if (!target.after(now)) target.add(Calendar.DAY_OF_YEAR, 1)
            } else {
                // dayOfWeek here uses prototype convention 0..6 with Sunday=0.
                val cal = (calendarDayFor(dayOfWeek))
                target.set(Calendar.DAY_OF_WEEK, cal)
                while (!target.after(now)) target.add(Calendar.WEEK_OF_YEAR, 1)
            }
            return (target.timeInMillis - now.timeInMillis).coerceAtLeast(0)
        }

        private fun calendarDayFor(prototypeDay: Int): Int = when (prototypeDay) {
            0 -> Calendar.SUNDAY
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            else -> Calendar.SUNDAY
        }
    }
}
