package com.example.studentportal.network.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SemesterResponse(
    @Json(name = "ID") val id: Int,
    @Json(name = "Year") val year: Int,
    @Json(name = "Num") val num: Int
)