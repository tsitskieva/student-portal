package com.example.studentportal.ui.theme


data class Lesson(val subject: String, val time: String)
data class Day(val date: String, val lessons: List<Lesson>)
