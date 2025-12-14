package com.example.activityplanner.ui.notifications


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all alarms when device reboots
            NotificationScheduler.rescheduleAllAlarms(context)
        }
    }
}