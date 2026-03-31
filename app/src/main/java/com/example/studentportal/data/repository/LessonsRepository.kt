package com.example.studentportal.data.repository

import Lesson
import android.content.Context
import android.util.Log
import com.example.studentportal.network.PortalApiService
import com.example.studentportal.network.PortalRetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

object LessonsRepository {
    private const val PREFS_NAME = "LessonsCachePrefs"
    private const val LESSONS_KEY_PREFIX = "lessons_"

    private val apiService: PortalApiService by lazy {
        PortalRetrofitClient.instance.create(PortalApiService::class.java)
    }

    private val gson = Gson()

    suspend fun loadLessons(
        context: Context,
        group: String,
        forceRefresh: Boolean = false
    ): List<Lesson> {
        if (!forceRefresh) {
            val cached = getCachedLessons(context, group)
            if (cached.isNotEmpty()) {
                return cached
            }
        }

        return try {
            val lessons = apiService.getLessons(group = group)
                .map { it.toModel() }

            saveLessons(context, group, lessons)
            lessons
        } catch (e: Exception) {
            Log.e("LessonsRepository", "Не удалось загрузить расписание с backend, используем кэш", e)
            getCachedLessons(context, group)
        }
    }

    fun getCachedLessons(context: Context, group: String): List<Lesson> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LESSONS_KEY_PREFIX + group, null)
            ?: return emptyList()

        val type = object : TypeToken<List<Lesson>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun getRelevantLessons(context: Context, group: String, currentWeekType: String): List<Lesson> {
        val normalizedWeekType = normalizeWeekType(currentWeekType)

        return getCachedLessons(context, group).filter { lesson ->
            lesson.group == group && (
                    normalizedWeekType.isBlank() ||
                            normalizeWeekType(lesson.weekType) == normalizedWeekType ||
                            lesson.weekType.equals("обе", ignoreCase = true)
                    )
        }
    }

    fun getTodaysLessons(context: Context, group: String, currentWeekType: String): List<Lesson> {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        return getRelevantLessons(context, group, currentWeekType)
            .filter { it.dayOfWeek == today }
            .sortedBy { it.getStartTime() }
    }

    private fun saveLessons(context: Context, group: String, lessons: List<Lesson>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LESSONS_KEY_PREFIX + group, gson.toJson(lessons))
            .apply()
    }

    private fun normalizeWeekType(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace(" неделя", "")
    }
}