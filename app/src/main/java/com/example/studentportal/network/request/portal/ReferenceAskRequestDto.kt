package com.example.studentportal.network.request.portal

import com.example.studentportal.data.model.ReferenceChatMessage

data class ReferenceAskRequestDto(
    val question: String,
    val history: List<ReferenceChatMessageDto>
) {
    companion object {
        fun from(question: String, history: List<ReferenceChatMessage>): ReferenceAskRequestDto {
            return ReferenceAskRequestDto(
                question = question,
                history = history.map {
                    ReferenceChatMessageDto(
                        role = it.role,
                        text = it.text
                    )
                }
            )
        }
    }
}

data class ReferenceChatMessageDto(
    val role: String,
    val text: String
)