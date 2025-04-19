package com.example.studentportal.ui.schedule.adapter

import Lesson
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.R
import com.example.studentportal.ui.schedule.ScheduleFragmentDirections

class LessonsAdapter(
    var lessons: List<Lesson>,
    var context: Context,
    private val navController: NavController,
    private val isCompactView: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_NORMAL = 1
        private const val VIEW_TYPE_COMPACT = 2
        private const val VIEW_TYPE_EMPTY = 3

        val lessonTimes = mapOf(
            "1-я пара" to "8:00-9:35",
            "2-я пара" to "9:50-11:25",
            "3-я пара" to "11:55-13:30",
            "4-я пара" to "13:45-15:20",
            "5-я пара" to "15:50-17:25"
        )
    }

    class NormalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val type: TextView = view.findViewById(R.id.lesson_list_type)
        val title: TextView = view.findViewById(R.id.lesson_list_title)
        val number: TextView = view.findViewById(R.id.lesson_list_number)
        val time: TextView = view.findViewById(R.id.lesson_list_time)
        val audience: TextView = view.findViewById(R.id.lesson_list_audience)
        val teacher: TextView = view.findViewById(R.id.lesson_list_teacher)
        val btn: LinearLayout = view.findViewById(R.id.lesson_button)
        val subgroup: TextView = view.findViewById(R.id.lesson_list_subgroup)
    }

    class CompactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val typeAndAudience: TextView = view.findViewById(R.id.lesson_list_type_and_audience_compact)
        val title: TextView = view.findViewById(R.id.lesson_list_title_compact)
        val number: TextView = view.findViewById(R.id.lesson_list_number_compact)
        val time: TextView = view.findViewById(R.id.lesson_list_time_compact)
        val btn: LinearLayout = view.findViewById(R.id.lesson_button)
    }

    class EmptyLessonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.lesson_list_number_empty)
        val time: TextView = view.findViewById(R.id.lesson_list_time_empty)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            lessons[position].isEmptyLesson -> VIEW_TYPE_EMPTY
            isCompactView -> VIEW_TYPE_COMPACT
            else -> VIEW_TYPE_NORMAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.lesson_in_list_empty, parent, false)
                EmptyLessonViewHolder(view)
            }
            VIEW_TYPE_COMPACT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.lesson_in_list_compact, parent, false)
                CompactViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.lesson_in_list, parent, false)
                NormalViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = lessons.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val lesson = lessons[position]

        when (holder) {
            is NormalViewHolder -> {
                holder.title.text = lesson.title
                holder.time.text = lesson.time
                holder.teacher.text = lesson.teacher
                holder.audience.text = "Ауд. ${lesson.audience}; корпус ${lesson.building}"
                holder.type.text = lesson.type
                holder.number.text = lesson.number

                when (lesson.subgroup) {
                    "1" -> {
                        holder.subgroup.visibility = View.VISIBLE
                        holder.subgroup.text = "1 подгруппа"
                    }
                    "2" -> {
                        holder.subgroup.visibility = View.VISIBLE
                        holder.subgroup.text = "2 подгруппа"
                    }
                    else -> holder.subgroup.visibility = View.GONE
                }

                holder.btn.setOnClickListener {
                    navigateToLessonDetail(lesson)
                }
            }
            is CompactViewHolder -> {
                holder.title.text = lesson.title
                holder.time.text = lesson.time
                holder.number.text = lesson.number
                holder.typeAndAudience.text = "${lesson.type} в ${lesson.audience} аудитории"

                holder.btn.setOnClickListener {
                    navigateToLessonDetail(lesson)
                }
            }
            is EmptyLessonViewHolder -> {
                holder.number.text = lesson.number
                holder.time.text = lessonTimes[lesson.number]
            }
        }
    }

    private fun navigateToLessonDetail(lesson: Lesson) {
        if (lesson.isEmptyLesson) return

        val buildingText = "${lesson.building}, ${lesson.address}"
        val action = ScheduleFragmentDirections.actionScheduleFragmentToLessonFragment(
            lessonId = lesson.id,
            lessonTitle = lesson.title,
            lessonTypeOfTest = lesson.typeOfTest,
            lessonType = lesson.type,
            lessonAudience = lesson.audience,
            lessonBuilding = buildingText,
            lessonTeacher = lesson.teacher
        )
        navController.navigate(action)
    }
}