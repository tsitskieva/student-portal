package com.example.studentportal.network.response

data class DisciplineDetailsResponse(
    val response: Response
) {
    data class Response(
        val Discipline: DisciplineDetailsData,
        val Submodules: Map<String, SubmoduleData>,
        val DisciplineMap: DisciplineMapData?
    )
}
