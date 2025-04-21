package com.example.studentportal.data.model

data class Group(
    val group: String,
    val direction: String,
    val id: Int,
    var isActive: Boolean = false
)