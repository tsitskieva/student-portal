package com.example.studentportal.ui.utils

import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.studentportal.R
import java.text.SimpleDateFormat
import java.util.*

class WeekManager(
    private val weekDays: List<TextView>,
    private val monthYearTextView: TextView,
    private val weekTypeTextView: TextView
) {
    private val calendar: Calendar = Calendar.getInstance()
    private val monthYearFormat = SimpleDateFormat("LLLL yyyy", Locale("ru"))
    private var selectedDayOfWeek: Int = calendar.get(Calendar.DAY_OF_WEEK)

    init {
        calendar.firstDayOfWeek = Calendar.MONDAY
        updateWeek()
    }

    fun nextWeek() {
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        updateWeek()
    }

    fun previousWeek() {
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        updateWeek()
    }

    private fun updateWeek() {
        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val todayCalendar = Calendar.getInstance()
        val todayDayOfMonth = todayCalendar.get(Calendar.DAY_OF_MONTH)
        val todayMonth = todayCalendar.get(Calendar.MONTH)
        val todayYear = todayCalendar.get(Calendar.YEAR)
        val todayDayOfWeek = todayCalendar.get(Calendar.DAY_OF_WEEK)

        for (i in 0 until 7) {
            val dayOfMonth = tempCalendar.get(Calendar.DAY_OF_MONTH)
            val month = tempCalendar.get(Calendar.MONTH)
            val year = tempCalendar.get(Calendar.YEAR)
            val currentDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK)

            val formattedDay = String.format(Locale.getDefault(), "%02d", dayOfMonth)
            weekDays[i].text = formattedDay

            // Reset all styles
            weekDays[i].background = null
            weekDays[i].setTextColor(ContextCompat.getColor(weekDays[i].context, R.color.text_primary))

            val isToday = dayOfMonth == todayDayOfMonth &&
                    month == todayMonth &&
                    year == todayYear
            val isSelected = currentDayOfWeek == selectedDayOfWeek

            when {
                isToday && isSelected -> {
                    weekDays[i].background = ContextCompat.getDrawable(
                        weekDays[i].context,
                        R.drawable.today_circle_background
                    )
                    weekDays[i].setTextColor(ContextCompat.getColor(weekDays[i].context, android.R.color.white))
                }
                isToday -> {
                    weekDays[i].background = ContextCompat.getDrawable(
                        weekDays[i].context,
                        R.drawable.today_circle_background
                    )
                    weekDays[i].setTextColor(ContextCompat.getColor(weekDays[i].context, android.R.color.white))
                }
                isSelected -> {
                    weekDays[i].background = ContextCompat.getDrawable(
                        weekDays[i].context,
                        R.drawable.circle_background
                    )
                    weekDays[i].setTextColor(ContextCompat.getColor(weekDays[i].context, android.R.color.white))
                }
            }

            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val monthYearText = monthYearFormat.format(calendar.time)
        monthYearTextView.text = monthYearText.replaceFirstChar { it.uppercase() }

        val weekType = getWeekType()
        weekTypeTextView.text = weekType
    }

    private fun getWeekType(): String {
        val startDate = Calendar.getInstance().apply {
            set(2024, Calendar.SEPTEMBER, 2)
        }

        val diffInMillis = calendar.timeInMillis - startDate.timeInMillis
        val diffInWeeks = (diffInMillis / (1000 * 60 * 60 * 24 * 7)).toInt()

        return if (diffInWeeks % 2 == 0) "Нижняя неделя" else "Верхняя" +
                "" +
                "" +
                "" +
                "" +
                " неделя"
    }

    fun getCurrentWeekTypeSimple(): String {
        return if (getWeekType().startsWith("Верхняя")) "верхняя" else "нижняя"
    }

    fun setSelectedDayOfWeek(dayOfWeek: Int) {
        selectedDayOfWeek = dayOfWeek
        Log.d("WeekManager", "Selected day of week: $dayOfWeek")
        updateWeek()
    }

    fun getSelectedDayOfWeek(): Int {
        return selectedDayOfWeek
    }

    fun getCurrentWeekType(): String {
        return getWeekType()
    }

    fun getCurrentDayIndex(): Int {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.SUNDAY -> 6
            else -> dayOfWeek - 2
        }
    }
}