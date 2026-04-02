package com.example.studentportal.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class ReferenceAssistantResponse(
    val canAnswer: Boolean,
    val answer: String,
    val sources: List<String> = emptyList(),
    val usedArticleIds: List<String> = emptyList(),
    val isAiGenerated: Boolean = false,
    val fallbackUsed: Boolean = false
)