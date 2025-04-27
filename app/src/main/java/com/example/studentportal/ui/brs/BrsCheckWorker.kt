package com.example.studentportal.ui.brs

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.preference.PreferenceManager
import com.example.studentportal.data.model.Discipline
import com.example.studentportal.data.repository.BRSRepository
import com.example.studentportal.ui.utils.NotificationService

class BrsCheckWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = BRSRepository(applicationContext)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        if (!sharedPrefs.getBoolean("brs_notifications_enabled", true)) {
            Log.d("BrsCheck", "Notifications disabled")
            return Result.success()
        }

        try {
            val token = repository.getActiveToken() // Добавьте получение токена
            val semesters = repository.getSemesters(token)
            semesters.forEach { semester ->
                val changed = repository.checkForScoreChanges(semester.id)
                if (changed.isNotEmpty()) {
                    sendNotifications(changed)
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun sendNotifications(disciplines: List<Discipline>) {
        val notificationService = NotificationService(applicationContext)
        disciplines.forEach { discipline ->
            notificationService.showBrsNotification(
                "Обновление баллов",
                "${discipline.name}: ${discipline.score}/${discipline.maxScore}",
                discipline.id
            )
        }
    }
}