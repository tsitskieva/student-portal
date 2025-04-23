package com.example.studentportal.ui.utils

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.studentportal.ui.activities.MainActivity

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val hoursBefore = intent.getIntExtra("hours_before", 0)
        val minutesBefore = intent.getIntExtra("minutes_before", 0)
        val type = intent.getStringExtra("lesson_type") ?: ""
        val title = intent.getStringExtra("lesson_title") ?: ""
        val audience = intent.getStringExtra("lesson_audience") ?: ""
        val building = intent.getStringExtra("lesson_building") ?: ""
        val notificationId = intent.getIntExtra("notification_id", 1001)

        // Создаем Intent для открытия MainActivity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_SCHEDULE_FRAGMENT", true)
        }

        // Создаем PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Форматирование времени
        val timeParts = mutableListOf<String>()
        if (hoursBefore > 0) timeParts.add("$hoursBefore ч.")
        if (minutesBefore > 0) timeParts.add("$minutesBefore мин.")
        val timeText = "Через ${timeParts.joinToString(" ")}"

        // Форматирование типа занятия
        val typeFormatted = type.replaceFirstChar { it.lowercase() }

        // Формирование основного текста
        val notificationText = buildString {
            append(title)

            // Добавляем строку с аудиторией/корпусом только если ОБА значения не "-"
            if (audience != "-" && building != "-") {
                append("\n${audience}, ${building}")
            }
        }

        // Создаем и показываем уведомление
        NotificationService(context).showNotification(
            "$timeText $typeFormatted",
            notificationText,
            notificationId,
            pendingIntent
        )
    }
}