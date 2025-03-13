package com.example.studentportal.ui.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.LessonActivity
import com.example.studentportal.R
import com.example.studentportal.data.lesson

class lessonsAdapter(var lessons: List<lesson>, var context: Context): RecyclerView.Adapter<lessonsAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val type: TextView = view.findViewById(R.id.lesson_list_type)
        val title: TextView = view.findViewById(R.id.lesson_list_title)
        val number: TextView = view.findViewById(R.id.lesson_list_number)
        val time: TextView = view.findViewById(R.id.lesson_list_time)
        val audience: TextView = view.findViewById(R.id.lesson_list_audience)
        val teacher: TextView = view.findViewById(R.id.lesson_list_teacher)
        val btn: LinearLayout = view.findViewById(R.id.lesson_button)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lesson_in_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lessons.count()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val lesson = lessons[position]
        Log.d("lessonsAdapter", "Binding lesson: $lesson")

        // Устанавливаем текст для каждого TextView
        holder.title.text = lesson.title
        holder.time.text = lesson.time
        holder.teacher.text = lesson.teacher

        // Формируем строку для аудитории и корпуса
        val audienceText = "Ауд. ${lesson.audience}; корпус ${lesson.building}"
        holder.audience.text = audienceText

        val buildingText = "${lesson.building}, ${lesson.adress}"

        holder.type.text = lesson.type
        holder.number.text = lesson.number

        holder.btn.setOnClickListener {
            val intent = Intent(context, LessonActivity::class.java)

            intent.putExtra("lessonId", lesson.id) // lessonId — уникальный идентификатор пары
            intent.putExtra("lessonTitle", lesson.title)
            intent.putExtra("lessonTypeOfTest", lesson.typeOfTest)
            intent.putExtra("lessonType", lesson.type)
            intent.putExtra("lessonAudience", lesson.audience)
            intent.putExtra("lessonBuilding", buildingText)
            intent.putExtra("lessonTeacher", lesson.teacher)

            context.startActivity(intent)
        }
    }

}