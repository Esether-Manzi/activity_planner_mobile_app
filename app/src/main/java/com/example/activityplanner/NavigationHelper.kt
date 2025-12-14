package com.example.activityplanner


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.activityplanner.ui.tasks.TaskListActivity
import com.example.activityplanner.ui.tasks.AddTaskActivity
import com.example.activityplanner.ui.AboutActivity
import com.example.activityplanner.ui.calendar.CalendarActivity
import com.example.activityplanner.ui.auth.LoginActivity

object NavigationHelper {

    fun navigateToTaskList(context: Context) {
        val intent = Intent(context, TaskListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        context.startActivity(intent)
    }

    fun navigateToAddTask(context: Context) {
        val intent = Intent(context, AddTaskActivity::class.java)
        context.startActivity(intent)
    }

    fun navigateToCalendar(context: Context) {
        val intent = Intent(context, CalendarActivity::class.java)
        context.startActivity(intent)
    }

    fun navigateToAbout(context: Context) {
        val intent = Intent(context, AboutActivity::class.java)
        context.startActivity(intent)
    }

    fun logout(context: Context) {
        clearUserData(context)

        val loginIntent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(loginIntent)

        if (context is android.app.Activity) {
            context.finish()
        }
    }

    private fun clearUserData(context: Context) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
}