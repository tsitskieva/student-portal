package com.example.studentportal.data.repository

import com.example.studentportal.data.model.News
import com.example.studentportal.network.PortalApiService
import com.example.studentportal.network.PortalRetrofitClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NewsRepository {
    private val apiService: PortalApiService by lazy {
        PortalRetrofitClient.instance.create(PortalApiService::class.java)
    }

    private var cachedNews: List<News> = emptyList()

    suspend fun getNews(forceRefresh: Boolean = false): List<News> {
        if (!forceRefresh && cachedNews.isNotEmpty()) {
            return cachedNews
        }

        cachedNews = apiService.getNews()
            .map { it.toModel() }
            .sortedByDescending { parseDate(it.date) }

        return cachedNews
    }

    suspend fun getNewsById(id: String, forceRefresh: Boolean = false): News {
        val news = getNews(forceRefresh).firstOrNull { it.id == id }
        if (news != null) {
            return news
        }

        return apiService.getNewsById(id).toModel()
    }

    private fun parseDate(dateString: String): Date {
        return try {
            SimpleDateFormat("dd MMM. yyyy г.", Locale("ru")).parse(dateString) ?: Date(0)
        } catch (_: Exception) {
            Date(0)
        }
    }
}