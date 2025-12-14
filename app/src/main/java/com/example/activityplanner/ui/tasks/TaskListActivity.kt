package com.example.activityplanner.ui.tasks


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.activityplanner.R
import com.example.activityplanner.db.TaskDatabaseHelper
import com.example.activityplanner.model.Task
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.activityplanner.ui.ToolbarUtils
import com.example.activityplanner.logic.AutoPriorityManager
import com.example.activityplanner.ui.AboutActivity
import com.example.activityplanner.ui.calendar.CalendarActivity
import com.example.activityplanner.ui.auth.LoginActivity
import com.example.activityplanner.session.SessionManager
import com.example.activityplanner.ui.notifications.NotificationScheduler

/**
 * TaskListActivity displays all tasks with priority badges, edit/delete actions, checkbox for multi-select,
 * and a FloatingActionButton to add a new task.
 */
class TaskListActivity : AppCompatActivity() {

    private lateinit var db: TaskDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        // Request notification permission (for Android 13+)
        requestNotificationPermission()

        scheduleNotificationsForExistingTasks()


        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        ToolbarUtils.attach(this, toolbar, "Activity Planner")

        // Initialize your views AFTER setContentView

        db = TaskDatabaseHelper(this)
        recyclerView = findViewById(R.id.rvTasks)
        fab = findViewById(R.id.fabAddTask)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(
            onEdit = { task -> EditTaskActivity.launch(this, task.id) },
            onComplete = { task, isCompleted ->
                db.updateTaskCompletion(task.id, isCompleted)
            },
            onDelete = { task ->
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete '${task.title}'?")
                    .setPositiveButton("Delete") { dialog, which ->
                        // Cancel notifications first
                        NotificationScheduler.cancelTaskNotifications(this, task.id)

                        // Then delete from database
                        val deleted = db.deleteTask(task.id)
                        if (deleted > 0) {
                            loadTasks()
                            Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        recyclerView.adapter = adapter

        // Add task via FAB
        fab.setOnClickListener {
            AddTaskActivity.launch(this)
        }

        // Bulk actions example (could be from a toolbar menu)
        // e.g., change priority for selected tasks to High
        // adapter.getSelectedIds().let { ids -> changePriority(ids, "High") }

        // Load items
        loadTasks()
    }

    /**
     * Loads tasks from DB and refreshes UI.
     */
    private fun loadTasks() {
        val tasks = db.getAllTasks()
        adapter.submitList(tasks)
        // Optionally run auto priority update on load
        AutoPriorityManager.updatePriorities(this, tasks) {
            // If changed, refresh
            if (it) adapter.submitList(db.getAllTasks())
        }
    }

    /**
     * Example bulk priority change for selected tasks.
     */
    private fun changePriority(selectedIds: List<Int>, newPriority: String) {
        selectedIds.forEach { id ->
            db.getTaskById(id)?.let { t ->
                db.updateTask(t.copy(priority = newPriority))
            }
        }
        loadTasks()
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] ==
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleNotificationsForExistingTasks() {
        val db = TaskDatabaseHelper(this)
        val tasks = db.getAllTasks()

        // Cancel all existing notifications first
        tasks.forEach { task ->
            NotificationScheduler.cancelTaskNotifications(this, task.id)
        }

        // Schedule new notifications
        tasks.forEach { task ->
            NotificationScheduler.scheduleTaskNotifications(this, task)
        }

        Log.d("Notifications", "Scheduled notifications for ${tasks.size} tasks")
    }

}
