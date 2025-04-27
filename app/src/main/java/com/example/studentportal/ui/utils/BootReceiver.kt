package com.example.studentportal.ui.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.studentportal.ui.brs.BrsCheckWorker
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationService(context).scheduleNotifications()
            val workRequest = PeriodicWorkRequestBuilder<BrsCheckWorker>(
                3, TimeUnit.HOURS
            ).build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}