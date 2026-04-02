package com.example.studentportal.backend.storage

import com.example.studentportal.backend.model.GroupItem
import com.example.studentportal.backend.model.LessonItem
import com.example.studentportal.backend.model.NewsItem
import com.example.studentportal.backend.model.ReferenceArticleItem
import com.example.studentportal.backend.model.ReferenceAssistantResponse
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.time.LocalDate

class FilePortalRepository(
    private val dataDirectory: File = File("data")
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val stopWords = setOf(
        "как", "что", "где", "куда", "когда", "кто",
        "какой", "какая", "какие", "какое", "какую",
        "есть", "ли", "про", "по", "в", "во", "на", "о", "об",
        "и", "или", "для", "у", "с", "со", "к", "ко", "от", "до",
        "из", "за", "а", "но", "это", "мне", "нужно", "можно",
        "могу", "будет", "если", "при", "я", "мы", "вы", "он", "она",
        "они", "моя", "мой", "мою", "наш", "ваш", "там", "тут",
        "курс", "курсе", "курса", "семестр", "семестра"
    )

    private val synonymMap = mapOf(
        "профком" to listOf("профсоюз"),
        "общага" to listOf("общежитие"),
        "общежитие" to listOf("кампус"),
        "степуха" to listOf("стипендия"),
        "стипуха" to listOf("стипендия"),
        "деканат" to listOf("деканат"),
        "приемка" to listOf("приемная", "комиссия"),
        "поступление" to listOf("приемная", "комиссия"),
        "контакты" to listOf("телефон", "почта", "email"),
        "связаться" to listOf("контакты", "телефон", "почта"),
        "заселение" to listOf("общежитие"),
        "общагу" to listOf("общежитие"),
        "ивтипт" to listOf("институт", "пьезотехники")
    )

    init {
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs()
        }

        copySeedIfMissing("news.json")
        copySeedIfMissing("groups.json")
        copySeedIfMissing("lessons.json")
        copySeedIfMissing("reference_articles.json")
    }

    fun getNews(): List<NewsItem> {
        return readNews().sortedByDescending { parseRuDate(it.date) ?: LocalDate.MIN }
    }

    fun getNewsById(id: String): NewsItem? {
        return readNews().firstOrNull { it.id == id }
    }

    fun getGroups(): List<GroupItem> {
        return json.decodeFromString(
            ListSerializer(GroupItem.serializer()),
            file("groups.json").readText(Charsets.UTF_8)
        ).sortedBy { it.group }
    }

    fun getLessons(group: String): List<LessonItem> {
        return json.decodeFromString(
            ListSerializer(LessonItem.serializer()),
            file("lessons.json").readText(Charsets.UTF_8)
        )
            .filter { it.group == group }
            .sortedBy { lessonOrder(it.number) }
    }

    fun getReferenceTopics(): List<String> {
        return readReferenceArticles()
            .map { it.category }
            .distinct()
            .sorted()
    }

    fun retrieveReferenceCandidates(query: String, limit: Int = 4): List<ReferenceArticleItem> {
        val normalizedQuery = expandQuery(normalizeText(query))
        val queryTokens = tokenize(normalizedQuery)

        if (queryTokens.isEmpty()) return emptyList()

        data class RankedArticle(
            val article: ReferenceArticleItem,
            val score: Int,
            val tokenOverlap: Int,
            val phraseHits: Int,
            val directHit: Boolean
        )

        val ranked = readReferenceArticles()
            .map { article ->
                val normalizedCategory = normalizeText(article.category)
                val normalizedTitle = normalizeText(article.title)

                val keywordHits = article.keywords.count { keyword ->
                    softContains(normalizedQuery, normalizeText(keyword))
                }

                val exampleHits = article.exampleQuestions.count { example ->
                    val normalizedExample = normalizeText(example)
                    softContains(normalizedQuery, normalizedExample) ||
                        softContains(normalizedExample, normalizedQuery)
                }

                val categoryHit = if (softContains(normalizedQuery, normalizedCategory)) 1 else 0
                val titleHit = if (softContains(normalizedQuery, normalizedTitle)) 1 else 0

                val articleTokens = tokenize(
                    buildString {
                        append(article.category)
                        append(' ')
                        append(article.title)
                        append(' ')
                        append(article.keywords.joinToString(" "))
                        append(' ')
                        append(article.exampleQuestions.joinToString(" "))
                        append(' ')
                        append(article.summary)
                        append(' ')
                        append(article.content)
                    }
                )

                val tokenOverlap = queryTokens.intersect(articleTokens).size
                val phraseHits = keywordHits + exampleHits
                val directHit = categoryHit > 0 || titleHit > 0 || exampleHits > 0

                val score =
                    categoryHit * 60 +
                        titleHit * 45 +
                        keywordHits * 28 +
                        exampleHits * 36 +
                        tokenOverlap * 10

                RankedArticle(
                    article = article,
                    score = score,
                    tokenOverlap = tokenOverlap,
                    phraseHits = phraseHits,
                    directHit = directHit
                )
            }
            .sortedByDescending { it.score }

        val filtered = ranked.filter { item ->
            item.score >= 24 ||
                item.directHit ||
                item.tokenOverlap >= 2 ||
                item.phraseHits >= 2
        }

        return filtered.take(limit).map { it.article }
    }

    fun buildLocalReferenceAnswer(query: String, candidates: List<ReferenceArticleItem>): ReferenceAssistantResponse {
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

        val top = candidates.first()
        val focusedAnswer = buildFocusedAnswer(query, top)

        return ReferenceAssistantResponse(
            canAnswer = true,
            answer = focusedAnswer,
            sources = candidates.take(3).map { it.title },
            usedArticleIds = candidates.take(3).map { it.id },
            isAiGenerated = false,
            fallbackUsed = true
        )
    }

    private fun buildFocusedAnswer(query: String, article: ReferenceArticleItem): String {
        val normalizedQuery = normalizeText(query)
        val sourceText = listOf(article.summary, article.content).joinToString(" ")

        val phones = extractPhones(sourceText)
        val emails = extractEmails(sourceText)
        val websites = extractWebsites(sourceText)
        val address = extractAddress(sourceText)

        return when {
            containsAnyPhrase(normalizedQuery, listOf("телефон", "номер", "позвонить", "как позвонить")) &&
                phones.isNotEmpty() -> {
                "Телефон: ${phones.first()}."
            }

            containsAnyPhrase(normalizedQuery, listOf("почта", "email", "e mail", "электронная почта")) &&
                emails.isNotEmpty() -> {
                "Электронная почта: ${emails.first()}."
            }

            containsAnyPhrase(normalizedQuery, listOf("сайт", "ссылка")) &&
                websites.isNotEmpty() -> {
                "Сайт: ${websites.first()}."
            }

            containsAnyPhrase(normalizedQuery, listOf("адрес", "где находится", "куда идти", "где расположен")) &&
                !address.isNullOrBlank() -> {
                "Адрес: $address."
            }

            containsAnyPhrase(normalizedQuery, listOf("как связаться", "контакты")) -> {
                when {
                    phones.isNotEmpty() -> "Телефон: ${phones.first()}."
                    emails.isNotEmpty() -> "Электронная почта: ${emails.first()}."
                    websites.isNotEmpty() -> "Сайт: ${websites.first()}."
                    !address.isNullOrBlank() -> "Адрес: $address."
                    else -> article.summary
                }
            }

            else -> article.summary
        }
    }

    private fun readNews(): List<NewsItem> {
        return json.decodeFromString(
            ListSerializer(NewsItem.serializer()),
            file("news.json").readText(Charsets.UTF_8)
        )
    }

    private fun readReferenceArticles(): List<ReferenceArticleItem> {
        return json.decodeFromString(
            ListSerializer(ReferenceArticleItem.serializer()),
            file("reference_articles.json").readText(Charsets.UTF_8)
        )
    }

    private fun file(name: String): File = File(dataDirectory, name)

    private fun copySeedIfMissing(fileName: String) {
        val target = file(fileName)
        if (target.exists()) return

        val resourcePath = "seed/$fileName"
        val stream: InputStream = javaClass.classLoader.getResourceAsStream(resourcePath)
            ?: error("Seed file not found in resources: $resourcePath")

        stream.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun lessonOrder(number: String): Int {
        return when (number.trim()) {
            "1-я пара" -> 1
            "2-я пара" -> 2
            "3-я пара" -> 3
            "4-я пара" -> 4
            "5-я пара" -> 5
            else -> Int.MAX_VALUE
        }
    }

    private fun parseRuDate(value: String): LocalDate? {
        val match = Regex("""(\d{1,2})\s+([а-яё.]+)\s+(\d{4})""").find(value.trim()) ?: return null
        val day = match.groupValues[1].toIntOrNull() ?: return null
        val month = when (match.groupValues[2].lowercase()) {
            "янв.", "января" -> 1
            "февр.", "фев.", "февраля" -> 2
            "мар.", "марта" -> 3
            "апр.", "апреля" -> 4
            "мая" -> 5
            "июн.", "июня" -> 6
            "июл.", "июля" -> 7
            "авг.", "августа" -> 8
            "сент.", "сентября" -> 9
            "окт.", "октября" -> 10
            "нояб.", "ноября" -> 11
            "дек.", "декабря" -> 12
            else -> return null
        }
        val year = match.groupValues[3].toIntOrNull() ?: return null
        return LocalDate.of(year, month, day)
    }

    private fun normalizeText(value: String): String {
        return value
            .lowercase()
            .replace('ё', 'е')
            .replace(Regex("""[^\p{L}\p{Nd}\s]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun expandQuery(value: String): String {
        val normalized = normalizeText(value)
        if (normalized.isBlank()) return normalized

        val parts = normalized.split(' ').filter { it.isNotBlank() }.toMutableList()
        val additions = mutableListOf<String>()

        parts.forEach { token ->
            synonymMap[token]?.let { additions.addAll(it) }
        }

        return (parts + additions).joinToString(" ")
    }

    private fun tokenize(value: String): Set<String> {
        return normalizeText(value)
            .split(' ')
            .filter { it.length >= 2 }
            .filter { it !in stopWords }
            .toSet()
    }

    private fun softContains(text: String, candidate: String): Boolean {
        if (text.isBlank() || candidate.isBlank()) return false
        if (text.contains(candidate)) return true

        val textTokens = tokenize(text)
        val candidateTokens = tokenize(candidate)

        if (textTokens.isEmpty() || candidateTokens.isEmpty()) return false

        return candidateTokens.all { candidateToken ->
            textTokens.any { textToken ->
                wordsRoughlyMatch(textToken, candidateToken)
            }
        }
    }

    private fun wordsRoughlyMatch(first: String, second: String): Boolean {
        if (first == second) return true
        if (first.length < 4 || second.length < 4) return false
        return commonPrefixLength(first, second) >= 5
    }

    private fun commonPrefixLength(first: String, second: String): Int {
        val limit = minOf(first.length, second.length)
        var count = 0
        while (count < limit && first[count] == second[count]) {
            count++
        }
        return count
    }

    private fun containsAnyPhrase(text: String, phrases: List<String>): Boolean {
        return phrases.any { phrase ->
            text.contains(normalizeText(phrase))
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
                """(?:по адресу|адрес)\s*[:\-]?\s*(.+?)(?=телефон|e-mail|электронная почта|официальный сайт|сайт:|$)""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            ),
            Regex(
                """(?:находится по адресу|расположен по адресу)\s*(.+?)(?=телефон|e-mail|электронная почта|официальный сайт|сайт:|$)""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            )
        )

        for (pattern in patterns) {
            val match = pattern.find(text) ?: continue
            val value = match.groupValues[1]
                .replace(Regex("""\s+"""), " ")
                .trim()
                .trimEnd('.', ',', ';', ':')

            if (value.isNotBlank()) {
                return value
            }
        }

        return null
    }
}