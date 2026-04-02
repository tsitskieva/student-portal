package com.example.studentportal.data.model

data class ReferenceAssistantResponse(
    val canAnswer: Boolean,
    val answer: String,
    val sources: List<String>,
    val usedArticleIds: List<String>,
    val isAiGenerated: Boolean,
    val fallbackUsed: Boolean
)