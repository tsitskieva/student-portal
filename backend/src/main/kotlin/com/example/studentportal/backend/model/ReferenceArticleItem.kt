package com.example.studentportal.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class ReferenceArticleItem(
    val id: String,
    val category: String,
    val title: String,
    val keywords: List<String>,
    val exampleQuestions: List<String>,
    val summary: String,
    val content: String,
    val sourceSection: String
)