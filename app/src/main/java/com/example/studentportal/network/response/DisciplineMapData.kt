package com.example.studentportal.network.response

data class DisciplineMapData(
    val Exam: Int?,
    val Modules: Map<String, ModuleData>?
)

