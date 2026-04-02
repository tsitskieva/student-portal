package com.example.studentportal.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class ReferenceChatMessage(
    val role: String,
    val text: String
)