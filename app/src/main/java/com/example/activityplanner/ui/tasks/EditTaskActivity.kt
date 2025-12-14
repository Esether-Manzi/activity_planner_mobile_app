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
import java.util.Calendar

class EditTaskActivity : AppCompatActivity() {

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

    private var taskId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        db = TaskDatabaseHelper(this)
        startCalendar = Calendar.getInstance()
        deadlineCalendar = Calendar.getInstance()

        //page Toolbar with title

        toolbar = findViewById(R.id.toolbar)
        ToolbarUtils.attach(this, toolbar, "Edit Task", showBack = true)

        setupCancelButton()

        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etCategory = findViewById(R.id.etCategory)
        btnStartDate = findViewById(R.id.btnStartDate)
        btnDeadlineDate = findViewById(R.id.btnDeadlineDate)
        prioritySpinner = findViewById(R.id.spinnerPriority)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)





        val priorities = arrayOf("High", "Medium", "Low")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        prioritySpinner.adapter = adapter

        prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedPriority = priorities[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnStartDate.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                TimePickerDialog(this, { _, hour, minute ->
                    startCalendar.set(year, month, day, hour, minute)
                    btnStartDate.text = startCalendar.time.toString()
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnDeadlineDate.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                TimePickerDialog(this, { _, hour, minute ->
                    deadlineCalendar.set(year, month, day, hour, minute)
                    btnDeadlineDate.text = deadlineCalendar.time.toString()
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }

        taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        if (taskId == -1) {
            Toast.makeText(this, "Invalid task", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val existing = db.getTaskById(taskId)
        if (existing != null) {
            etTitle.setText(existing.title)
            etDescription.setText(existing.description)
            etCategory.setText(existing.category)
            startCalendar.timeInMillis = existing.startDateTime
            deadlineCalendar.timeInMillis = existing.deadlineDateTime
            btnStartDate.text = startCalendar.time.toString()
            btnDeadlineDate.text = deadlineCalendar.time.toString()
            val pos = priorities.indexOf(existing.priority)
            if (pos >= 0) prioritySpinner.setSelection(pos)
            selectedPriority = existing.priority
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val category = etCategory.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedTask = Task(
                id = taskId,
                title = title,
                description = description,
                startDateTime = startCalendar.timeInMillis,
                deadlineDateTime = deadlineCalendar.timeInMillis,
                priority = selectedPriority,
                category = category
            )

            val result = db.updateTask(updatedTask)
            if (result > 0) {
                Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show()
                finish()
                // After updating task
                NotificationScheduler.cancelTaskNotifications(this, taskId)
                NotificationScheduler.scheduleTaskNotifications(this, updatedTask)
            } else {
                Toast.makeText(this, "Error updating task", Toast.LENGTH_SHORT).show()
            }
        }


    }
    fun setupCancelButton() {

        findViewById<View>(R.id.btnCancel)?.setOnClickListener {
            finish() // Go back to previous activity
        }
    }

    companion object {
        private const val EXTRA_TASK_ID = "task_id"

        fun launch(context: Context, taskId: Int) {
            val intent = Intent(context, EditTaskActivity::class.java)
            intent.putExtra(EXTRA_TASK_ID, taskId)
            context.startActivity(intent)
        }
    }
}
