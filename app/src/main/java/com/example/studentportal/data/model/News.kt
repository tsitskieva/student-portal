package com.example.studentportal.data.model

data class News(
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