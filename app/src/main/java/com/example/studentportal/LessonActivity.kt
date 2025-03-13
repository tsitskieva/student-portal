package com.example.studentportal

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.TouchDelegate
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LessonActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lesson)

        // Получаем уникальный идентификатор пары из Intent
        val lessonId = intent.getStringExtra("lessonId") ?: "defaultId" // Используем "defaultId", если идентификатор не передан

        // Восстанавливаем текст из SharedPreferences
        val sharedPreferences = getSharedPreferences("LessonActivityPrefs", MODE_PRIVATE)
        val savedComment = sharedPreferences.getString("comment_$lessonId", "") // Используем уникальный ключ

        val commentInput = findViewById<EditText>(R.id.commentInput)
        commentInput.setText(savedComment) // Устанавливаем сохраненный текст

        // Находим все TextView для отображения информации о паре
        val title: TextView = findViewById(R.id.lesson_title)
        val typeOfTest: TextView = findViewById(R.id.test_type)
        val type: TextView = findViewById(R.id.lesson_type)
        val audience: TextView = findViewById(R.id.lesson_audience)
        val building: TextView = findViewById(R.id.lesson_building)
        val teacher: TextView = findViewById(R.id.lesson_teacher)

        // Устанавливаем данные из Intent
        title.text = intent.getStringExtra("lessonTitle")
        typeOfTest.text = intent.getStringExtra("lessonTypeOfTest")
        type.text = intent.getStringExtra("lessonType")
        audience.text = intent.getStringExtra("lessonAudience")
        building.text = intent.getStringExtra("lessonBuilding")
        teacher.text = intent.getStringExtra("lessonTeacher")

        // Находим кнопку btnBack
        val btnBack: ImageView = findViewById(R.id.btnBack)

        // Устанавливаем обработчик нажатия
        btnBack.setOnClickListener {
            finish() // Просто завершаем текущую активность
        }

        // Увеличиваем область нажатия для btnBack
        val parent = btnBack.parent as View
        parent.post {
            val rect = Rect()
            btnBack.getHitRect(rect) // Получаем текущую область нажатия

            // Увеличиваем область нажатия (например, на 20dp со всех сторон)
            val extendSize = (20 * resources.displayMetrics.density).toInt()
            rect.left -= extendSize
            rect.top -= extendSize
            rect.right += extendSize
            rect.bottom += extendSize

            // Устанавливаем новую область нажатия
            parent.touchDelegate = TouchDelegate(rect, btnBack)
        }
    }

    override fun onPause() {
        super.onPause()
        // Получаем уникальный идентификатор пары из Intent
        val lessonId = intent.getStringExtra("lessonId") ?: "defaultId"

        // Сохраняем текст из EditText в SharedPreferences
        val commentInput = findViewById<EditText>(R.id.commentInput)
        val sharedPreferences = getSharedPreferences("LessonActivityPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("comment_$lessonId", commentInput.text.toString()) // Используем уникальный ключ
        editor.apply() // Асинхронное сохранение
    }
}