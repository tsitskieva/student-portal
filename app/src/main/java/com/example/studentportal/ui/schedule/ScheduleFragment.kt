package com.example.studentportal.ui.schedule

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
    private lateinit var weekTypeTV: TextView
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

        weekYearTV = view.findViewById(R.id.textView2)
        weekTypeTV = view.findViewById(R.id.textView3)
        lessonsList = view.findViewById(R.id.lessons_list)

        weekDays = listOf(
            view.findViewById(R.id.day1),
            view.findViewById(R.id.day2),
            view.findViewById(R.id.day3),
            view.findViewById(R.id.day4),
            view.findViewById(R.id.day5),
            view.findViewById(R.id.day6),
            view.findViewById(R.id.day7)
        )

        lessonsList.layoutManager = LinearLayoutManager(requireContext())
        lessonsAdapter = LessonsAdapter(emptyList(), requireContext(), findNavController())
        lessonsList.adapter = lessonsAdapter

        // Инициализация с текущим днем
        weekManager = WeekManager(weekDays, weekYearTV, weekTypeTV).apply {
            setSelectedDayOfWeek(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
            weekDays[getCurrentDayIndex()].performClick()
        }

        view.findViewById<ImageView>(R.id.buttonBack).setOnClickListener {
            weekManager?.previousWeek()
            updateData()
        }

        view.findViewById<ImageView>(R.id.buttonNext).setOnClickListener {
            weekManager?.nextWeek()
            updateData()
        }

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
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateLessonsForSelectedDay() {
        val lessons = weekManager?.let {
            getLessonsForDay(it.getSelectedDayOfWeek(), it.getCurrentWeekType())
        } ?: emptyList()
        lessonsAdapter.lessons = lessons
        lessonsAdapter.notifyDataSetChanged()
    }

    private fun getLessonsForDay(dayOfWeek: Int, weekType: String): List<Lesson> {
        val normalizedWeekType = weekType.toLowerCase().replace(" неделя", "")
        val filteredLessons = LessonsRepository.lessons.filter {
            it.dayOfWeek == dayOfWeek && (it.weekType.toLowerCase() == normalizedWeekType || it.weekType == "обе")
        }
        Log.d(
            "ScheduleFragment",
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
        val normalizedWeekType = weekType.toLowerCase().replace(" неделя", "")

        return LessonsRepository.lessons.count {
            it.dayOfWeek == dayOfWeek && (it.weekType.toLowerCase() == normalizedWeekType || it.weekType == "обе")
        }
    }

    private fun updateDotsUnderDays() {
        val view = requireView()
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dotsContainers = listOf(
            view.findViewById<LinearLayout>(R.id.dotsContainer1),
            view.findViewById<LinearLayout>(R.id.dotsContainer2),
            view.findViewById<LinearLayout>(R.id.dotsContainer3),
            view.findViewById<LinearLayout>(R.id.dotsContainer4),
            view.findViewById<LinearLayout>(R.id.dotsContainer5),
            view.findViewById<LinearLayout>(R.id.dotsContainer6),
            view.findViewById<LinearLayout>(R.id.dotsContainer7)
        )

        dotsContainers.forEach { it.removeAllViews() }

        for (i in 0 until 7) {
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val numberOfLessons = getNumberOfLessonsForDay(dayOfMonth, currentMonth, currentYear)

            for (j in 0 until numberOfLessons) {
                val dotView = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.ic_dot)
                    layoutParams = LinearLayout.LayoutParams(
                        resources.getDimensionPixelSize(R.dimen.dot_size),
                        resources.getDimensionPixelSize(R.dimen.dot_size)
                    )
                    val margin = resources.getDimensionPixelSize(R.dimen.dot_margin)
                    (layoutParams as LinearLayout.LayoutParams).setMargins(margin, 0, margin, 0)
                }
                dotsContainers[i].addView(dotView)
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}