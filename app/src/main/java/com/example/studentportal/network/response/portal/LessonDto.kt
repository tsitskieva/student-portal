package com.example.studentportal.network.response.portal

import Lesson

data class LessonDto(
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
    val subgroup: String,
    val group: String,
    val isEmptyLesson: Boolean = false
) {
    fun toModel(): Lesson {
        return Lesson(
            id = id,
            type = type,
            title = title,
            number = number,
            time = time,
            audience = audience,
            teacher = teacher,
            typeOfTest = typeOfTest,
            building = building,
            address = address,
            dayOfWeek = dayOfWeek,
            weekType = weekType,
            subgroup = subgroup,
            group = group,
            isEmptyLesson = isEmptyLesson
        )
    }
}