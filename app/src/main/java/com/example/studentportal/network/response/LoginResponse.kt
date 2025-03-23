package com.example.studentportal.network.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "response")
    val response: InnerResponse
) {
    @JsonClass(generateAdapter = true)
    data class InnerResponse(
        @Json(name = "success")
        val success: Boolean,

        @Json(name = "token")
        val token: String
    )
}

