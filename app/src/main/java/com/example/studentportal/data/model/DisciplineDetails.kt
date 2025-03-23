package com.example.studentportal.data.model

data class DisciplineDetails(
    val id: Int,
    val name: String,
    val score: Int,
    val maxScore: Int,
    val modules: List<Module>,
    val exam: Module?,
    val isEmpty: Boolean
)
