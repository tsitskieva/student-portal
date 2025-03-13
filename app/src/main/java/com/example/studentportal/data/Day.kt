package com.example.studentportal.data

data class Day(
    val dayOfMonth: Int, // Число месяца (1, 2, 3, ...)
    val isCurrentMonth: Boolean // Принадлежит ли день текущему месяцу
)