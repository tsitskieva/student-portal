package com.example.studentportal.backend.service

import com.example.studentportal.backend.model.OllamaAssistantJson
import com.example.studentportal.backend.model.ReferenceArticleItem
import com.example.studentportal.backend.model.ReferenceAskRequest
import com.example.studentportal.backend.model.ReferenceAssistantResponse
import com.example.studentportal.backend.model.ReferenceAssistantStatus
import com.example.studentportal.backend.storage.FilePortalRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration


class OllamaUniversityAssistant(
    private val repository: FilePortalRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    private val baseUrl: String = (System.getenv("OLLAMA_BASE_URL") ?: "http://localhost:11434").trim().removeSuffix("/")
    private val model: String = (System.getenv("OLLAMA_MODEL") ?: "qwen2.5:14b").trim()

    fun answer(request: ReferenceAskRequest): ReferenceAssistantResponse {
        val originalQuestion = request.question.trim()
        val effectiveQuestion = resolveFollowUpQuestion(originalQuestion, request.history)

        val candidates = repository.retrieveReferenceCandidates(effectiveQuestion, limit = 5)

        if (candidates.isEmpty()) {
            return buildRefusalResponse()
        }

        if (shouldForceRefusal(originalQuestion, effectiveQuestion, candidates)) {
            return buildRefusalResponse()
        }

        val fastAnswer = buildFastDeterministicAnswer(
            question = effectiveQuestion,
            historySize = request.history.size,
            candidates = candidates
        )
        if (fastAnswer != null) {
            return fastAnswer
        }

        val llmCandidates = candidates.take(3)
        val fallback = repository.buildLocalReferenceAnswer(effectiveQuestion, llmCandidates)

        return try {
            val aiResponse = requestOllama(
                originalQuestion = originalQuestion,
                effectiveQuestion = effectiveQuestion,
                history = request.history.map { it.role to it.text },
                candidates = llmCandidates
            )

            postProcessAnswer(
                question = effectiveQuestion,
                aiResponse = aiResponse,
                candidates = llmCandidates,
                fallback = fallback
            )
        } catch (e: Exception) {
            e.printStackTrace()
            fallback
        }
    }

    fun status(): ReferenceAssistantStatus {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/api/tags"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() !in 200..299) {
                return ReferenceAssistantStatus(
                    provider = "ollama",
                    baseUrl = baseUrl,
                    model = model,
                    ollamaReachable = false,
                    modelAvailable = false,
                    canUseLocalLlm = false,
                    message = "Ollama недоступен: HTTP ${response.statusCode()}"
                )
            }

            val root = json.parseToJsonElement(response.body()).jsonObject
            val models = root["models"] as? JsonArray ?: JsonArray(emptyList())
            val modelAvailable = models.any { element ->
                val name = element.jsonObject["model"]?.toString()?.trim('"') ?: ""
                name.equals(model, ignoreCase = true)
            }

            ReferenceAssistantStatus(
                provider = "ollama",
                baseUrl = baseUrl,
                model = model,
                ollamaReachable = true,
                modelAvailable = modelAvailable,
                canUseLocalLlm = modelAvailable,
                message = if (modelAvailable) {
                    "Ollama доступен, модель $model найдена."
                } else {
                    "Ollama доступен, но модель $model не найдена. Выполните: ollama pull $model"
                }
            )
        } catch (e: Exception) {
            ReferenceAssistantStatus(
                provider = "ollama",
                baseUrl = baseUrl,
                model = model,
                ollamaReachable = false,
                modelAvailable = false,
                canUseLocalLlm = false,
                message = "Не удалось подключиться к Ollama: ${e.message ?: "unknown error"}"
            )
        }
    }

    private fun isBroadTopicQuery(question: String): Boolean {
        val normalized = question.trim().lowercase()
        return normalized in setOf(
            "деканат",
            "ивтипт",
            "институт",
            "общежитие",
            "приемная комиссия",
            "приёмная комиссия",
            "профсоюз",
            "социальная поддержка",
            "стипендия",
            "стипендии",
            "университет"
        ) ||
                normalized.startsWith("расскажи кратко и по делу про ") ||
                normalized.startsWith("расскажи кратко и по делу о ") ||
                normalized.startsWith("расскажи кратко и по делу об ")
    }

    private fun requestOllama(
        originalQuestion: String,
        effectiveQuestion: String,
        history: List<Pair<String, String>>,
        candidates: List<ReferenceArticleItem>
    ): ReferenceAssistantResponse {
        val systemPrompt = """
            Ты — доменный AI-помощник по университетской справочной информации Южного федерального университета.
        
            Отвечай только на основе предоставленного контекста.
            Не выдумывай факты, которых нет в контексте.
        
            Правила:
            1. Если вопрос не относится к университетской справочной информации или в контексте нет достаточных данных, верни canAnswer=false.
            2. Если canAnswer=false, обязательно заполни answer понятным текстом на русском языке. Никогда не оставляй answer пустым.
            3. Отвечай по-русски, естественно, дружелюбно, кратко и по делу.
            4. Не отвечай телеграфным стилем. Не пиши просто набор строк вроде «телефон ... адрес ...».
            5. Если вопрос про телефон, адрес, почту, сайт, режим работы или контакты, отвечай коротко и естественным языком.
            6. Если вопрос общий, дай небольшую полезную сводку по теме в 2–4 предложениях.
            7. Если вопрос про условия стипендии, общежития или социальной поддержки, сопоставляй условия из вопроса с условиями из контекста и объясняй вывод простым языком.
            8. На абсурдные, мусорные или нелогичные запросы не отвечай по существу. В таком случае верни canAnswer=false и короткое объяснение, что вопрос сформулирован некорректно или не относится к справочной информации вуза.
            9. Если вопрос простой, отвечай в 1–3 предложениях.
            10. Не используй markdown, списки и лишние вводные фразы.
            11. Верни только JSON без пояснений.
        
            Формат JSON:
            {
              "canAnswer": true,
              "answer": "строка",
              "sourceTitles": ["строка"]
            }
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

        val broadTopicMode = isBroadTopicQuery(effectiveQuestion)

        val userPrompt = buildString {
            appendLine("История диалога:")
            appendLine(if (historyText.isBlank()) "История пуста." else historyText)
            appendLine()

            appendLine("Исходный вопрос пользователя:")
            appendLine(originalQuestion)
            appendLine()

            if (effectiveQuestion != originalQuestion) {
                appendLine("Уточнённая интерпретация вопроса с учётом контекста диалога:")
                appendLine(effectiveQuestion)
                appendLine()
            }

            if (broadTopicMode) {
                appendLine("Это общий запрос по теме. Нужно дать краткую полезную справку по теме.")
                appendLine()
            }

            appendLine("Контекст из университетской базы знаний:")
            appendLine(contextText)
            appendLine()
            appendLine("Верни JSON вида {\"canAnswer\":true,\"answer\":\"...\",\"sourceTitles\":[\"...\"]}")
        }

        val schema = buildJsonObject {
            put("type", JsonPrimitive("object"))
            put("properties", buildJsonObject {
                put("canAnswer", buildJsonObject { put("type", JsonPrimitive("boolean")) })
                put("answer", buildJsonObject { put("type", JsonPrimitive("string")) })
                put("sourceTitles", buildJsonObject {
                    put("type", JsonPrimitive("array"))
                    put("items", buildJsonObject { put("type", JsonPrimitive("string")) })
                })
            })
            put("required", buildJsonArray {
                add(JsonPrimitive("canAnswer"))
                add(JsonPrimitive("answer"))
                add(JsonPrimitive("sourceTitles"))
            })
            put("additionalProperties", JsonPrimitive(false))
        }

        val payload = buildJsonObject {
            put("model", JsonPrimitive(model))
            put("stream", JsonPrimitive(false))
            put("format", schema)
            put("options", buildJsonObject {
                put("temperature", JsonPrimitive(0.1))
                put("num_predict", JsonPrimitive(140))
            })
            put("messages", buildJsonArray {
                add(message("system", systemPrompt))
                add(message("user", userPrompt))
            })
        }

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/api/chat"))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(90))
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build()

        val httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
        if (httpResponse.statusCode() !in 200..299) {
            throw IllegalStateException("Ollama API error: ${httpResponse.statusCode()} ${httpResponse.body()}")
        }

        val responseJson = json.parseToJsonElement(httpResponse.body()).jsonObject

        val messageElement = responseJson["message"]
            ?: throw IllegalStateException("Ollama response does not contain message")

        val contentElement = messageElement.jsonObject["content"]
            ?: throw IllegalStateException("Ollama response does not contain assistant content")

        val messageContent = contentElement.jsonPrimitive.content.trim()

        val aiResult = json.decodeFromString(OllamaAssistantJson.serializer(), messageContent)

        val usedArticles = candidates.filter { candidate -> aiResult.sourceTitles.contains(candidate.title) }

        return ReferenceAssistantResponse(
            canAnswer = aiResult.canAnswer,
            answer = aiResult.answer.trim(),
            sources = if (aiResult.canAnswer) {
                aiResult.sourceTitles.ifEmpty { candidates.take(3).map { it.title } }
            } else {
                aiResult.sourceTitles.ifEmpty { emptyList() }
            },
            usedArticleIds = if (aiResult.canAnswer) {
                usedArticles.map { it.id }.ifEmpty { candidates.take(3).map { it.id } }
            } else {
                usedArticles.map { it.id }
            },
            isAiGenerated = true,
            fallbackUsed = false
        )
    }

    private fun message(role: String, content: String): JsonObject {
        return buildJsonObject {
            put("role", JsonPrimitive(role))
            put("content", JsonPrimitive(content))
        }
    }

    private fun postProcessAnswer(
        question: String,
        aiResponse: ReferenceAssistantResponse,
        candidates: List<ReferenceArticleItem>,
        fallback: ReferenceAssistantResponse
    ): ReferenceAssistantResponse {
        if (!aiResponse.canAnswer) {
            return aiResponse.copy(
                answer = aiResponse.answer.trim().ifBlank {
                    "Я не могу корректно ответить на этот вопрос, потому что он не относится к университетской справочной информации или сформулирован некорректно."
                },
                sources = emptyList(),
                usedArticleIds = emptyList()
            )
        }

        buildNaturalDirectAnswer(question, candidates)?.let { directAnswer ->
            return aiResponse.copy(answer = directAnswer)
        }

        val polished = polishAssistantAnswer(aiResponse.answer)
        return if (polished.isBlank()) {
            fallback
        } else {
            aiResponse.copy(answer = polished)
        }
    }

    private fun buildNaturalDirectAnswer(
        question: String,
        candidates: List<ReferenceArticleItem>
    ): String? {
        if (candidates.isEmpty()) return null
        if (isBroadTopicQuery(question)) return null

        val normalizedQuestion = normalizeForMatch(question)
        val article = candidates.first()
        val text = candidates.joinToString("\n") { "${it.title}. ${it.summary}. ${it.content}" }

        val phone = extractPhones(text).firstOrNull()
        val address = extractAddress(text)
        val email = extractEmails(text).firstOrNull()
        val website = extractWebsites(text).firstOrNull()
        val hours = extractHours(text)

        return when {
            containsAnyPhrase(normalizedQuestion, listOf("как связаться", "контакты")) -> {
                buildContactAnswer(article, phone, address, email, website, hours)
            }

            containsAnyPhrase(normalizedQuestion, listOf("телефон", "номер", "позвонить", "как позвонить")) && phone != null -> {
                "${contactLead(article)} можно позвонить по телефону $phone."
            }

            containsAnyPhrase(normalizedQuestion, listOf("адрес", "где находится", "где он находится", "где она находится", "куда идти", "где расположен")) && !address.isNullOrBlank() -> {
                "${subjectName(article)} находится по адресу: $address."
            }

            containsAnyPhrase(normalizedQuestion, listOf("режим работы", "часы работы", "когда работает", "во сколько работает")) && !hours.isNullOrBlank() -> {
                "${subjectName(article)} работает $hours."
            }

            containsAnyPhrase(normalizedQuestion, listOf("почта", "email", "e-mail", "электронная почта")) && !email.isNullOrBlank() -> {
                "Электронная почта ${subjectGenitive(article)}: $email."
            }

            containsAnyPhrase(normalizedQuestion, listOf("сайт")) && !website.isNullOrBlank() -> {
                "Официальный сайт ${subjectGenitive(article)}: $website."
            }

            else -> null
        }
    }

    private fun buildContactAnswer(
        article: ReferenceArticleItem,
        phone: String?,
        address: String?,
        email: String?,
        website: String?,
        hours: String?
    ): String? {
        val sentences = mutableListOf<String>()

        if (!phone.isNullOrBlank()) {
            sentences += "${contactLead(article)} можно связаться по телефону $phone"
        }
        if (!address.isNullOrBlank()) {
            sentences += "Адрес: $address"
        }
        if (!email.isNullOrBlank()) {
            sentences += "Электронная почта: $email"
        }
        if (!hours.isNullOrBlank()) {
            sentences += "Режим работы: $hours"
        }
        if (!website.isNullOrBlank()) {
            sentences += "Сайт: $website"
        }

        return if (sentences.isEmpty()) null else sentences.joinToString(". ", postfix = ".")
    }

    private fun contactLead(article: ReferenceArticleItem): String {
        val key = "${article.category} ${article.title}".lowercase()
        return when {
            "деканат" in key -> "С деканатом ИВТиПТ"
            "профсоюз" in key -> "С профсоюзом ЮФУ"
            "приемн" in key || "приёмн" in key -> "С приёмной комиссией ЮФУ"
            "ивтипт" in key -> "С ИВТиПТ"
            else -> "По этому вопросу"
        }
    }

    private fun subjectName(article: ReferenceArticleItem): String {
        val key = "${article.category} ${article.title}".lowercase()
        return when {
            "деканат" in key -> "Деканат ИВТиПТ"
            "профсоюз" in key -> "Профсоюз ЮФУ"
            "приемн" in key || "приёмн" in key -> "Приёмная комиссия ЮФУ"
            "ивтипт" in key -> "ИВТиПТ"
            "общежит" in key -> "Информация об общежитиях"
            else -> article.title
        }
    }

    private fun subjectGenitive(article: ReferenceArticleItem): String {
        val key = "${article.category} ${article.title}".lowercase()
        return when {
            "деканат" in key -> "деканата ИВТиПТ"
            "профсоюз" in key -> "профсоюза ЮФУ"
            "приемн" in key || "приёмн" in key -> "приёмной комиссии ЮФУ"
            "ивтипт" in key -> "ИВТиПТ"
            "общежит" in key -> "раздела об общежитиях"
            else -> article.title
        }
    }

    private fun polishAssistantAnswer(value: String): String {
        return value
            .replace(Regex("""\s+"""), " ")
            .replace(" ,", ",")
            .replace(" .", ".")
            .trim()
            .replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
    }

    private fun normalizeForMatch(value: String): String {
        return value.lowercase().replace('ё', 'е')
    }

    private fun containsAnyPhrase(text: String, phrases: List<String>): Boolean {
        return phrases.any { phrase ->
            text.contains(normalizeForMatch(phrase))
        }
    }

    private fun extractPhones(text: String): List<String> {
        return Regex("""(?:\+7|8)[\d\s\-()]{9,}\d""")
            .findAll(text)
            .map { match ->
                match.value
                    .replace(Regex("""\s+"""), " ")
                    .trim()
                    .trimEnd('.', ',', ';', ':')
            }
            .distinct()
            .toList()
    }

    private fun extractEmails(text: String): List<String> {
        return Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
            .findAll(text)
            .map { it.value.trim().trimEnd('.', ',', ';', ':') }
            .distinct()
            .toList()
    }

    private fun extractWebsites(text: String): List<String> {
        return Regex("""(?<!@)\b(?:[a-z0-9-]+\.)+[a-z]{2,}\b""", RegexOption.IGNORE_CASE)
            .findAll(text)
            .map { it.value.trim().trimEnd('.', ',', ';', ':') }
            .filterNot { it.contains("@") }
            .distinct()
            .toList()
    }

    private fun extractAddress(text: String): String? {
        val patterns = listOf(
            Regex(
                """(?:по адресу|адрес)\s*[:\-]?\s*(.+?)(?=телефон|e-mail|email|электронная почта|официальный сайт|сайт:|режим работы|$)""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            ),
            Regex(
                """(?:находится по адресу|расположен по адресу)\s*(.+?)(?=телефон|e-mail|email|электронная почта|официальный сайт|сайт:|режим работы|$)""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            )
        )

        for (pattern in patterns) {
            val match = pattern.find(text) ?: continue
            val value = match.groupValues[1]
                .replace(Regex("""\s+"""), " ")
                .trim()
                .trimEnd('.', ',', ';', ':')

            if (value.isNotBlank()) return value
        }

        return null
    }

    private fun extractHours(text: String): String? {
        val patterns = listOf(
            Regex("""с\s*\d{1,2}[:.]\d{2}\s*до\s*\d{1,2}[:.]\d{2}""", RegexOption.IGNORE_CASE),
            Regex("""\d{1,2}[:.]\d{2}\s*[-–]\s*\d{1,2}[:.]\d{2}""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text) ?: continue
            return match.value.replace('.', ':').trim()
        }

        return null
    }

    private fun buildFastDeterministicAnswer(
        question: String,
        historySize: Int,
        candidates: List<ReferenceArticleItem>
    ): ReferenceAssistantResponse? {
        if (candidates.isEmpty()) return null

        val normalizedQuestion = normalizeForMatch(question)

        val hasPronounFollowUp = containsAnyPhrase(
            normalizedQuestion,
            listOf("а где", "а как", "а когда", "а какой", "а какая", "а какие", "а кто", "а куда", "а почему")
        )

        if (historySize > 0 && hasPronounFollowUp) {
            return null
        }

        if (isComplexReasoningQuestion(normalizedQuestion)) {
            return null
        }

        val article = candidates.first()
        val singleArticle = listOf(article)

        val answer = when {
            isBroadTopicQuery(question) -> buildBroadTopicAnswer(article)
            else -> buildNaturalDirectAnswer(question, singleArticle)
        } ?: return null

        return ReferenceAssistantResponse(
            canAnswer = true,
            answer = answer,
            sources = listOf(article.title),
            usedArticleIds = listOf(article.id),
            isAiGenerated = false,
            fallbackUsed = true
        )
    }

    private fun isComplexReasoningQuestion(question: String): Boolean {
        return containsAnyPhrase(
            question,
            listOf(
                "если", "могу ли", "могу рассчитывать", "какую стипендию",
                "на что могу рассчитывать", "какие условия", "кто может",
                "при каких условиях", "если закрою", "бюджет", "бакалавриат",
                "магистратура", "курс", "без троек", "отлично", "хорошо"
            )
        )
    }

    private fun buildBroadTopicAnswer(article: ReferenceArticleItem): String? {
        val text = "${article.title}. ${article.summary}. ${article.content}"

        val phone = extractPhones(text).firstOrNull()
        val address = extractAddress(text)
        val email = extractEmails(text).firstOrNull()
        val website = extractWebsites(text).firstOrNull()
        val hours = extractHours(text)

        val key = "${article.category} ${article.title}".lowercase()

        return when {
            "деканат" in key -> {
                buildString {
                    append("Деканат ИВТиПТ находится по адресу: ${address ?: "адрес не указан"}.")
                    if (!phone.isNullOrBlank()) append(" По вопросам студентов можно обращаться по телефону $phone.")
                    if (!email.isNullOrBlank()) append(" Электронная почта: $email.")
                    append(" Деканат помогает по учебным и организационным вопросам.")
                }.trim()
            }

            "профсоюз" in key -> {
                buildString {
                    append("Профсоюз ЮФУ защищает интересы студентов и помогает по вопросам социальной поддержки.")
                    if (!phone.isNullOrBlank()) append(" Связаться можно по телефону $phone.")
                    if (!hours.isNullOrBlank()) append(" Режим работы: $hours.")
                    if (!website.isNullOrBlank()) append(" Сайт: $website.")
                }.trim()
            }

            "приемн" in key || "приёмн" in key -> {
                buildString {
                    append("Приёмная комиссия ЮФУ помогает по вопросам поступления и подачи документов.")
                    if (!address.isNullOrBlank()) append(" Она находится по адресу: $address.")
                    if (!phone.isNullOrBlank()) append(" Телефон: $phone.")
                    if (!email.isNullOrBlank()) append(" Электронная почта: $email.")
                }.trim()
            }

            "стипенд" in key -> {
                "ЮФУ предоставляет несколько видов стипендий: государственную академическую, социальную и повышенную государственную академическую. Размер и условия зависят от успеваемости и категории студента. Для точного ответа лучше уточнить вашу ситуацию."
            }

            "общежит" in key -> {
                buildString {
                    append("ЮФУ предоставляет общежития студентам в Ростове-на-Дону и Таганроге.")
                    if (!address.isNullOrBlank()) append(" В справочной информации указаны адреса, в том числе: $address.")
                    append(" Если нужно, можно уточнить условия заселения или приоритет предоставления мест.")
                }.trim()
            }

            "ивтипт" in key || "институт" in key -> {
                buildString {
                    append("ИВТиПТ — это институт в структуре ЮФУ.")
                    if (!address.isNullOrBlank()) append(" Он находится по адресу: $address.")
                    if (!phone.isNullOrBlank()) append(" Связаться можно по телефону $phone.")
                    if (!email.isNullOrBlank()) append(" Электронная почта: $email.")
                }.trim()
            }

            else -> article.summary.ifBlank { article.content.take(250) }
        }
    }

    private fun buildRefusalResponse(): ReferenceAssistantResponse {
        return ReferenceAssistantResponse(
            canAnswer = false,
            answer = "Я не могу корректно ответить на этот вопрос, потому что он не относится к университетской справочной информации или сформулирован некорректно.",
            sources = emptyList(),
            usedArticleIds = emptyList(),
            isAiGenerated = false,
            fallbackUsed = true
        )
    }

    private fun resolveFollowUpQuestion(
        question: String,
        history: List<com.example.studentportal.backend.model.ReferenceChatMessage>
    ): String {
        val normalizedQuestion = normalizeForMatch(question)

        val looksLikeFollowUp = containsAnyPhrase(
            normalizedQuestion,
            listOf(
                "а где", "а как", "а какой", "а какая", "а какие",
                "а куда", "а когда", "а что", "а кто",
                "где он", "где она", "где оно", "где они",
                "как с ним", "как с ней", "как туда", "как туда обратиться",
                "где находится", "где она находится", "где он находится"
            )
        )

        if (!looksLikeFollowUp) return question

        val lastUserQuestion = history
            .asReversed()
            .firstOrNull { it.role.equals("user", ignoreCase = true) && it.text.isNotBlank() }
            ?.text
            ?: return question

        val subject = detectSubjectFromText(lastUserQuestion) ?: return question

        return when {
            containsAnyPhrase(normalizedQuestion, listOf("где", "находится", "адрес", "куда идти")) ->
                "Где находится $subject?"
            containsAnyPhrase(normalizedQuestion, listOf("как связаться", "контакты", "телефон", "номер", "почта")) ->
                "Как связаться с $subject?"
            containsAnyPhrase(normalizedQuestion, listOf("режим работы", "часы работы", "когда работает")) ->
                "Какой режим работы у $subject?"
            else -> "$question (речь идёт про $subject)"
        }
    }

    private fun detectSubjectFromText(text: String): String? {
        val normalized = normalizeForMatch(text)

        return when {
            "деканат" in normalized -> "деканат ИВТиПТ"
            "профсоюз" in normalized -> "профсоюз ЮФУ"
            "приемн" in normalized || "приёмн" in normalized -> "приёмную комиссию ЮФУ"
            "ивтипт" in normalized -> "ИВТиПТ"
            "общежит" in normalized -> "общежитие"
            "социальн" in normalized && "поддерж" in normalized -> "социальную поддержку студентов"
            "стипенд" in normalized -> "стипендию"
            "университет" in normalized || "юфу" in normalized -> "ЮФУ"
            else -> null
        }
    }

    private fun shouldForceRefusal(
        originalQuestion: String,
        effectiveQuestion: String,
        candidates: List<ReferenceArticleItem>
    ): Boolean {
        val normalizedOriginal = normalizeForMatch(originalQuestion)
        val normalizedEffective = normalizeForMatch(effectiveQuestion)

        val combinedContext = normalizeForMatch(
            candidates.joinToString(" ") {
                "${it.category} ${it.title} ${it.summary} ${it.content}"
            }
        )

        val unsupportedConcepts = listOf(
            "бассейн",
            "лифт",
            "этаж",
            "маршрут",
            "автобус",
            "парковка",
            "столовая",
            "спортзал"
        )

        val hasUnsupportedConcept = unsupportedConcepts.any { token ->
            normalizedOriginal.contains(token) && !combinedContext.contains(token)
        }

        val asksSpecificUnknownAction = containsAnyPhrase(
            normalizedEffective,
            listOf(
                "где записаться",
                "как записаться",
                "на каком этаже",
                "где находится бассейн",
                "как попасть",
                "куда идти"
            )
        )

        return hasUnsupportedConcept && asksSpecificUnknownAction
    }
}
