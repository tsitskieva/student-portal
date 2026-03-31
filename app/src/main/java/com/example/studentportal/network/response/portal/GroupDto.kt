package com.example.studentportal.network.response.portal

import com.example.studentportal.data.model.Group

data class GroupDto(
    val group: String,
    val direction: String,
    val id: Int
) {
    fun toModel(): Group {
        return Group(
            group = group,
            direction = direction,
            id = id
        )
    }
}