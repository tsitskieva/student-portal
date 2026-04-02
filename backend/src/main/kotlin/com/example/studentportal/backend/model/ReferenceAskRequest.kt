package com.example.studentportal.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class ReferenceAskRequest(
    val question: String,
    val history: List<ReferenceChatMessage> = emptyList()
)