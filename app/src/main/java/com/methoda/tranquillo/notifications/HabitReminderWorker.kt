package com.methoda.tranquillo.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.methoda.tranquillo.MainActivity
import com.methoda.tranquillo.PerfectlyTranquilloApp
import com.methoda.tranquillo.R
import com.methoda.tranquillo.data.HabitsRepository
import com.methoda.tranquillo.data.PrefsStore

/**
 * Posts a habit-reminder notification when fired by WorkManager. Skips the
 * post if the habit was already completed today, and re-enqueues itself for
 * weekly habits so the next occurrence is scheduled.
 */
class HabitReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getString(KEY_HABIT_ID) ?: return Result.success()
        val isWeekly = inputData.getBoolean(KEY_IS_WEEKLY, false)
        val app = applicationContext as? PerfectlyTranquilloApp ?: return Result.success()

        val today = HabitsRepository.isoToday()
        val (label, hint, doneToday, remindAt, dayOfWeek) = if (isWeekly) {
            val h = app.db.weeklyHabitDao().findById(habitId) ?: return Result.success()
            HabitInfo(h.label, h.hint, h.lastDoneDate == today, h.remindAt, h.dayOfWeek)
        } else {
            val h = app.db.habitDao().findById(habitId) ?: return Result.success()
            HabitInfo(h.label, h.hint, h.lastDoneDate == today, h.remindAt, null)
        }

        // Re-enqueue weekly worker for next occurrence, regardless of skip.
        if (isWeekly && remindAt != null && dayOfWeek != null) {
            HabitReminderScheduler(applicationContext)
                .scheduleWeekly(habitId, remindAt, dayOfWeek)
        }

        if (doneToday) return Result.success()
        if (remindAt.isNullOrBlank()) return Result.success()
        if (app.prefs.notifModeNow() == PrefsStore.NOTIF_SILENT) return Result.success()

        postNotification(applicationContext, habitId, label, hint)
        return Result.success()
    }

    companion object {
        const val KEY_HABIT_ID = "habit_id"
        const val KEY_IS_WEEKLY = "is_weekly"
        const val NOTIFICATION_TAG = "habit_reminder"

        fun postNotification(context: Context, habitId: String, label: String, hint: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) return
            }

            val tapIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_DEEP_LINK_ROUTE, "habits")
            }
            val tapPi = PendingIntent.getActivity(
                context,
                habitId.hashCode(),
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notif = NotificationCompat.Builder(
                context,
                NotificationChannelSetup.CHANNEL_HABIT_REMINDERS
            )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(label)
                .setContentText(hint.ifBlank { "a small thing — when you're ready" })
                .setStyle(NotificationCompat.BigTextStyle().bigText(
                    hint.ifBlank { "a small thing — when you're ready" }
                ))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(tapPi)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_TAG, habitId.hashCode(), notif)
        }
    }
}

private data class HabitInfo(
    val label: String,
    val hint: String,
    val doneToday: Boolean,
    val remindAt: String?,
    val dayOfWeek: Int?
)
