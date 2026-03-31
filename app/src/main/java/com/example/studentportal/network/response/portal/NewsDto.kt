package com.example.studentportal.network.response.portal

import com.example.studentportal.data.model.News

data class NewsDto(
    val id: String,
    val title: String,
    val date: String,
    val image: String,
    val categories: List<String>,
    val isImportant: Boolean,
    val author: String,
    val description: String,
    val galleryImages: List<String>
) {
    fun toModel(): News {
        return News(
            id = id,
            title = title,
            date = date,
            image = image,
            categories = categories,
            isImportant = isImportant,
            author = author,
            description = description,
            galleryImages = galleryImages
        )
    }
}