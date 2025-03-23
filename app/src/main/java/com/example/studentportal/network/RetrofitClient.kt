package com.example.studentportal.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val newUrl = originalRequest.url.newBuilder()
                .scheme(selectScheme(originalRequest))
                .build()

            chain.proceed(
                originalRequest.newBuilder()
                    .url(newUrl)
                    .build()
            )
        }
        .build()

    private fun selectScheme(request: Request): String {
        return if (request.url.host == "grade.sfedu.ru") {
            when {
                isAuthRequest(request) -> "https"
                else -> "http"
            }
        } else {
            "https"
        }
    }

    private fun isAuthRequest(request: Request): Boolean {
        return request.method == "POST" &&
                request.url.encodedPath.contains("auth/get_token")
    }

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(
                HttpUrl.Builder()
                    .scheme("http")
                    .host("grade.sfedu.ru")
                    .build()
            )
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}