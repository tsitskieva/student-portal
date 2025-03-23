package com.example.studentportal.ui.schedule

import android.annotation.SuppressLint
import android.app.Activity.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.R
import com.example.studentportal.data.model.Lesson
import com.example.studentportal.data.repository.LessonsRepository
import com.example.studentportal.ui.schedule.adapter.LessonsAdapter
import com.example.studentportal.ui.utils.WeekManager
import java.util.Calendar

class ScheduleFragment : Fragment() {
    private lateinit var weekYearTV: TextView
    private lateinit var weekTypeTV: TextView // TextView для отображения типа недели
    private lateinit var weekDays: List<TextView>
    private var weekManager: WeekManager? = null
    private lateinit var lessonsList: RecyclerView
    private lateinit var lessonsAdapter: LessonsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация элементов через view
        weekYearTV = view.findViewById(R.id.textView2)
        weekTypeTV = view.findViewById(R.id.textView3)
        lessonsList = view.findViewById(R.id.lessons_list)

        // Инициализация дней недели
        weekDays = listOf(
            view.findViewById(R.id.day1),
            view.findViewById(R.id.day2),
            view.findViewById(R.id.day3),
            view.findViewById(R.id.day4),
            view.findViewById(R.id.day5),
            view.findViewById(R.id.day6),
            view.findViewById(R.id.day7)
        )

        // Настройка RecyclerView
        lessonsList.layoutManager = LinearLayoutManager(requireContext())
        lessonsAdapter = LessonsAdapter(emptyList(), requireContext(),findNavController())
        lessonsList.adapter = lessonsAdapter

        // Восстановление выбранного дня
        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val selectedDayOfWeek = sharedPreferences.getInt("selectedDayOfWeek", Calendar.MONDAY)

        // Инициализация WeekManager
        weekManager = WeekManager(weekDays, weekYearTV, weekTypeTV).apply {
            setSelectedDayOfWeek(selectedDayOfWeek)
            weekDays[getCurrentDayIndex()].performClick()
        }

        // Обработчики кнопок
        view.findViewById<ImageView>(R.id.buttonBack).setOnClickListener {
            weekManager?.previousWeek()
            updateData()
        }

        view.findViewById<ImageView>(R.id.buttonNext).setOnClickListener {
            weekManager?.nextWeek()
            updateData()
        }

        // Обработчики дней недели
        weekDays.forEach { day ->
            day.setOnClickListener {
                val dayIndex = weekDays.indexOf(day)
                weekManager?.setSelectedDayOfWeek(Calendar.MONDAY + dayIndex)
                updateData()
            }
        }

        updateData()
    }

    private fun updateData() {
        updateDotsUnderDays()
        updateLessonsForSelectedDay()
        updateDaySelection()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateLessonsForSelectedDay() {
        val lessons = weekManager?.let {
            getLessonsForDay(it.getSelectedDayOfWeek(), it.getCurrentWeekType())
        } ?: emptyList()
        lessonsAdapter.lessons = lessons
        lessonsAdapter.notifyDataSetChanged()
    }

    private fun selectDay(selectedDay: TextView?) {
        if (selectedDay == null) return

        // Получаем сегодняшний день
        val todayCalendar = Calendar.getInstance()
        val todayDayOfMonth = todayCalendar.get(Calendar.DAY_OF_MONTH)
        val todayMonth = todayCalendar.get(Calendar.MONTH)
        val todayYear = todayCalendar.get(Calendar.YEAR)

        // Получаем выбранный день
        val selectedDayOfMonth = selectedDay.text.toString().toInt()
        val selectedMonth = todayCalendar.get(Calendar.MONTH)
        val selectedYear = todayCalendar.get(Calendar.YEAR)

        // Сбрасываем фон у всех дней, кроме сегодняшнего
        for (day in weekDays) {
            val dayOfMonth = day.text.toString().toInt()
            val month = todayCalendar.get(Calendar.MONTH)
            val year = todayCalendar.get(Calendar.YEAR)

            // Проверяем, является ли текущий день сегодняшним
            if (dayOfMonth == todayDayOfMonth && month == todayMonth && year == todayYear) {
                // Применяем фон для сегодняшнего дня
                day.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.today_circle_background)
            } else {
                // Сбрасываем фон, если день не сегодняшний
                day.background = null
            }
        }

        // Если выбранный день не сегодняшний, выделяем его circle_background
        if (selectedDayOfMonth != todayDayOfMonth || selectedMonth != todayMonth || selectedYear != todayYear) {
            selectedDay.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.circle_background)
        }
    }

    private fun getLessonsForDay(dayOfWeek: Int, weekType: String): List<Lesson> {
        // Приводим weekType к нижнему регистру и убираем слово "неделя"
        val normalizedWeekType =
            weekType.toLowerCase().replace(" неделя", "") // Убираем " неделя", если есть

        val filteredLessons = LessonsRepository.lessons.filter {
            it.dayOfWeek == dayOfWeek && (it.weekType.toLowerCase() == normalizedWeekType || it.weekType == "обе")
        }
        Log.d(
            "MainActivity",
            "Filtered lessons for day $dayOfWeek and weekType $normalizedWeekType: $filteredLessons"
        )
        return filteredLessons
    }

    private fun getNumberOfLessonsForDay(dayOfMonth: Int, month: Int, year: Int): Int {
        val calendar = Calendar.getInstance().apply {
            set(year, month, dayOfMonth)
        }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val weekType = weekManager?.getCurrentWeekType() ?: "Верхняя неделя"

        // Приводим weekType к нижнему регистру и убираем слово "неделя"
        val normalizedWeekType = weekType.toLowerCase().replace(" неделя", "")

        // Фильтруем пары по дню недели и типу недели
        val lessons = LessonsRepository.lessons.filter {
            it.dayOfWeek == dayOfWeek && (it.weekType.toLowerCase() == normalizedWeekType || it.weekType == "обе")
        }

        return lessons.size
    }

    private fun updateDotsUnderDays() {
        val view = requireView()
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        // Устанавливаем календарь на начало текущей недели (понедельник)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        // Получаем контейнеры для точек
        val dotsContainers = listOf(
            view.findViewById<LinearLayout>(R.id.dotsContainer1),
            view.findViewById<LinearLayout>(R.id.dotsContainer2),
            view.findViewById<LinearLayout>(R.id.dotsContainer3),
            view.findViewById<LinearLayout>(R.id.dotsContainer4),
            view.findViewById<LinearLayout>(R.id.dotsContainer5),
            view.findViewById<LinearLayout>(R.id.dotsContainer6),
            view.findViewById<LinearLayout>(R.id.dotsContainer7)
        )

        // Очищаем контейнеры перед обновлением
        dotsContainers.forEach { it.removeAllViews() }

        // Обновляем точки для каждого дня недели
        for (i in 0 until 7) {
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val numberOfLessons = getNumberOfLessonsForDay(dayOfMonth, currentMonth, currentYear)

            // Создаем точки (ImageView с вектором ic_dot)
            for (j in 0 until numberOfLessons) {
                val dotView = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.ic_dot) // Устанавливаем вектор
                    layoutParams = LinearLayout.LayoutParams(
                        resources.getDimensionPixelSize(R.dimen.dot_size), // 3dp
                        resources.getDimensionPixelSize(R.dimen.dot_size)  // 3dp
                    )
                    val margin =
                        resources.getDimensionPixelSize(R.dimen.dot_margin) // Отступ между точками
                    (layoutParams as LinearLayout.LayoutParams).setMargins(margin, 0, margin, 0)
                }
                dotsContainers[i].addView(dotView) // Добавляем точку в контейнер
            }

            // Переходим к следующему дню
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun updateDaySelection() {
        val selectedDayOfWeek = weekManager?.getSelectedDayOfWeek() ?: Calendar.MONDAY
        val dayIndex = selectedDayOfWeek - Calendar.MONDAY
        if (dayIndex in weekDays.indices) {
            selectDay(weekDays[dayIndex])
        }
    }
}