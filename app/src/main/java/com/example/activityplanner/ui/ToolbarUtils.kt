package com.example.activityplanner.ui


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.activityplanner.R
import com.example.activityplanner.session.SessionManager
import com.example.activityplanner.ui.AboutActivity
import com.example.activityplanner.ui.calendar.CalendarActivity
import com.example.activityplanner.ui.auth.LoginActivity
import com.example.activityplanner.ui.tasks.AddTaskActivity
import com.example.activityplanner.ui.tasks.TaskListActivity
import com.google.android.material.appbar.MaterialToolbar

object ToolbarUtils {
    fun attach(activity: AppCompatActivity, toolbar: MaterialToolbar, title: String, showBack: Boolean = false) {
        activity.supportActionBar?.title = title
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(showBack)

        toolbar.setNavigationOnClickListener {
            if (showBack) activity.onBackPressedDispatcher.onBackPressed()
        }

        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.main_navigation_menu)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_task_list -> {
                    activity.startActivity(Intent(activity, TaskListActivity::class.java))
                    true
                }
                R.id.action_calendar -> {
                    activity.startActivity(Intent(activity, CalendarActivity::class.java))
                    true
                }
                R.id.action_about -> {
                    activity.startActivity(Intent(activity, AboutActivity::class.java))
                    true
                }
                R.id.action_add_task -> {
                    activity.startActivity(Intent(activity, AddTaskActivity::class.java))
                    true
                }
                R.id.action_logout -> {
                    SessionManager(activity).clearSession()
                    activity.startActivity(Intent(activity, LoginActivity::class.java))
                    activity.finish()
                    true
                }
                else -> false
            }
        }
    }
}
