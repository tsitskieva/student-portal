import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.studentportal.R
import java.text.SimpleDateFormat
import java.util.*

class WeekManager(
    private val weekDays: List<TextView>, // Список TextView для отображения чисел недели
    private val monthYearTextView: TextView, // TextView для отображения месяца и года
    private val weekTypeTextView: TextView // TextView для отображения типа недели
) {
    private val calendar: Calendar = Calendar.getInstance()
    private val monthYearFormat = SimpleDateFormat("LLLL yyyy", Locale("ru")) // Формат для отображения месяца и года
    private var selectedDayOfWeek: Int = calendar.get(Calendar.DAY_OF_WEEK) // Выбранный день недели

    init {
        calendar.firstDayOfWeek = Calendar.MONDAY // Устанавливаем понедельник как первый день недели
        updateWeek() // Инициализация первой недели
    }

    // Переход на следующую неделю
    fun nextWeek() {
        calendar.add(Calendar.DAY_OF_YEAR, 7) // Переход на 7 дней вперед
        updateWeek()
    }

    // Переход на предыдущую неделю
    fun previousWeek() {
        calendar.add(Calendar.DAY_OF_YEAR, -7) // Переход на 7 дней назад
        updateWeek()
    }

    // Обновление интерфейса
    private fun updateWeek() {
        val tempCalendar = calendar.clone() as Calendar

        // Устанавливаем календарь на начало текущей недели (понедельник)
        tempCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        // Получаем сегодняшний день
        val todayCalendar = Calendar.getInstance()
        val todayDayOfMonth = todayCalendar.get(Calendar.DAY_OF_MONTH)
        val todayMonth = todayCalendar.get(Calendar.MONTH)
        val todayYear = todayCalendar.get(Calendar.YEAR)

        // Обновляем TextView с числами недели
        for (i in 0 until 7) {
            val dayOfMonth = tempCalendar.get(Calendar.DAY_OF_MONTH)
            val month = tempCalendar.get(Calendar.MONTH)
            val year = tempCalendar.get(Calendar.YEAR)

            // Форматируем число дня месяца в формат "01", "02", ..., "31"
            val formattedDay = String.format(Locale.getDefault(), "%02d", dayOfMonth)
            weekDays[i].text = formattedDay // Устанавливаем отформатированное число

            // Проверяем, является ли текущий день сегодняшним
            if (dayOfMonth == todayDayOfMonth && month == todayMonth && year == todayYear) {
                // Применяем фон для сегодняшнего дня
                weekDays[i].background = ContextCompat.getDrawable(weekDays[i].context, R.drawable.today_circle_background)
            } else {
                // Сбрасываем фон, если день не сегодняшний
                weekDays[i].background = null
            }

            // Проверяем, является ли текущий день выбранным
            val selectedDayOfWeek = getSelectedDayOfWeek()
            val currentDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK)
            if (currentDayOfWeek == selectedDayOfWeek) {
                // Применяем фон для выбранного дня
                weekDays[i].background = ContextCompat.getDrawable(weekDays[i].context, R.drawable.circle_background)
            }

            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Обновляем TextView с месяцем и годом
        val monthYearText = monthYearFormat.format(calendar.time)
        monthYearTextView.text = monthYearText.replaceFirstChar { it.uppercase() } // Делаем первую букву заглавной

        // Обновляем TextView с типом недели
        val weekType = getWeekType()
        weekTypeTextView.text = weekType
    }

    // Получение типа недели
    private fun getWeekType(): String {
        val startDate = Calendar.getInstance().apply {
            set(2024, Calendar.SEPTEMBER, 2) // 2 сентября 2024 года
        }

        // Вычисляем разницу в миллисекундах между текущей датой и начальной датой
        val diffInMillis = calendar.timeInMillis - startDate.timeInMillis
        val diffInWeeks = (diffInMillis / (1000 * 60 * 60 * 24 * 7)).toInt()

        // Если разница в неделях четная, то это верхняя неделя, иначе — нижняя
        return if (diffInWeeks % 2 == 0) "Верхняя неделя" else "Нижняя неделя"
    }

    // Установка выбранного дня недели
    fun setSelectedDayOfWeek(dayOfWeek: Int) {
        selectedDayOfWeek = dayOfWeek
        Log.d("WeekManager", "Selected day of week: $dayOfWeek")
    }

    // Получение выбранного дня недели
    fun getSelectedDayOfWeek(): Int {
        return selectedDayOfWeek
    }

    // Получение текущего типа недели
    fun getCurrentWeekType(): String {
        return getWeekType()
    }

    // Получение индекса текущего дня недели
    fun getCurrentDayIndex(): Int {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        Log.d("WeekManager", "Current date: ${calendar.time}, Day of week: $dayOfWeek")
        return when (dayOfWeek) {
            Calendar.SUNDAY -> 6 // Воскресенье = 6
            else -> dayOfWeek - 2 // Понедельник = 0, Вторник = 1, ..., Суббота = 5
        }
    }
}