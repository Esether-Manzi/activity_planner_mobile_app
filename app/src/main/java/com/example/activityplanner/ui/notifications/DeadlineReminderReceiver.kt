package com.example.activityplanner.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.activityplanner.R
import com.example.activityplanner.db.TaskDatabaseHelper
import com.example.activityplanner.ui.tasks.TaskListActivity
import java.text.SimpleDateFormat
import java.util.*

class DeadlineReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "deadline_channel"
        const val CHANNEL_NAME = "Deadline Reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("task_id", 0)
        val taskTitle = intent.getStringExtra("task_title") ?: "Task"
        val deadlineTime = intent.getLongExtra("task_deadline", 0L)

        createNotificationChannel(context)
        showDeadlineNotification(context, taskId, taskTitle, deadlineTime)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming task deadlines"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showDeadlineNotification(
        context: Context,
        taskId: Int,
        taskTitle: String,
        deadlineTime: Long
    ) {
        // Create intent for when notification is tapped
        val contentIntent = Intent(context, TaskListActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Format deadline time
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val deadlineFormatted = dateFormat.format(Date(deadlineTime))

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Make sure this icon exists
            .setContentTitle("⏰ Deadline Approaching!")
            .setContentText("$taskTitle is due at $deadlineFormatted")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Task '$taskTitle' is approaching its deadline at $deadlineFormatted.\nComplete it soon!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(taskId, notification)
        }
    }
}