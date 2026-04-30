package com.methoda.tranquillo.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat

object NotificationChannelSetup {
    const val CHANNEL_HABIT_REMINDERS = "habit_reminders"
    private const val CHANNEL_NAME = "Habit reminders"
    private const val CHANNEL_DESCRIPTION = "Soft nudges for the habits you've set a time for."

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_HABIT_REMINDERS) != null) return

        val channel = NotificationChannel(
            CHANNEL_HABIT_REMINDERS,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }
}
