package com.example.studentportal.data.model

class Lesson(
    val id: String,
    val type: String,
    val title: String,
    val number: String,
    val time: String,
    val audience: String,
    val teacher: String,
    val typeOfTest: String,
    val building: String,
    val address: String,
    val dayOfWeek: Int,
    val weekType: String,
    val isEmptyLesson: Boolean = false,
    val group: Int = 0
) {
    companion object {
        fun createEmptyLesson(number: String, dayOfWeek: Int, weekType: String): Lesson {
            return Lesson(
                id = "",
                type = "",
                title = "",
                number = number,
                time = "",
                audience = "",
                teacher = "",
                typeOfTest = "",
                building = "",
                address = "",
                dayOfWeek = dayOfWeek,
                weekType = weekType,
                isEmptyLesson = true,
                group = 0
            )
        }
    }
}