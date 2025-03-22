package com.example.studentportal

import WeekManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.data.dataLessons
import com.example.studentportal.data.lesson
//import com.example.studentportal.ui.adapter.CalendarManager
import com.example.studentportal.ui.adapter.lessonsAdapter
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private lateinit var weekLayout: ConstraintLayout
    private lateinit var monthLayout: ConstraintLayout
    private lateinit var monthlyLinearLayout: View
    private lateinit var monthYearTV: View
    private lateinit var weekYearTV: TextView
    private lateinit var weekTypeTV: TextView // TextView для отображения типа недели
    private var monthYearText: TextView? = null
    private var calendarRecyclerView: RecyclerView? = null
//    private var calendarManager: CalendarManager? = null
    private lateinit var weekDays: List<TextView>
    private var weekManager: WeekManager? = null
    private lateinit var lessonsList: RecyclerView
    private lateinit var lessonsAdapter: lessonsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)
        initWidgets()

        // Восстанавливаем выбранный день из SharedPreferences
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val selectedDayOfWeek = sharedPreferences.getInt("selectedDayOfWeek", Calendar.MONDAY)
        weekManager?.setSelectedDayOfWeek(selectedDayOfWeek)

        // Инициализация WeekManager
        weekManager = WeekManager(weekDays, weekYearTV, weekTypeTV)

        // Получаем индекс сегодняшнего дня
        val todayIndex = weekManager?.getCurrentDayIndex() ?: 0

        // Выделяем сегодняшний день, если индекс в пределах списка
        if (todayIndex in weekDays.indices) {
            selectDay(weekDays[todayIndex])
            weekManager?.setSelectedDayOfWeek(Calendar.MONDAY + todayIndex) // Устанавливаем выбранный день
        }

        // Обновляем точки под днями
        updateDotsUnderDays()

        // Обработка нажатий на кнопки "назад" и "вперед"
        findViewById<ImageView>(R.id.buttonBack).setOnClickListener {
            weekManager?.previousWeek()
            updateLessonsForSelectedDay() // Обновляем список пар при изменении недели
            updateDotsUnderDays()
            updateDaySelection() // Обновляем выделение выбранного дня
        }
        findViewById<ImageView>(R.id.buttonNext).setOnClickListener {
            weekManager?.nextWeek()
            updateLessonsForSelectedDay() // Обновляем список пар при изменении недели
            updateDotsUnderDays()
            updateDaySelection() // Обновляем выделение выбранного дня
        }

        // Инициализация CalendarManager
//        calendarManager = CalendarManager(monthYearText, calendarRecyclerView)

        weekLayout = findViewById(R.id.week_Layout)
//        monthLayout = findViewById(R.id.month_Layout)
        monthlyLinearLayout = findViewById(R.id.monthly_linearlayout)
//        monthYearTV = findViewById(R.id.monthYearTV)

//        monthlyLinearLayout.setOnClickListener {
//            // Скрываем weekLayout
//            weekLayout.visibility = View.GONE
//            // Показываем monthLayout
//            monthLayout.visibility = View.VISIBLE
//            setLessonsListConstraints(true) // Передаем true, так как monthLayout виден
//        }
//
//        monthYearTV.setOnClickListener {
//            // Скрываем monthLayout
//            monthLayout.visibility = View.GONE
//            // Показываем weekLayout
//            weekLayout.visibility = View.VISIBLE
//            setLessonsListConstraints(false) // Передаем false, так как weekLayout виден
//        }

        // Инициализация RecyclerView и адаптера
        lessonsList = findViewById(R.id.lessons_list)
        lessonsList.layoutManager = LinearLayoutManager(this)
        lessonsAdapter = lessonsAdapter(emptyList(), this)
        lessonsList.adapter = lessonsAdapter

        // Обновляем список пар для выбранного дня
        updateLessonsForSelectedDay()
    }

    private fun initWidgets() {
        weekYearTV = findViewById(R.id.textView2)
        weekTypeTV = findViewById(R.id.textView3) // Инициализация TextView для типа недели
        weekLayout = findViewById(R.id.week_Layout)
//        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
//        monthYearText = findViewById(R.id.monthYearTV)
        weekDays = listOf(
            findViewById(R.id.day1),
            findViewById(R.id.day2),
            findViewById(R.id.day3),
            findViewById(R.id.day4),
            findViewById(R.id.day5),
            findViewById(R.id.day6),
            findViewById(R.id.day7)
        )
        // Устанавливаем обработчики нажатия на дни недели
        for (day in weekDays) {
            day.setOnClickListener {
                selectDay(day)
                val dayIndex = weekDays.indexOf(day)
                val selectedDayOfWeek = Calendar.MONDAY + dayIndex
                weekManager?.setSelectedDayOfWeek(selectedDayOfWeek)

                // Сохраняем выбранный день
                saveSelectedDay(selectedDayOfWeek)

                updateLessonsForSelectedDay() // Обновляем список пар
            }
        }
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
                day.background = ContextCompat.getDrawable(this, R.drawable.today_circle_background)
            } else {
                // Сбрасываем фон, если день не сегодняшний
                day.background = null
            }
        }

        // Если выбранный день не сегодняшний, выделяем его circle_background
        if (selectedDayOfMonth != todayDayOfMonth || selectedMonth != todayMonth || selectedYear != todayYear) {
            selectedDay.background = ContextCompat.getDrawable(this, R.drawable.circle_background)
        }
    }

    private fun updateLessonsForSelectedDay() {
        val selectedDayOfWeek = weekManager?.getSelectedDayOfWeek() ?: Calendar.MONDAY
        val weekType = weekManager?.getCurrentWeekType() ?: "Верхняя неделя"
        Log.d("MainActivity", "Selected day of week: $selectedDayOfWeek, Week type: $weekType")

        val lessons = getLessonsForDay(selectedDayOfWeek, weekType)
        Log.d("MainActivity", "Lessons to display: $lessons")

        lessonsAdapter.lessons = lessons
        lessonsAdapter.notifyDataSetChanged()
    }

    private fun getLessonsForDay(dayOfWeek: Int, weekType: String): List<lesson> {
        // Приводим weekType к нижнему регистру и убираем слово "неделя"
        val normalizedWeekType = weekType
            .toLowerCase()
            .replace(" неделя", "") // Убираем " неделя", если есть

        val filteredLessons = dataLessons.lessons.filter {
            it.dayOfWeek == dayOfWeek &&
                    (it.weekType.toLowerCase() == normalizedWeekType || it.weekType == "обе")
        }
        Log.d("MainActivity", "Filtered lessons for day $dayOfWeek and weekType $normalizedWeekType: $filteredLessons")
        return filteredLessons
    }

    private fun getNumberOfLessonsForDay(dayOfMonth: Int, month: Int, year: Int): Int {
        val calendar = Calendar.getInstance().apply {
            set(year, month, dayOfMonth)
        }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val weekType = weekManager?.getCurrentWeekType() ?: "Верхняя неделя"

        // Приводим weekType к нижнему регистру и убираем слово "неделя"
        val normalizedWeekType = weekType
            .toLowerCase()
            .replace(" неделя", "")

        // Фильтруем пары по дню недели и типу недели
        val lessons = dataLessons.lessons.filter {
            it.dayOfWeek == dayOfWeek &&
                    (it.weekType.toLowerCase() == normalizedWeekType || it.weekType == "обе")
        }

        return lessons.size
    }

    private fun updateDotsUnderDays() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        // Устанавливаем календарь на начало текущей недели (понедельник)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        // Получаем контейнеры для точек
        val dotsContainers = listOf(
            findViewById<LinearLayout>(R.id.dotsContainer1),
            findViewById<LinearLayout>(R.id.dotsContainer2),
            findViewById<LinearLayout>(R.id.dotsContainer3),
            findViewById<LinearLayout>(R.id.dotsContainer4),
            findViewById<LinearLayout>(R.id.dotsContainer5),
            findViewById<LinearLayout>(R.id.dotsContainer6),
            findViewById<LinearLayout>(R.id.dotsContainer7)
        )

        // Очищаем контейнеры перед обновлением
        dotsContainers.forEach { it.removeAllViews() }

        // Обновляем точки для каждого дня недели
        for (i in 0 until 7) {
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val numberOfLessons = getNumberOfLessonsForDay(dayOfMonth, currentMonth, currentYear)

            // Создаем точки (ImageView с вектором ic_dot)
            for (j in 0 until numberOfLessons) {
                val dotView = ImageView(this).apply {
                    setImageResource(R.drawable.ic_dot) // Устанавливаем вектор
                    layoutParams = LinearLayout.LayoutParams(
                        resources.getDimensionPixelSize(R.dimen.dot_size), // 3dp
                        resources.getDimensionPixelSize(R.dimen.dot_size)  // 3dp
                    )
                    val margin = resources.getDimensionPixelSize(R.dimen.dot_margin) // Отступ между точками
                    (layoutParams as LinearLayout.LayoutParams).setMargins(margin, 0, margin, 0)
                }
                dotsContainers[i].addView(dotView) // Добавляем точку в контейнер
            }

            // Переходим к следующему дню
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun saveSelectedDay(dayOfWeek: Int) {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("selectedDayOfWeek", dayOfWeek)
        editor.apply()
    }
    private fun updateDaySelection() {
        val selectedDayOfWeek = weekManager?.getSelectedDayOfWeek() ?: Calendar.MONDAY
        val dayIndex = selectedDayOfWeek - Calendar.MONDAY
        if (dayIndex in weekDays.indices) {
            selectDay(weekDays[dayIndex])
        }
    }

//    fun previousMonthAction(view: View?) {
//        calendarManager?.previousMonth() // Вызов метода для переключения на предыдущий месяц
//    }
//
//    fun nextMonthAction(view: View?) {
//        calendarManager?.nextMonth() // Вызов метода для переключения на следующий месяц
//    }

//    private fun setLessonsListConstraints(isMonthLayoutVisible: Boolean) {
//        val lessonsList = findViewById<RecyclerView>(R.id.lessons_list)
//        val params = lessonsList.layoutParams as ConstraintLayout.LayoutParams
//
//        // Устанавливаем зависимость от видимого ConstraintLayout
//        if (isMonthLayoutVisible) {
//            params.topToBottom = R.id.month_Layout // Устанавливаем зависимость от monthLayout
//        } else {
//            params.topToBottom = R.id.week_Layout // Устанавливаем зависимость от weekLayout
//        }
//
//        // Устанавливаем верхний отступ в 30dp
//        val marginInPixels = (30 * resources.displayMetrics.density).toInt() // Преобразуем dp в пиксели
//        params.topMargin = marginInPixels
//
//        // Применяем обновленные параметры
//        lessonsList.layoutParams = params
//    }
}