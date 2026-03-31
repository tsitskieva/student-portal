package com.example.studentportal.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String
)
