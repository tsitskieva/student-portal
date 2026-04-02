package com.example.studentportal.backend.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAiAssistantJson(
    @SerialName("canAnswer")
    val canAnswer: Boolean,
    @SerialName("answer")
    val answer: String,
    @SerialName("sourceTitles")
    val sourceTitles: List<String> = emptyList()
)