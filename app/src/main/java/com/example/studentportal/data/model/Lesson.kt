import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class Lesson(
    val id: String,
    val type: String,
    val title: String,
    val number: String,
    val time: String,
    val audience: String,
    val teacher: String,
    val typeOfTest: String,
    val building: String,
    val address: String,
    val dayOfWeek: Int,
    val weekType: String,
    val subgroup: String,
    val group: String,
    val isEmptyLesson: Boolean = false
) {
    val formattedTitle: String
        get() = when (subgroup) {
            "1" -> "$title (1 подгруппа)"
            "2" -> "$title (2 подгруппа)"
            else -> title
        }

    // Получаем время начала пары в формате LocalTime
    fun getStartTime(): LocalTime {
        val startTimeStr = time.split("-")[0].trim() // "8:00-9:35" → "8:00"
        return LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("H:mm"))
    }



    fun getNotificationTime(hoursBefore: Int, minutesBefore: Int): LocalDateTime {
        val startTimeStr = time.split("-")[0].trim()
        val startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("H:mm"))

        return LocalDateTime.now()
            .with(startTime)
            .minusHours(hoursBefore.toLong())
            .minusMinutes(minutesBefore.toLong())
    }

    // Проверяет, относится ли пара к текущему дню недели
    fun isToday(): Boolean {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == currentDay
    }

    companion object {
        fun createEmptyLesson(number: String, dayOfWeek: Int, weekType: String): Lesson {
            return Lesson(
                id = "",
                type = "",
                title = "",
                number = number,
                time = "",
                audience = "",
                teacher = "",
                typeOfTest = "",
                building = "",
                address = "",
                dayOfWeek = dayOfWeek,
                weekType = weekType,
                subgroup = "",
                group = "",
                isEmptyLesson = true
            )
        }
    }
}