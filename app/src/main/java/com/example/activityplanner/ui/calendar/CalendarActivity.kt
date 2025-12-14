package com.example.activityplanner.ui.calendar

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.activityplanner.R
import com.example.activityplanner.db.TaskDatabaseHelper
import com.example.activityplanner.model.Task
import com.example.activityplanner.ui.ToolbarUtils
import com.example.activityplanner.ui.tasks.TaskAdapter
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var tvSelectedDate: TextView
    private lateinit var rvTasksForDate: RecyclerView
    private lateinit var tvNoTasks: TextView
    private lateinit var toolbar: MaterialToolbar

    private lateinit var taskAdapter: TaskAdapter
    private var selectedDate: Date = Date()
    private lateinit var dbHelper: TaskDatabaseHelper

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        dbHelper = TaskDatabaseHelper(this)
        initializeViews()
        setupCalendar()
        setupRecyclerView()
        loadTasksForSelectedDate()


        //page Toolbar with title

        toolbar = findViewById(R.id.toolbar)
        ToolbarUtils.attach(this, toolbar, "My calendar", showBack = true)
    }

    private fun initializeViews() {
        calendarView = findViewById(R.id.calendarView)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        rvTasksForDate = findViewById(R.id.rvTasksForDate)
        tvNoTasks = findViewById(R.id.tvNoTasks)
    }

    private fun setupCalendar() {
        calendarView.date = selectedDate.time

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedDate = calendar.time
            updateSelectedDateDisplay()
            loadTasksForSelectedDate()
        }

        updateSelectedDateDisplay()
    }

    private fun updateSelectedDateDisplay() {
        tvSelectedDate.text = "Tasks for ${dateFormatter.format(selectedDate)}"
    }

    private fun setupRecyclerView() {
        rvTasksForDate.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with proper parameters matching your TaskAdapter
        taskAdapter = TaskAdapter(
            onEdit = { task ->
                // Navigate to EditTaskActivity when Edit button is clicked
                val intent = Intent(this, com.example.activityplanner.ui.tasks.EditTaskActivity::class.java).apply {
                    putExtra("TASK_ID", task.id)
                }
                startActivity(intent)
            },
            onDelete = { task ->
                // Handle delete functionality
                if (task.id != 0) {
                    dbHelper.deleteTask(task.id)
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
                    loadTasksForSelectedDate() // Refresh the list
                }
            },
            onComplete = { task, isChecked ->
                // Update completion status in database
                dbHelper.updateTaskCompletion(task.id, isChecked)
                Toast.makeText(
                    this,
                    if (isChecked) "Task marked as completed" else "Task marked as pending",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        rvTasksForDate.adapter = taskAdapter
    }

    private fun loadTasksForSelectedDate() {
        val allTasks = dbHelper.getAllTasks()

        val tasksForDate = allTasks.filter { task ->
            isTaskOnDate(task, selectedDate)
        }

        // Update adapter with new tasks using submitList
        taskAdapter.submitList(tasksForDate)

        if (tasksForDate.isEmpty()) {
            tvNoTasks.visibility = View.VISIBLE
            rvTasksForDate.visibility = View.GONE
        } else {
            tvNoTasks.visibility = View.GONE
            rvTasksForDate.visibility = View.VISIBLE
        }
    }

    private fun isTaskOnDate(task: Task, date: Date): Boolean {
        val calendar = Calendar.getInstance()

        // Normalize the selected date to start of day (00:00:00)
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val normalizedDate = calendar.time.time

        // Normalize the selected date to end of day (23:59:59.999)
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time.time

        // Check if task falls on this date (using startDateTime)
        val taskStartTime = task.startDateTime
        val taskDeadlineTime = task.deadlineDateTime

        // A task is considered "on this date" if either:
        // 1. Task starts on this date
        // 2. Task deadline is on this date
        // 3. This date is between task start and deadline

        // Normalize task start to start of its day
        calendar.time = Date(taskStartTime)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val taskStartDate = calendar.time.time

        // Normalize task deadline to end of its day
        calendar.time = Date(taskDeadlineTime)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val taskDeadlineDate = calendar.time.time

        return normalizedDate in taskStartDate..taskDeadlineDate
    }

    // Helper method to format date for display
    private fun formatDate(millis: Long): String {
        return dateTimeFormatter.format(Date(millis))
    }

    override fun onResume() {
        super.onResume()
        loadTasksForSelectedDate()
    }
}