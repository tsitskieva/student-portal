package com.example.studentportal.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupItem(
    val group: String,
    val direction: String,
    val id: Int
)
