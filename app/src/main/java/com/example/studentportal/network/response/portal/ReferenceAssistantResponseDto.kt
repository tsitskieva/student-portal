package com.example.studentportal.network.response.portal

import com.example.studentportal.data.model.ReferenceAssistantResponse

data class ReferenceAssistantResponseDto(
    val canAnswer: Boolean,
    val answer: String,
    val sources: List<String>,
    val usedArticleIds: List<String>,
    val isAiGenerated: Boolean,
    val fallbackUsed: Boolean
) {
    fun toModel(): ReferenceAssistantResponse {
        return ReferenceAssistantResponse(
            canAnswer = canAnswer,
            answer = answer,
            sources = sources,
            usedArticleIds = usedArticleIds,
            isAiGenerated = isAiGenerated,
            fallbackUsed = fallbackUsed
        )
    }
}