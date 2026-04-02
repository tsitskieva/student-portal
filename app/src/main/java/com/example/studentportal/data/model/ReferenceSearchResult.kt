package com.example.studentportal.data.model

data class ReferenceSearchResult(
    val id: String,
    val category: String,
    val title: String,
    val shortAnswer: String,
    val fullAnswer: String,
    val sourceSection: String,
    val matchedKeywords: List<String>
)
