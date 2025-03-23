package com.example.studentportal.ui.schedule.lesson

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.studentportal.R

@Suppress("DEPRECATION")
class LessonFragment : Fragment() {
    private val args: LessonFragmentArgs by navArgs()
    private lateinit var lessonId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lesson, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем аргументы
        lessonId = args.lessonId

        // Инициализация элементов
        val title: TextView = view.findViewById(R.id.lesson_title)
        val typeOfTest: TextView = view.findViewById(R.id.test_type)
        val type: TextView = view.findViewById(R.id.lesson_type)
        val audience: TextView = view.findViewById(R.id.lesson_audience)
        val building: TextView = view.findViewById(R.id.lesson_building)
        val teacher: TextView = view.findViewById(R.id.lesson_teacher)
        val commentInput: EditText = view.findViewById(R.id.commentInput)
        val btnBack: ImageView = view.findViewById(R.id.btnBack)

        // Установка данных из аргументов
        title.text = args.lessonTitle
        typeOfTest.text = args.lessonTypeOfTest
        type.text = args.lessonType
        audience.text = args.lessonAudience
        building.text = args.lessonBuilding
        teacher.text = args.lessonTeacher

        // Восстановление комментария
        val sharedPreferences = requireContext().getSharedPreferences("LessonPrefs", Context.MODE_PRIVATE)
        commentInput.setText(sharedPreferences.getString("comment_$lessonId", ""))

        // Обработка кнопки назад
        btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        setupTouchDelegate(btnBack)
    }

    override fun onPause() {
        super.onPause()
        saveComment()
    }

    private fun saveComment() {
        val commentInput = view?.findViewById<EditText>(R.id.commentInput) ?: return
        val sharedPreferences = requireContext().getSharedPreferences("LessonPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("comment_$lessonId", commentInput.text.toString())
            .apply()
    }

    private fun setupTouchDelegate(btnBack: ImageView) {
        val parent = btnBack.parent as View
        parent.post {
            val rect = Rect()
            btnBack.getHitRect(rect)
            val extendSize = (20 * resources.displayMetrics.density).toInt()
            rect.inset(-extendSize, -extendSize)
            parent.touchDelegate = TouchDelegate(rect, btnBack)
        }
    }
}