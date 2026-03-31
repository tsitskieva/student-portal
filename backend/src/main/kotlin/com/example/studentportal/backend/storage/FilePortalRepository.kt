package com.example.studentportal.backend.storage

import com.example.studentportal.backend.model.GroupItem
import com.example.studentportal.backend.model.LessonItem
import com.example.studentportal.backend.model.NewsItem
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

    init {
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs()
        }

        copySeedIfMissing("news.json")
        copySeedIfMissing("groups.json")
        copySeedIfMissing("lessons.json")
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

    private fun readNews(): List<NewsItem> {
        return json.decodeFromString(
            ListSerializer(NewsItem.serializer()),
            file("news.json").readText(Charsets.UTF_8)
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
        val match = Regex("(\\d{1,2})\\s+([а-яё.]+)\\s+(\\d{4})").find(value.trim()) ?: return null
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
}
