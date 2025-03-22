package com.example.studentportal
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.data.dataNews
import com.example.studentportal.ui.adapter.NewsAdapter
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class NewsActivity : ComponentActivity() {
    private lateinit var btnBottomSheet: ImageView
    private lateinit var selectedCategoriesContainer: FlexboxLayout
    private val selectedCategories = mutableListOf<String>()
    private lateinit var newsAdapter: NewsAdapter
    private var showOnlyImportant = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        // инициализация RecyclerView и адаптера
        val newsRecyclerView: RecyclerView = findViewById(R.id.news_list)
        newsRecyclerView.layoutManager = LinearLayoutManager(this)
        newsAdapter = NewsAdapter(dataNews.newss, selectedCategories, this)
        newsRecyclerView.adapter = newsAdapter

        btnBottomSheet = findViewById(R.id.news_filter)

        // находим элемент "важные новости"
        val importantNewsButton: ImageView = findViewById(R.id.news_important)

        // обработчик нажатия на "важные новости"
        importantNewsButton.setOnClickListener {
            showOnlyImportant = !showOnlyImportant // переключаем флаг
            // обновляем список новостей в адаптере
            newsAdapter.updateNews(dataNews.newss, selectedCategories, showOnlyImportant)
        }

        val bottomSheetView = layoutInflater.inflate(R.layout.filter_activity, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)

        selectedCategoriesContainer = bottomSheetView.findViewById(R.id.selected_categories_container)

        // обработка нажатий на категории
        val categories = listOf(
            bottomSheetView.findViewById<TextView>(R.id.filter_student),
            bottomSheetView.findViewById<TextView>(R.id.filter_applicant),
            bottomSheetView.findViewById<TextView>(R.id.filter_uni),
            bottomSheetView.findViewById<TextView>(R.id.filter_employee),
            bottomSheetView.findViewById<TextView>(R.id.filter_fvt),
            bottomSheetView.findViewById<TextView>(R.id.filter_samodelka)
        )

        categories.forEach { category ->
            category.setOnClickListener {
                val categoryText = category.text.toString()
                if (selectedCategories.contains(categoryText)) {
                    selectedCategories.remove(categoryText)
                } else {
                    selectedCategories.add(categoryText)
                }
                updateSelectedCategories()
                // обновляем список новостей в адаптере
                newsAdapter.updateNews(dataNews.newss, selectedCategories, showOnlyImportant)
            }
        }

        btnBottomSheet.setOnClickListener {
            bottomSheetDialog.show()
        }
    }

    private fun updateSelectedCategories() {
        selectedCategoriesContainer.removeAllViews()
        selectedCategories.forEach { category ->
            val categoryView = layoutInflater.inflate(R.layout.selected_category_item, selectedCategoriesContainer, false)
            val categoryText = categoryView.findViewById<TextView>(R.id.selected_category_text)
            val removeButton = categoryView.findViewById<ImageView>(R.id.selected_category_remove)

            categoryText.text = category
            removeButton.setOnClickListener {
                selectedCategories.remove(category)
                updateSelectedCategories()
                // обновляем список новостей в адаптере
                newsAdapter.updateNews(dataNews.newss, selectedCategories, showOnlyImportant)
            }

            selectedCategoriesContainer.addView(categoryView)
        }
    }
}