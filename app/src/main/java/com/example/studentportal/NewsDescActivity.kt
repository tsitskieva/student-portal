package com.example.studentportal

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.data.dataNews
import com.example.studentportal.ui.adapter.GalleryAdapter
import com.example.studentportal.ui.adapter.LatestNewsAdapter

class NewsDescActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.news_description)

        // находим все TextView для отображения информации о новости
        val title: TextView = findViewById(R.id.news_name)
        val date: TextView = findViewById(R.id.news_date)
        val descriptionPart1: TextView = findViewById(R.id.news_description_part1)
        val descriptionPart2: TextView = findViewById(R.id.news_description_part2)
        val author: TextView = findViewById(R.id.news_author)
        val categoriesContainer: LinearLayout = findViewById(R.id.categories_container_desc)
        val imageResId = intent.getIntExtra("newsMainPhoto", 0)
        if (imageResId != 0) {
            findViewById<ImageView>(R.id.news_main_photo).setImageResource(imageResId)
        }
        val galleryRecyclerView: RecyclerView = findViewById(R.id.gallery_recycler_view)

        // устанавливаем данные из Intent
        title.text = intent.getStringExtra("newsTitle")
        date.text = intent.getStringExtra("newsDate")
        author.text = intent.getStringExtra("newsAuthor")
        val fullDescription = intent.getStringExtra("newsDescription")
        val categories = intent.getStringArrayListExtra("newsCategories")
        val galleryImages = intent.getStringArrayListExtra("newsGalleryImages")
        val linearLayout2: LinearLayout = findViewById(R.id.linearLayout2)
        val linearLayout3: LinearLayout = findViewById(R.id.linearLayout3)

        // разделяем текст на две части, если предложений больше 5
        if (fullDescription != null) {
            val sentences = fullDescription.split(".") // разделяем текст по точкам
            if (sentences.size > 5) {
                // если предложений больше 5, разделяем текст
                val (part1, part2) = splitTextIntoParts(fullDescription)
                descriptionPart1.text = part1
                descriptionPart2.text = part2

                // показываем оба контейнера
                linearLayout2.visibility = View.VISIBLE
                linearLayout3.visibility = View.VISIBLE
            } else {
                // если предложений 5 или меньше, весь текст выводим в первый контейнер
                descriptionPart1.text = fullDescription
                descriptionPart2.text = "" // второй контейнер остается пустым

                // скрываем второй контейнер
                linearLayout2.visibility = View.VISIBLE
                linearLayout3.visibility = View.GONE
            }
        }

        // находим кнопку btnBack
        val btnBack: ImageView = findViewById(R.id.back_button)

        // устанавливаем обработчик нажатия
        btnBack.setOnClickListener {
            finish() // просто завершаем текущую активность
        }
        categoriesContainer.removeAllViews()
        categories?.forEach { category ->
            val categoryView = LayoutInflater.from(this).inflate(R.layout.category_item, categoriesContainer, false)
            val categoryText = categoryView.findViewById<TextView>(R.id.category_text)
            categoryText.text = category
            categoriesContainer.addView(categoryView)

            // добавляем отступ между категориями
            val layoutParams = categoryView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(0, 0, 10, 0)
            categoryView.layoutParams = layoutParams
        }
        galleryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        if (galleryImages != null) {
            val galleryAdapter = GalleryAdapter(galleryImages, this)
            galleryRecyclerView.adapter = galleryAdapter
        }

        // находим RecyclerView для последних новостей
        val latestNewsRecyclerView: RecyclerView = findViewById(R.id.latest_news_recycler_view)

        // берем последние 3 новости
        val sortedNews = dataNews.newss.take(3)

        // устанавливаем LayoutManager и адаптер
        latestNewsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val latestNewsAdapter = LatestNewsAdapter(sortedNews, this, latestNewsRecyclerView)
        latestNewsRecyclerView.adapter = latestNewsAdapter

        // добавляем отступ между элементами
        val spacing = (10 * resources.displayMetrics.density).toInt() // 16dp в пиксели
        latestNewsRecyclerView.addItemDecoration(HorizontalSpacingItemDecoration(spacing))

        // убираем отступы в RecyclerView
        latestNewsRecyclerView.setPadding(0, 0, 0, 0)
        latestNewsRecyclerView.clipToPadding = false // Отключаем обрезку отступов

        // находим RecyclerView для важных новостей
        val latestImportantNewsRecyclerView: RecyclerView = findViewById(R.id.latest_important_news_recycler_view)

        // фильтруем по важности
        val sortedImportantNews = dataNews.newss
            .filter { it.isImportant } // Фильтруем только важные новости
            .take(3) // Берем первые 3 новости

        // устанавливаем LayoutManager и адаптер
        latestImportantNewsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val latestImportantNewsAdapter = LatestNewsAdapter(sortedImportantNews, this, latestImportantNewsRecyclerView)
        latestImportantNewsRecyclerView.adapter = latestImportantNewsAdapter

        // добавляем отступ между элементами
        latestImportantNewsRecyclerView.addItemDecoration(HorizontalSpacingItemDecoration(spacing))

        // убираем отступы в RecyclerView
        latestImportantNewsRecyclerView.setPadding(0, 0, 0, 0)
        latestImportantNewsRecyclerView.clipToPadding = false // Отключаем обрезку отступов

    }
    /**
     * разделяет текст на две части так, чтобы большая часть была во втором контейнере.
     *
     * @param text Полный текст новости.
     * @return Пара строк: первая часть (меньшая) и вторая часть (большая).
     */
    private fun splitTextIntoParts(text: String): Pair<String, String> {
        val sentences = text.split(".") // разделяем текст по точкам
        val midPoint = sentences.size / 3 // разделяем текст на 1/3 и 2/3

        // первая часть (меньшая)
        val part1 = sentences.take(midPoint).joinToString(".") + "."
        // вторая часть (большая)
        val part2 = sentences.drop(midPoint).joinToString(".") + "."

        return Pair(part1, part2)
    }
    class HorizontalSpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.right = spacing // отступ справа для каждого элемента
        }
    }
}
