package com.example.studentportal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.ui.theme.lessonsAdapter


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

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
}


