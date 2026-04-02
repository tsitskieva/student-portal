package com.example.studentportal.network

import com.example.studentportal.network.request.portal.ReferenceAskRequestDto
import com.example.studentportal.network.response.portal.GroupDto
import com.example.studentportal.network.response.portal.LessonDto
import com.example.studentportal.network.response.portal.NewsDto
import com.example.studentportal.network.response.portal.ReferenceAssistantResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PortalApiService {
    @GET("api/v1/news")
    suspend fun getNews(
        @Query("important") important: Boolean? = null,
        @Query("category") category: String? = null,
        @Query("limit") limit: Int? = null
    ): List<NewsDto>

    @GET("api/v1/news/{id}")
    suspend fun getNewsById(
        @Path("id") id: String
    ): NewsDto

    @GET("api/v1/groups")
    suspend fun getGroups(): List<GroupDto>

    @GET("api/v1/schedule")
    suspend fun getLessons(
        @Query("group") group: String,
        @Query("dayOfWeek") dayOfWeek: Int? = null,
        @Query("weekType") weekType: String? = null
    ): List<LessonDto>

    @GET("api/v1/reference/topics")
    suspend fun getReferenceTopics(): List<String>

    @POST("api/v1/reference/ask")
    suspend fun askReference(
        @Body request: ReferenceAskRequestDto
    ): ReferenceAssistantResponseDto
}