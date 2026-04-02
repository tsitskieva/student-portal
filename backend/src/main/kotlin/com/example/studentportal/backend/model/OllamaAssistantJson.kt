package com.example.studentportal.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class OllamaAssistantJson(
    val canAnswer: Boolean,
    val answer: String,
    val sourceTitles: List<String> = emptyList()
)
