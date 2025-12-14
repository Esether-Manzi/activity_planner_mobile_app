package com.example.activityplanner.ui.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.activityplanner.R
import com.example.activityplanner.db.TaskDatabaseHelper
import com.example.activityplanner.model.Task
import com.example.activityplanner.ui.ToolbarUtils
import com.example.activityplanner.ui.notifications.NotificationScheduler
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private lateinit var db: TaskDatabaseHelper
    private lateinit var toolbar: MaterialToolbar

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etCategory: EditText
    private lateinit var btnStartDate: Button
    private lateinit var btnDeadlineDate: Button
    private lateinit var prioritySpinner: Spinner
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private lateinit var startCalendar: Calendar
    private lateinit var deadlineCalendar: Calendar
    private var selectedPriority: String = "Medium"

    private var editingTaskId: Int? = null
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_add)

        db = TaskDatabaseHelper(this)

        startCalendar = Calendar.getInstance()
        deadlineCalendar = Calendar.getInstance()

        // Toolbar and title

        toolbar = findViewById(R.id.toolbar)
        ToolbarUtils.attach(this, toolbar, "Add Task", showBack = true)

        // Initialize views
        initializeViews()

        // Setup priority spinner
        setupPrioritySpinner()

        // Setup date pickers
        setupDatePickers()

        // Setup buttons
        setupButtons()

        // If editing existing task
        loadTaskIfEditing()
    }

    private fun initializeViews() {
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etCategory = findViewById(R.id.etCategory)
        btnStartDate = findViewById(R.id.btnStartDate)
        btnDeadlineDate = findViewById(R.id.btnDeadlineDate)
        prioritySpinner = findViewById(R.id.spinnerPriority)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        // Set initial button text
        btnStartDate.text = "Select Start Date & Time"
        btnDeadlineDate.text = "Select Deadline Date & Time"
    }

    private fun setupPrioritySpinner() {
        // Get priorities from string array resource
        val priorities = resources.getStringArray(R.array.priorities_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = adapter
        prioritySpinner.setSelection(1) // Default to Medium (second item)

        prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedPriority = priorities[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedPriority = "Medium"
            }
        }
    }

    private fun setupDatePickers() {
        // Start date + time picker
        btnStartDate.setOnClickListener {
            val now = Calendar.getInstance()

            DatePickerDialog(this, { _, year, month, day ->
                TimePickerDialog(this, { _, hour, minute ->
                    startCalendar.set(year, month, day, hour, minute)
                    btnStartDate.text = "Start: ${dateFormatter.format(startCalendar.time)}"
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Deadline date + time picker
        btnDeadlineDate.setOnClickListener {
            val now = Calendar.getInstance()

            DatePickerDialog(this, { _, year, month, day ->
                TimePickerDialog(this, { _, hour, minute ->
                    deadlineCalendar.set(year, month, day, hour, minute)
                    btnDeadlineDate.text = "Deadline: ${dateFormatter.format(deadlineCalendar.time)}"
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupButtons() {
        // Save button
        btnSave.setOnClickListener {
            saveTask()
        }

        // Cancel button
        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveTask() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val category = etCategory.text.toString().trim()

        // Validate input
        if (title.isEmpty()) {
            etTitle.error = "Title is required"
            etTitle.requestFocus()
            return
        }

        // Check if dates are set
        if (btnStartDate.text.toString() == "Select Start Date & Time") {
            Toast.makeText(this, "Please select a start date and time", Toast.LENGTH_SHORT).show()
            return
        }

        if (btnDeadlineDate.text.toString() == "Select Deadline Date & Time") {
            Toast.makeText(this, "Please select a deadline date and time", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if deadline is after start date
        if (deadlineCalendar.timeInMillis <= startCalendar.timeInMillis) {
            Toast.makeText(this, "Deadline must be after start date", Toast.LENGTH_SHORT).show()
            return
        }

        val task = Task(
            id = editingTaskId ?: 0,
            title = title,
            description = description,
            startDateTime = startCalendar.timeInMillis,
            deadlineDateTime = deadlineCalendar.timeInMillis,
            priority = selectedPriority,
            category = category,
            completed = false
        )

        val result = if (editingTaskId == null) {
            db.insertTask(task)
        } else {
            db.updateTask(task).toLong()
        }

        if (result != -1L) {
            val message = if (editingTaskId == null) "Task added successfully" else "Task updated successfully"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            finish()
            val savedTask = db.getTaskById(result.toInt())
            savedTask?.let {
                // Schedule notifications
                NotificationScheduler.scheduleTaskNotifications(this, it)
            }
        } else {
            Toast.makeText(this, "Error saving task", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTaskIfEditing() {
        editingTaskId = intent.getIntExtra("TASK_ID", -1).takeIf { it != -1 }

        if (editingTaskId != null) {
            val existing = db.getTaskById(editingTaskId!!)
            if (existing != null) {

                // Fill form with existing data
                etTitle.setText(existing.title)
                etDescription.setText(existing.description)
                etCategory.setText(existing.category)

                // Set dates
                startCalendar.timeInMillis = existing.startDateTime
                deadlineCalendar.timeInMillis = existing.deadlineDateTime
                btnStartDate.text = "Start: ${dateFormatter.format(Date(existing.startDateTime))}"
                btnDeadlineDate.text = "Deadline: ${dateFormatter.format(Date(existing.deadlineDateTime))}"

                // Set priority
                val priorities = resources.getStringArray(R.array.priorities_array)
                val pos = priorities.indexOf(existing.priority)
                if (pos >= 0) prioritySpinner.setSelection(pos)
            }
        }
    }

    companion object {
        private const val EXTRA_TASK_ID = "TASK_ID"

        fun launch(context: Context) {
            val intent = Intent(context, AddTaskActivity::class.java)
            context.startActivity(intent)
        }

        fun launchForEdit(context: Context, taskId: Int) {
            val intent = Intent(context, AddTaskActivity::class.java)
            intent.putExtra(EXTRA_TASK_ID, taskId)
            context.startActivity(intent)
        }
    }
}