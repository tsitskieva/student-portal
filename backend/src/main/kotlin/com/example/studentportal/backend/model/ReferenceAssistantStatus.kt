package com.example.studentportal.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class ReferenceAssistantStatus(
    val provider: String,
    val baseUrl: String,
    val model: String,
    val ollamaReachable: Boolean,
    val modelAvailable: Boolean,
    val canUseLocalLlm: Boolean,
    val message: String
)
