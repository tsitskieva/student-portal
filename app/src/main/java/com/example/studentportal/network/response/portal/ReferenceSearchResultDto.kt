package com.example.studentportal.network.response.portal

import com.example.studentportal.data.model.ReferenceSearchResult

data class ReferenceSearchResultDto(
    val id: String,
    val category: String,
    val title: String,
    val shortAnswer: String,
    val fullAnswer: String,
    val sourceSection: String,
    val matchedKeywords: List<String>
) {
    fun toModel(): ReferenceSearchResult {
        return ReferenceSearchResult(
            id = id,
            category = category,
            title = title,
            shortAnswer = shortAnswer,
            fullAnswer = fullAnswer,
            sourceSection = sourceSection,
            matchedKeywords = matchedKeywords
        )
    }
}
