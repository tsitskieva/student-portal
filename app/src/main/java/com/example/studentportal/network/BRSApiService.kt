package com.example.studentportal.network

import com.example.studentportal.network.response.DisciplineDetailsResponse
import com.example.studentportal.network.response.DisciplinesResponse
import com.example.studentportal.network.response.LoginResponse
import com.example.studentportal.network.response.SemestersResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BRSApiService {
    @FormUrlEncoded
    @POST("api/v1/auth/get_token")
    suspend fun login(
        @Field("login") login: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("api/v1/student/semester_list")
    suspend fun getSemesters(
        @Query("token") token: String
    ): SemestersResponse

    @GET("api/v1/student/discipline/subject")
    suspend fun getDisciplineDetails(
        @Query("token") token: String,
        @Query("id") disciplineId: Int
    ): DisciplineDetailsResponse

    @GET("api/v1/student")
    suspend fun getDisciplines(
        @Query("token") token: String,
        @Query("SemesterID") semesterId: Int
    ): DisciplinesResponse
}