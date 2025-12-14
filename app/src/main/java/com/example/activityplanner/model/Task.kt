package com.example.activityplanner.model

import android.database.Cursor
// Data model representing a Task item in the planner
data class Task(
    val id: Int = 0,
    val title: String,
    val description: String,
    val startDateTime: Long,   // Store as epoch millis for easy operations
    val deadlineDateTime: Long,
    val priority: String,      // "High", "Medium", "Low"
    val category: String,       // e.g., "Work", "Personal", "School"
    val completed: Boolean = false
){
    companion object {
        fun fromCursor(cursor: Cursor): Task {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val startDateTime = cursor.getLong(cursor.getColumnIndexOrThrow("startDateTime"))
            val deadlineDateTime = cursor.getLong(cursor.getColumnIndexOrThrow("deadlineDateTime"))
            val priority = cursor.getString(cursor.getColumnIndexOrThrow("priority"))
            val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
            val completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1

            return Task(
                id = id,
                title = title,
                description = description,
                startDateTime = startDateTime,
                deadlineDateTime = deadlineDateTime,
                priority = priority,
                category = category,
                completed = completed
            )
        }
    }
}
