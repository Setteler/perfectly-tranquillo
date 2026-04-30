package com.methoda.tranquillo

import android.app.Application
import com.methoda.tranquillo.data.AppDatabase
import com.methoda.tranquillo.data.PrefsStore
import com.methoda.tranquillo.notifications.HabitReminderScheduler
import com.methoda.tranquillo.notifications.NotificationChannelSetup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PerfectlyTranquilloApp : Application() {
    val db: AppDatabase by lazy { AppDatabase.get(this) }
    val prefs: PrefsStore by lazy { PrefsStore(this) }
    val reminderScheduler: HabitReminderScheduler by lazy { HabitReminderScheduler(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationChannelSetup.ensureChannels(this)

        // Re-enqueue any habit reminders so install / permission-grant / WM
        // process death recovers the schedule.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            reminderScheduler.rescheduleAll()
        }
    }
}
