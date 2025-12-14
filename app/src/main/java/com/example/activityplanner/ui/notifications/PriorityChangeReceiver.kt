package com.example.activityplanner.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.activityplanner.R
import com.example.activityplanner.db.TaskDatabaseHelper
import com.example.activityplanner.logic.AutoPriorityManager

class PriorityChangeReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "priority_channel"
        const val CHANNEL_NAME = "Priority Updates"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("task_id", 0)

        createNotificationChannel(context)
        checkAndNotifyPriorityChange(context, taskId)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for task priority changes"
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkAndNotifyPriorityChange(context: Context, taskId: Int) {
        val db = TaskDatabaseHelper(context)
        val task = db.getTaskById(taskId) ?: return

        // Calculate days remaining
        val daysRemaining = ((task.deadlineDateTime - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()

        // Determine new priority based on remaining days
        val newPriority = when {
            daysRemaining <= 1 -> "High"
            daysRemaining <= 3 -> "Medium"
            else -> "Low"
        }

        // Check if priority changed
        if (newPriority != task.priority) {
            // Update task with new priority
            val updatedTask = task.copy(priority = newPriority)
            db.updateTask(updatedTask)

            // Show notification
            showPriorityChangeNotification(context, taskId, task.title, task.priority, newPriority, daysRemaining)
        }
    }

    private fun showPriorityChangeNotification(
        context: Context,
        taskId: Int,
        taskTitle: String,
        oldPriority: String,
        newPriority: String,
        daysRemaining: Int
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📊 Priority Updated")
            .setContentText("$taskTitle: $oldPriority → $newPriority")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Task '$taskTitle' priority has been updated from $oldPriority to $newPriority.\nDays remaining: $daysRemaining")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(taskId + 1000, notification) // Different ID than deadline notifications
        }
    }
}