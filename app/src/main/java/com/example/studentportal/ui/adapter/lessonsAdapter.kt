package com.example.studentportal.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lesson_in_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lessons.count()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text =  lessons[position].title
        holder.time.text =  lessons[position].time
        holder.teacher.text =  lessons[position].teacher
        holder.audience.text =  lessons[position].audience
        holder.type.text =  lessons[position].type
        holder.number.text =  lessons[position].number
    }

}