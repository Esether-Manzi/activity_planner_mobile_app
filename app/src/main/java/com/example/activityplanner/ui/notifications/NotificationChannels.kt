package com.example.activityplanner.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Deadline Channel
            val deadlineChannel = NotificationChannel(
                DeadlineReminderReceiver.CHANNEL_ID,
                DeadlineReminderReceiver.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming task deadlines"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }

            // Priority Channel
            val priorityChannel = NotificationChannel(
                PriorityChangeReceiver.CHANNEL_ID,
                PriorityChangeReceiver.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for task priority changes"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(deadlineChannel)
            notificationManager.createNotificationChannel(priorityChannel)
        }
    }
}