package com.example.studentportal.ui.schedule

import Lesson
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.R
import com.example.studentportal.data.repository.LessonsRepository
import com.example.studentportal.ui.profile.managers.SelectedGroupsManager
import com.example.studentportal.ui.schedule.adapter.LessonsAdapter
import com.example.studentportal.ui.utils.WeekManager
import java.util.Calendar

class ScheduleFragment : Fragment() {
    private var isCompactView: Boolean = false
    private lateinit var weekYearTV: TextView
    private lateinit var weekTypeTV: TextView
    private lateinit var weekDays: List<TextView>
    private var weekManager: WeekManager? = null
    private lateinit var lessonsList: RecyclerView
    private lateinit var lessonsAdapter: LessonsAdapter
    private lateinit var noChosenGroupContainer: ConstraintLayout
    private lateinit var noLessonGroupContainer: ConstraintLayout
    private lateinit var addGroupButton: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noLessonGroupContainer = view.findViewById(R.id.empty_state_lesson_container)
        noChosenGroupContainer = view.findViewById(R.id.no_choosen_state_group_container)
        addGroupButton = view.findViewById(R.id.addGroup1)

        addGroupButton.setOnClickListener {
            findNavController().navigate(R.id.action_scheduleFragment_to_groupsSettings)
        }

        val sharedPrefs = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        isCompactView = sharedPrefs.getBoolean("compact_view_enabled", false)

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
        lessonsAdapter = LessonsAdapter(emptyList(), requireContext(), findNavController(), isCompactView)
        lessonsList.adapter = lessonsAdapter

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
                val dayOfWeek = if (dayIndex == 6) Calendar.SUNDAY else Calendar.MONDAY + dayIndex
                weekManager?.setSelectedDayOfWeek(dayOfWeek)
                updateData()
            }
        }

        updateData()

        parentFragmentManager.setFragmentResultListener("active_group_changed", viewLifecycleOwner) { _, _ ->
            updateData()
        }
    }

    private fun updateData() {
        val activeGroup = SelectedGroupsManager.getSelectedGroups(requireContext()).find { it.isActive }

        if (activeGroup == null) {
            // Нет активной группы - показываем состояние "Добавить группу"
            showNoGroupState()
            return
        } else {
            // Есть активная группа - скрываем контейнер "Добавить группу"
            noChosenGroupContainer.visibility = View.GONE
            lessonsList.visibility = View.VISIBLE
        }

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

        // Обновляем состояние видимости контейнеров
        updateContainersVisibility(lessons.isEmpty())
    }

    private fun updateContainersVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            // Если пар нет - показываем контейнер "Нет пар"
            noLessonGroupContainer.visibility = View.VISIBLE
            lessonsList.visibility = View.GONE
        } else {
            // Если пары есть - показываем список
            noLessonGroupContainer.visibility = View.GONE
            lessonsList.visibility = View.VISIBLE
        }
    }

    private fun showNoGroupState() {
        noChosenGroupContainer.visibility = View.VISIBLE
        lessonsList.visibility = View.GONE
        noLessonGroupContainer.visibility = View.GONE
    }

    private fun getLessonsForDay(dayOfWeek: Int, weekType: String): List<Lesson> {

        val activeGroup = SelectedGroupsManager.getSelectedGroups(requireContext()).find { it.isActive }
            ?: return emptyList()

        val normalizedWeekType = weekType.toLowerCase().replace(" неделя", "")
        val realLessons = LessonsRepository.lessons.filter {
            it.dayOfWeek == dayOfWeek &&
                    (it.weekType.toLowerCase() == normalizedWeekType || it.weekType == "обе") &&
                    it.group == activeGroup.group
        }

        // Если нет реальных пар - возвращаем пустой список
        if (realLessons.isEmpty()) return emptyList()

        val sharedPrefs = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val showEmptyLessons = sharedPrefs.getBoolean("show_empty_lessons", false)

        // Если не показываем пустые пары - возвращаем только реальные
        if (!showEmptyLessons) return realLessons

        // Получаем номера реальных пар
        val realLessonNumbers = realLessons.map { it.number }.toSet()

        // Все возможные номера пар
        val allLessonNumbers = listOf("1-я пара", "2-я пара", "3-я пара", "4-я пара", "5-я пара")

        // Создаем список всех пар (реальных и пустых)
        return allLessonNumbers.map { number ->
            if (number in realLessonNumbers) {
                realLessons.first { it.number == number }
            } else {
                Lesson.createEmptyLesson(number, dayOfWeek, weekType)
            }
        }
    }

    private fun getNumberOfLessonsForDay(dayOfMonth: Int, month: Int, year: Int): Int {
        val activeGroup = SelectedGroupsManager.getSelectedGroups(requireContext()).find { it.isActive }
            ?: return 0

        val calendar = Calendar.getInstance().apply {
            set(year, month, dayOfMonth)
        }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val weekType = weekManager?.getCurrentWeekType() ?: "Верхняя неделя"
        val normalizedWeekType = weekType.toLowerCase().replace(" неделя", "")

        return LessonsRepository.lessons.count {
            it.dayOfWeek == dayOfWeek &&
                    (it.weekType.toLowerCase() == normalizedWeekType || it.weekType == "обе") &&
                    !it.isEmptyLesson &&
                    it.group == activeGroup.group
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

    override fun onResume() {
        super.onResume()
        val sharedPrefs = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // Проверяем изменения компактного вида
        val newCompactViewSetting = sharedPrefs.getBoolean("compact_view_enabled", false)
        if (newCompactViewSetting != isCompactView) {
            isCompactView = newCompactViewSetting
            lessonsAdapter = LessonsAdapter(lessonsAdapter.lessons, requireContext(), findNavController(), isCompactView)
            lessonsList.adapter = lessonsAdapter
        }

        // Проверяем изменения отображения пустых пар
        updateData()
    }
}