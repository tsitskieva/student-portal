package com.example.studentportal.backend

import com.example.studentportal.backend.model.ErrorResponse
import com.example.studentportal.backend.storage.FilePortalRepository
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main() {
    val repository = FilePortalRepository()

    embeddedServer(Netty, host = "0.0.0.0", port = 8080) {
        configureSerialization()
        configureRouting(repository)
    }.start(wait = true)
}

private fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
        )
    }
}

private fun Application.configureRouting(repository: FilePortalRepository) {
    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        route("/api/v1") {
            get("/groups") {
                call.respond(repository.getGroups())
            }

            get("/news") {
                val important = call.request.queryParameters["important"]?.toBooleanStrictOrNull()
                val category = call.request.queryParameters["category"]?.trim().orEmpty()
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()

                var result = repository.getNews()

                if (important != null) {
                    result = result.filter { it.isImportant == important }
                }

                if (category.isNotBlank()) {
                    result = result.filter { news ->
                        news.categories.any { it.equals(category, ignoreCase = true) }
                    }
                }

                if (limit != null && limit > 0) {
                    result = result.take(limit)
                }

                call.respond(result)
            }

            get("/news/{id}") {
                val id = call.parameters["id"]
                if (id.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Параметр id обязателен"))
                    return@get
                }

                val news = repository.getNewsById(id)
                if (news == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Новость не найдена"))
                    return@get
                }

                call.respond(news)
            }

            get("/schedule") {
                val group = call.request.queryParameters["group"]?.trim().orEmpty()
                if (group.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Параметр group обязателен"))
                    return@get
                }

                val dayOfWeek = call.request.queryParameters["dayOfWeek"]?.toIntOrNull()
                val weekType = normalizeWeekType(call.request.queryParameters["weekType"])

                var lessons = repository.getLessons(group)

                if (dayOfWeek != null) {
                    lessons = lessons.filter { it.dayOfWeek == dayOfWeek }
                }

                if (weekType != null) {
                    lessons = lessons.filter {
                        normalizeWeekType(it.weekType) == weekType || it.weekType.equals("обе", ignoreCase = true)
                    }
                }

                call.respond(lessons)
            }
        }
    }
}

private fun normalizeWeekType(value: String?): String? {
    if (value.isNullOrBlank()) return null

    return value
        .trim()
        .lowercase()
        .replace(" неделя", "")
}
