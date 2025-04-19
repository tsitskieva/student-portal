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