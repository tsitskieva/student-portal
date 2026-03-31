package com.example.studentportal.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class NewsItem(
    val id: String,
    val title: String,
    val date: String,
    val image: String,
    val categories: List<String>,
    val isImportant: Boolean,
    val author: String,
    val description: String,
    val galleryImages: List<String>
)
