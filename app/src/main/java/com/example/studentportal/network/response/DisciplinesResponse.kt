package com.example.studentportal.network.response

import com.example.studentportal.data.model.Discipline
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DisciplinesResponse(
    @Json(name = "response") val response: Response
) {
    @JsonClass(generateAdapter = true)
    data class Response(
        @Json(name = "Disciplines") val disciplines: List<DisciplineResponse>,
        @Json(name = "Marks") val marks: Map<String, String>?
    )

    val mappedDisciplines
        get() = response.disciplines.map { discipline ->
            Discipline(
                id = discipline.ID,
                name = discipline.SubjectName,
                score = getScore(discipline),
                maxScore = getMaxScore(discipline)
            )
        }

    private fun getScore(discipline: DisciplineResponse): Int {
        return response.marks?.get(discipline.ID.toString())
            ?.replace("ECTS-", "")
            ?.toIntOrNull()
            ?: discipline.Rate?.toIntOrNull()
            ?: 0
    }

    private fun getMaxScore(discipline: DisciplineResponse): Int {
        return discipline.MaxCurrentRate?.toIntOrNull()
            ?: discipline.MaxRate?.toIntOrNull()
            ?: 100 // Значение по умолчанию
    }
}
