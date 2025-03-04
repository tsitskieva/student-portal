package com.example.studentportal

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.data.lesson
import com.example.studentportal.ui.adapter.CalendarManager
import com.example.studentportal.ui.adapter.lessonsAdapter


class MainActivity : ComponentActivity() {
    private lateinit var weekLayout: ConstraintLayout
    private lateinit var monthLayout: ConstraintLayout
    private lateinit var monthlyLinearLayout: View
    private lateinit var monthYearTV: View
    private var monthYearText: TextView? = null
    private var calendarRecyclerView: RecyclerView? = null
    private var calendarManager: CalendarManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)
        initWidgets()
        // Инициализация CalendarManager
        calendarManager = CalendarManager(monthYearText, calendarRecyclerView)

        weekLayout = findViewById(R.id.week_Layout)
        monthLayout = findViewById(R.id.month_Layout)
        monthlyLinearLayout = findViewById(R.id.monthly_linearlayout)
        monthYearTV = findViewById(R.id.monthYearTV)

        monthlyLinearLayout.setOnClickListener {
            // Скрываем weekLayout
            weekLayout.visibility = View.GONE
            // Показываем monthLayout
            monthLayout.visibility = View.VISIBLE
            setLessonsListConstraints(true) // Передаем true, так как monthLayout виден
        }

        monthYearTV.setOnClickListener {
            // Скрываем monthLayout
            monthLayout.visibility = View.GONE
            // Показываем weekLayout
            weekLayout.visibility = View.VISIBLE
            setLessonsListConstraints(false) // Передаем false, так как weekLayout виден
        }

        val lessonsList: RecyclerView = findViewById(R.id.lessons_list)
        //массив с парами
        val lessons = arrayListOf<lesson>()

        lessons.add(lesson("Семинар", "Философия","1-я пара","09:50-11:25","Ауд. 405; корпус ИВТиПТ","Новохатько Александр Григорьевич"))
        lessons.add(lesson("Семинар", "Философия","2-я пара","11:55-13:25","Ауд. 405; корпус ИВТиПТ","Новохатько Александр Григорьевич"))
        lessons.add(lesson("Семинар", "Философия","3-я пара","13:45-15:20","Ауд. 405; корпус ИВТиПТ","Новохатько Александр Григорьевич"))
        lessons.add(lesson("Семинар", "Философия","4-я пара","15:50-17:25","Ауд. 405; корпус ИВТиПТ","Новохатько Александр Григорьевич"))

        lessonsList.layoutManager = LinearLayoutManager(this)
        lessonsList.adapter = lessonsAdapter(lessons, this)


    }
    private fun initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        monthYearText = findViewById(R.id.monthYearTV)
    }
    fun previousMonthAction(view: View?) {
        calendarManager!!.previousMonth() // Вызов метода для переключения на предыдущий месяц
    }

    fun nextMonthAction(view: View?) {
        calendarManager!!.nextMonth() // Вызов метода для переключения на следующий месяц
    }
    private fun setLessonsListConstraints(isMonthLayoutVisible: Boolean) {
        val lessonsList = findViewById<RecyclerView>(R.id.lessons_list)
        val params = lessonsList.layoutParams as ConstraintLayout.LayoutParams

        // Устанавливаем зависимость от видимого ConstraintLayout
        if (isMonthLayoutVisible) {
            params.topToBottom = R.id.month_Layout // Устанавливаем зависимость от monthLayout
        } else {
            params.topToBottom = R.id.week_Layout // Устанавливаем зависимость от weekLayout
        }

        // Устанавливаем верхний отступ в 30dp
        val marginInPixels = (30 * resources.displayMetrics.density).toInt() // Преобразуем dp в пиксели
        params.topMargin = marginInPixels

        // Применяем обновленные параметры
        lessonsList.layoutParams = params
    }


}


