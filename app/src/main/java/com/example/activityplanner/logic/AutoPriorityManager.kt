package com.example.activityplanner.logic

import android.content.Context
import android.util.Log
import com.example.activityplanner.db.TaskDatabaseHelper
import com.example.activityplanner.model.Task
import com.example.activityplanner.ui.notifications.NotificationScheduler
import kotlin.math.max

/**
 * AutoPriorityManager recalculates task priorities based on deadline proximity
 * and can trigger UI updates or notifications.
 */
object AutoPriorityManager {

    private const val TAG = "AutoPriorityManager"

    /**
     * Updates priorities for a list of tasks and optionally triggers a callback.
     * @param context Context for database access
     * @param tasks List of tasks to evaluate
     * @param onComplete Callback with true if any priority changed
     */
    fun updatePriorities(context: Context, tasks: List<Task>, onComplete: (Boolean) -> Unit) {
        val db = TaskDatabaseHelper(context)
        var changed = false

        tasks.forEach { task ->
            val newPriority = calculatePriority(task)

            if (newPriority != task.priority) {
                Log.d(TAG, "Priority changed for '${task.title}': ${task.priority} -> $newPriority")

                val updatedTask = task.copy(priority = newPriority)

                // Update in database
                val updateResult = db.updateTask(updatedTask)

                if (updateResult > 0) {
                    changed = true

                    // Reschedule notifications for this task (since deadline or priority changed)
                    NotificationScheduler.cancelTaskNotifications(context, task.id)
                    NotificationScheduler.scheduleTaskNotifications(context, updatedTask)

                    // Show notification for priority change (optional)
                    showPriorityChangeNotification(context, task, newPriority)
                }
            }
        }

        onComplete(changed)
    }

    /**
     * Calculates priority based on days to deadline.
     * @param task Task to calculate priority for
     * @return New priority ("High", "Medium", or "Low")
     */
    fun calculatePriority(task: Task): String {
        val now = System.currentTimeMillis()

        // Ensure deadline is in the future for calculation
        if (task.deadlineDateTime <= now) {
            return "High" // If already past deadline, mark as High priority
        }

        // Calculate days remaining
        val millisecondsPerDay = 24 * 60 * 60 * 1000L
        val daysRemaining = (task.deadlineDateTime - now) / millisecondsPerDay

        return when {
            daysRemaining <= 2 -> "High"        // 2 days or less
            daysRemaining <= 5 -> "Medium"      // 3-5 days
            else -> "Low"                       // 6+ days
        }
    }

    /**
     * Recalculates priority for a single task and updates if changed.
     * @param context Context for database access
     * @param task Task to update
     * @return True if priority was changed
     */
    fun updateSingleTaskPriority(context: Context, task: Task): Boolean {
        val newPriority = calculatePriority(task)

        if (newPriority != task.priority) {
            val db = TaskDatabaseHelper(context)
            val updatedTask = task.copy(priority = newPriority)

            val updateResult = db.updateTask(updatedTask)

            if (updateResult > 0) {
                // Reschedule notifications
                NotificationScheduler.cancelTaskNotifications(context, task.id)
                NotificationScheduler.scheduleTaskNotifications(context, updatedTask)

                // Show notification
                showPriorityChangeNotification(context, task, newPriority)
                return true
            }
        }

        return false
    }

    /**
     * Shows a notification when priority changes (optional).
     */
    private fun showPriorityChangeNotification(
        context: Context,
        originalTask: Task,
        newPriority: String
    ) {
        // You can create a simple notification here or leave this empty
        // if you're already showing notifications through PriorityChangeReceiver

        Log.d(TAG, "Priority changed for '${originalTask.title}': ${originalTask.priority} -> $newPriority")
        // Notification is already handled by PriorityChangeReceiver
    }

    /**
     * Gets a list of tasks that need priority updates.
     * @param context Context for database access
     * @return List of tasks that should be checked for priority updates
     */
    fun getTasksNeedingPriorityUpdate(context: Context): List<Task> {
        val db = TaskDatabaseHelper(context)
        val allTasks = db.getAllTasks()
        val now = System.currentTimeMillis()

        return allTasks.filter { task ->
            // Only check tasks that are not completed
            !task.completed && task.deadlineDateTime > now
        }
    }

    /**
     * Checks if a task's priority should be updated based on time.
     * @param task Task to check
     * @return True if priority should be recalculated
     */
    fun shouldUpdatePriority(task: Task): Boolean {
        val now = System.currentTimeMillis()

        // Don't update completed tasks
        if (task.completed) return false

        // Check if deadline is within next 24 hours (high priority threshold)
        val hoursToDeadline = (task.deadlineDateTime - now) / (60 * 60 * 1000)

        // Update if:
        // 1. Task is within 24 hours of deadline and not High priority
        // 2. Or task is within 3 days and not Medium priority
        return when {
            hoursToDeadline <= 24 && task.priority != "High" -> true
            hoursToDeadline <= 72 && task.priority != "Medium" -> true
            else -> false
        }
    }

    /**
     * Runs periodic priority check for all tasks.
     * Call this from a service or when app opens.
     */
    fun runPeriodicPriorityCheck(context: Context) {
        val tasks = getTasksNeedingPriorityUpdate(context)

        if (tasks.isNotEmpty()) {
            updatePriorities(context, tasks) { changed ->
                if (changed) {
                    Log.d(TAG, "Periodic priority check: ${tasks.size} tasks checked, some updated")
                } else {
                    Log.d(TAG, "Periodic priority check: ${tasks.size} tasks checked, none updated")
                }
            }
        } else {
            Log.d(TAG, "Periodic priority check: No tasks need updating")
        }
    }
}