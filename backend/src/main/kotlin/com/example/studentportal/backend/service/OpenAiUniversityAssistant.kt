package com.example.studentportal.backend.service

import com.example.studentportal.backend.model.OpenAiAssistantJson
import com.example.studentportal.backend.model.ReferenceArticleItem
import com.example.studentportal.backend.model.ReferenceAskRequest
import com.example.studentportal.backend.model.ReferenceAssistantResponse
import com.example.studentportal.backend.storage.FilePortalRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class OpenAiUniversityAssistant(
    private val repository: FilePortalRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    private val apiKey: String = System.getenv("OPENAI_API_KEY")?.trim().orEmpty()
    private val model: String = System.getenv("OPENAI_MODEL")?.trim().takeUnless { it.isNullOrBlank() } ?: "gpt-5.4"

    fun answer(request: ReferenceAskRequest): ReferenceAssistantResponse {
        val question = request.question.trim()
        val candidates = repository.retrieveReferenceCandidates(question, limit = 4)

        if (candidates.isEmpty()) {
            return ReferenceAssistantResponse(
                canAnswer = false,
                answer = "Я могу помочь только с вопросами по университетской информации. Попробуйте спросить про деканат, ИВТиПТ, стипендии, общежитие, приёмную комиссию или профсоюз.",
                sources = emptyList(),
                usedArticleIds = emptyList(),
                isAiGenerated = false,
                fallbackUsed = true
            )
        }

        val fallback = repository.buildLocalReferenceAnswer(question, candidates)

        if (apiKey.isBlank()) {
            return fallback
        }

        return try {
            requestOpenAi(question, request.history.map { it.role to it.text }, candidates)
        } catch (_: Exception) {
            fallback
        }
    }

    private fun requestOpenAi(
        question: String,
        history: List<Pair<String, String>>,
        candidates: List<ReferenceArticleItem>
    ): ReferenceAssistantResponse {
        val developerPrompt = """
            Ты — доменный AI-помощник по университетской справочной информации Южного федерального университета.
            Отвечай только на основе предоставленного контекста.
            Правила:
            1. Не выдумывай факты, которых нет в контексте.
            2. Если вопрос не связан с университетской справочной информацией или в контексте нет точного ответа, верни canAnswer=false.
            3. Если пользователь спрашивает только телефон, почту, адрес, сайт или контакты — отвечай только нужной частью без лишнего текста.
            4. Если вопрос про условия стипендии, общежития или социальной поддержки, сопоставляй условия из вопроса с условиями из контекста и объясняй вывод простым языком.
            5. Если для точного вывода не хватает данных, скажи об этом прямо.
            6. Отвечай по-русски, кратко и по делу.
            7. Верни только JSON без markdown и без пояснений.
        """.trimIndent()

        val historyText = history
            .takeLast(8)
            .joinToString("\n") { (role, text) ->
                val normalizedRole = if (role.lowercase() == "assistant") "ASSISTANT" else "USER"
                "$normalizedRole: $text"
            }

        val contextText = candidates.joinToString("\n\n") { article ->
            """
            [ARTICLE]
            id: ${article.id}
            title: ${article.title}
            category: ${article.category}
            source: ${article.sourceSection}
            summary: ${article.summary}
            content: ${article.content}
            """.trimIndent()
        }

        val userPrompt = buildString {
            appendLine("История диалога:")
            appendLine(if (historyText.isBlank()) "История пуста." else historyText)
            appendLine()
            appendLine("Текущий вопрос пользователя:")
            appendLine(question)
            appendLine()
            appendLine("Контекст из университетской базы знаний:")
            appendLine(contextText)
            appendLine()
            appendLine("""Верни JSON вида {"canAnswer":true,"answer":"...","sourceTitles":["..."]}""")
        }

        val requestPayload = buildJsonObject {
            put("model", JsonPrimitive(model))
            put(
                "messages",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("role", JsonPrimitive("developer"))
                            put("content", JsonPrimitive(developerPrompt))
                        }
                    )
                    add(
                        buildJsonObject {
                            put("role", JsonPrimitive("user"))
                            put("content", JsonPrimitive(userPrompt))
                        }
                    )
                }
            )
            put(
                "response_format",
                buildJsonObject {
                    put("type", JsonPrimitive("json_schema"))
                    put(
                        "json_schema",
                        buildJsonObject {
                            put("name", JsonPrimitive("university_reference_answer"))
                            put("strict", JsonPrimitive(true))
                            put(
                                "schema",
                                buildJsonObject {
                                    put("type", JsonPrimitive("object"))
                                    put(
                                        "properties",
                                        buildJsonObject {
                                            put("canAnswer", buildJsonObject { put("type", JsonPrimitive("boolean")) })
                                            put("answer", buildJsonObject { put("type", JsonPrimitive("string")) })
                                            put(
                                                "sourceTitles",
                                                buildJsonObject {
                                                    put("type", JsonPrimitive("array"))
                                                    put("items", buildJsonObject { put("type", JsonPrimitive("string")) })
                                                }
                                            )
                                        }
                                    )
                                    put(
                                        "required",
                                        buildJsonArray {
                                            add(JsonPrimitive("canAnswer"))
                                            add(JsonPrimitive("answer"))
                                            add(JsonPrimitive("sourceTitles"))
                                        }
                                    )
                                    put("additionalProperties", JsonPrimitive(false))
                                }
                            )
                        }
                    )
                }
            )
        }

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.openai.com/v1/chat/completions"))
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(requestPayload.toString()))
            .build()

        val httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())

        if (httpResponse.statusCode() !in 200..299) {
            throw IllegalStateException("OpenAI API error: ${httpResponse.statusCode()} ${httpResponse.body()}")
        }

        val responseJson = json.parseToJsonElement(httpResponse.body()).jsonObject
        val messageContent = responseJson["choices"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("message")
            ?.jsonObject
            ?.get("content")
            ?.jsonPrimitive
            ?.content
            ?: throw IllegalStateException("OpenAI response does not contain message content")

        val aiResult = json.decodeFromString(OpenAiAssistantJson.serializer(), messageContent)

        return ReferenceAssistantResponse(
            canAnswer = aiResult.canAnswer,
            answer = aiResult.answer.trim(),
            sources = aiResult.sourceTitles.ifEmpty { candidates.take(3).map { it.title } },
            usedArticleIds = candidates
                .filter { candidate -> aiResult.sourceTitles.contains(candidate.title) }
                .map { it.id }
                .ifEmpty { candidates.take(3).map { it.id } },
            isAiGenerated = true,
            fallbackUsed = false
        )
    }
}