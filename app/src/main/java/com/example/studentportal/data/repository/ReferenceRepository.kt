package com.example.studentportal.data.repository

import com.example.studentportal.data.model.ReferenceAssistantResponse
import com.example.studentportal.data.model.ReferenceChatMessage
import com.example.studentportal.network.PortalApiService
import com.example.studentportal.network.PortalRetrofitClient
import com.example.studentportal.network.request.portal.ReferenceAskRequestDto

object ReferenceRepository {
    private val apiService: PortalApiService by lazy {
        PortalRetrofitClient.instance.create(PortalApiService::class.java)
    }

    private var cachedTopics: List<String> = emptyList()

    suspend fun getTopics(forceRefresh: Boolean = false): List<String> {
        if (!forceRefresh && cachedTopics.isNotEmpty()) {
            return cachedTopics
        }

        cachedTopics = apiService.getReferenceTopics()
        return cachedTopics
    }

    suspend fun ask(question: String, history: List<ReferenceChatMessage>): ReferenceAssistantResponse {
        return apiService.askReference(
            ReferenceAskRequestDto.from(question, history)
        ).toModel()
    }
}