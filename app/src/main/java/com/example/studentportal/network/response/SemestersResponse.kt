package com.example.studentportal.network.response

import com.example.studentportal.data.model.Semester
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SemestersResponse(
    @Json(name = "response") val response: Map<String, SemesterResponse>
) {
    val semesters: List<Semester>
        get() = response.values.map {
            Semester(it.id, it.year, it.num)
        }.sortedByDescending { "${it.year}${it.num}" }
}