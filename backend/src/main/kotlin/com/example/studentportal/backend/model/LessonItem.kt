package com.example.studentportal.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class LessonItem(
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
)
