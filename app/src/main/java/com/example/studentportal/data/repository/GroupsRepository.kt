package com.example.studentportal.data.repository

import com.example.studentportal.data.model.Group
import com.example.studentportal.network.PortalApiService
import com.example.studentportal.network.PortalRetrofitClient

object GroupsRepository {
    private val apiService: PortalApiService by lazy {
        PortalRetrofitClient.instance.create(PortalApiService::class.java)
    }

    private var cachedGroups: List<Group> = emptyList()

    suspend fun getGroups(forceRefresh: Boolean = false): List<Group> {
        if (!forceRefresh && cachedGroups.isNotEmpty()) {
            return cachedGroups
        }

        cachedGroups = apiService.getGroups()
            .map { it.toModel() }
            .sortedBy { it.group }

        return cachedGroups
    }
}