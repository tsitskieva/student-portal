package com.example.studentportal.network.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DisciplineResponse(
    @Json(name = "ID") val ID: Int,
    @Json(name = "SubjectName") val SubjectName: String,
    @Json(name = "Rate") val Rate: String?,
    @Json(name = "MaxCurrentRate") val MaxCurrentRate: String?,
    @Json(name = "MaxRate") val MaxRate: String?
)