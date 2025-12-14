package com.example.activityplanner.ui.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.activityplanner.db.TaskDatabaseHelper
import com.example.activityplanner.model.Task
import java.util.*

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"

    fun scheduleTaskNotifications(context: Context, task: Task) {
        scheduleDeadlineNotification(context, task)
        schedulePriorityCheckNotification(context, task)
    }

    private fun scheduleDeadlineNotification(context: Context, task: Task) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Schedule notification 1 hour before deadline
            val notificationTime = task.deadlineDateTime - (60 * 60 * 1000) // 1 hour before

            // Don't schedule if already passed
            if (notificationTime <= System.currentTimeMillis()) {
                return
            }

            val intent = Intent(context, DeadlineReminderReceiver::class.java).apply {
                putExtra("task_id", task.id)
                putExtra("task_title", task.title)
                putExtra("task_deadline", task.deadlineDateTime)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id, // Unique request code using task id
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule the alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
                )
            }

            Log.d(TAG, "Scheduled deadline notification for task: ${task.title} at $notificationTime")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling deadline notification", e)
        }
    }

    private fun schedulePriorityCheckNotification(context: Context, task: Task) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Schedule priority check 24 hours before deadline
            val checkTime = task.deadlineDateTime - (24 * 60 * 60 * 1000)

            // Don't schedule if already passed
            if (checkTime <= System.currentTimeMillis()) {
                return
            }

            val intent = Intent(context, PriorityChangeReceiver::class.java).apply {
                putExtra("task_id", task.id)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id + 1000, // Different request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    checkTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    checkTime,
                    pendingIntent
                )
            }

            Log.d(TAG, "Scheduled priority check for task: ${task.title} at $checkTime")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling priority check", e)
        }
    }

    fun cancelTaskNotifications(context: Context, taskId: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Cancel deadline notification
            val deadlineIntent = Intent(context, DeadlineReminderReceiver::class.java)
            val deadlinePendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                deadlineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(deadlinePendingIntent)

            // Cancel priority check notification
            val priorityIntent = Intent(context, PriorityChangeReceiver::class.java)
            val priorityPendingIntent = PendingIntent.getBroadcast(
                context,
                taskId + 1000,
                priorityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(priorityPendingIntent)

            Log.d(TAG, "Cancelled notifications for task ID: $taskId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling notifications", e)
        }
    }

    fun rescheduleAllAlarms(context: Context) {
        try {
            val db = TaskDatabaseHelper(context)
            val tasks = db.getAllTasks()

            tasks.forEach { task ->
                scheduleTaskNotifications(context, task)
            }

            Log.d(TAG, "Rescheduled ${tasks.size} task notifications after boot")
        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling alarms", e)
        }
    }
}