package com.example.studentportal.ui.utils

import Lesson
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.studentportal.R
import com.example.studentportal.data.repository.LessonsRepository
import com.example.studentportal.ui.profile.managers.SelectedGroupsManager
import java.util.*
import com.example.studentportal.ui.activities.MainActivity

class NotificationService(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "lessons_notifications"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Уведомления о парах",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о начале пар"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotifications() {
        cancelAllNotifications()

        val sharedPrefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isNotificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", false)
        if (!isNotificationsEnabled) return

        val hoursBefore = sharedPrefs.getInt("notification_hours", 8) // 8 по умолчанию
        val minutesBefore = sharedPrefs.getInt("notification_minutes", 0) // 0 по умолчанию
        if (hoursBefore == 0 && minutesBefore == 0) return

        val notifyOnlyBeforeFirst = sharedPrefs.getBoolean("notify_only_before_first", false)
        val selectedGroups = SelectedGroupsManager.getSelectedGroups(context)
        val activeGroup = selectedGroups.find { it.isActive } ?: return

        // Простое определение типа недели (можно заменить на вашу логику)
        val calendar = Calendar.getInstance()
        val weekType = if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) "Нижняя неделя" else "Верхняя неделя"

        val todayLessons = LessonsRepository.getTodaysLessons(activeGroup.group, weekType)

        if (todayLessons.isEmpty()) {
            Log.d("Notifications", "No lessons today for group ${activeGroup.group}")
            return
        }

        if (notifyOnlyBeforeFirst) {
            // Берем только первую пару дня
            todayLessons.minByOrNull { it.getStartTime() }?.let { firstLesson ->
                scheduleNotificationForLesson(firstLesson, hoursBefore, minutesBefore)
            }
        } else {
            // Стандартный режим - все пары
            todayLessons.forEach { lesson ->
                scheduleNotificationForLesson(lesson, hoursBefore, minutesBefore)
            }
        }
    }

    private fun scheduleNotificationForLesson(lesson: Lesson, hoursBefore: Int, minutesBefore: Int) {
        val notificationTime = getNotificationTime(lesson.time, hoursBefore, minutesBefore)
        if (notificationTime.timeInMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("hours_before", hoursBefore)
            putExtra("minutes_before", minutesBefore)
            putExtra("lesson_type", lesson.type)
            putExtra("lesson_title", lesson.title)
            putExtra("lesson_audience", lesson.audience)
            putExtra("lesson_building", lesson.building)
            putExtra("notification_id", NOTIFICATION_ID + lesson.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            lesson.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                notificationTime.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun getNotificationTime(lessonTime: String, hoursBefore: Int, minutesBefore: Int): Calendar {
        val calendar = Calendar.getInstance()
        val startTimeStr = lessonTime.split("-")[0].trim()
        val (hour, minute) = startTimeStr.split(":").map { it.toInt() }

        // Устанавливаем время начала пары
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Вычитаем время для уведомления
        calendar.add(Calendar.HOUR, -hoursBefore)
        calendar.add(Calendar.MINUTE, -minutesBefore)

        return calendar
    }

    fun cancelAllNotifications() {
        val selectedGroups = SelectedGroupsManager.getSelectedGroups(context)
        selectedGroups.forEach { group ->
            LessonsRepository.getRelevantLessons(group.group, "").forEach { lesson ->
                val intent = Intent(context, NotificationReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    lesson.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    fun showNotification(title: String, message: String, notificationId: Int, pendingIntent: PendingIntent? = null) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_SCHEDULE", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_schedule)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(message)
                .setBigContentTitle(title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}