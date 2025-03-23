package com.example.studentportal.ui.news

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.R
import com.example.studentportal.data.repository.NewsRepository
import com.example.studentportal.ui.news.adapter.NewsAdapter
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class NewsFragment : Fragment() {
    private lateinit var btnBottomSheet: ImageView
    private lateinit var btnImportant: ImageView
    private lateinit var selectedCategoriesContainer: FlexboxLayout
    private val selectedCategories = mutableListOf<String>()
    private lateinit var newsAdapter: NewsAdapter
    private var showOnlyImportant = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsRecyclerView = view.findViewById<RecyclerView>(R.id.news_list)
        newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        newsAdapter = NewsAdapter(
            newsList = NewsRepository.newss,
            selectedCategories = selectedCategories,
            context = requireContext()
        )
        newsRecyclerView.adapter = newsAdapter

        btnBottomSheet = view.findViewById(R.id.news_filter)
        btnImportant = view.findViewById(R.id.news_important)

        setupClickListeners()
        setupBottomSheet()

    }

    private fun setupClickListeners() {
        // Обработка кнопки "важные новости"
        btnImportant.setOnClickListener {
            showOnlyImportant = !showOnlyImportant
            val iconRes =
                if (showOnlyImportant) R.drawable.ic_important_news else R.drawable.ic_important
            btnImportant.setImageResource(iconRes)
            newsAdapter.updateNews(NewsRepository.newss, selectedCategories, showOnlyImportant)
        }

        btnImportant.setImageResource(
            if (showOnlyImportant) R.drawable.ic_important_news
            else R.drawable.ic_important
        )

        // Обработка кликов на элементы списка
        newsAdapter.setOnItemClickListener { news ->
            val action = NewsFragmentDirections.actionNewsToDetails(
                newsTitle = news.title,
                newsDate = news.date,
                newsAuthor = news.author,
                newsMainPhoto = getImageResource(news.image),
                newsDescriptionPart1 = splitDescription(news.description).first,
                newsDescriptionPart2 = splitDescription(news.description).second,
                newsCategories = news.categories.toTypedArray(),
                newsGalleryImages = news.galleryImages.toTypedArray()
            )
            findNavController().navigate(action)
        }
    }

    @SuppressLint("InflateParams")
    private fun setupBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.filter_activity, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.behavior.isFitToContents = true
        bottomSheetDialog.setContentView(bottomSheetView)
        selectedCategoriesContainer =
            bottomSheetView.findViewById(R.id.selected_categories_container)

        // Обработка выбора категорий
        listOf(
            R.id.filter_student,
            R.id.filter_applicant,
            R.id.filter_uni,
            R.id.filter_employee,
            R.id.filter_fvt,
            R.id.filter_samodelka
        ).forEach { id ->
            bottomSheetView.findViewById<TextView>(id).setOnClickListener {
                val categoryView = it as TextView
                val category = categoryView.text.toString()
                toggleCategory(category)
                newsAdapter.updateNews(NewsRepository.newss, selectedCategories, showOnlyImportant)
            }
        }

        btnBottomSheet.setOnClickListener {
            bottomSheetDialog.show()
            updateSelectedCategoriesUI()
        }
    }

    private fun toggleCategory(category: String) {
        if (selectedCategories.contains(category)) {
            selectedCategories.remove(category)
        } else {
            selectedCategories.add(category)
        }
        updateSelectedCategoriesUI()
    }

    private fun updateSelectedCategoriesUI() {
        selectedCategoriesContainer.removeAllViews()
        selectedCategories.forEach { category ->
            val view = layoutInflater.inflate(
                R.layout.selected_category_item,
                selectedCategoriesContainer,
                false
            ).apply {
                findViewById<TextView>(R.id.selected_category_text).text = category
                findViewById<ImageView>(R.id.selected_category_remove).setOnClickListener {
                    selectedCategories.remove(category)
                    updateSelectedCategoriesUI()
                    newsAdapter.updateNews(
                        NewsRepository.newss,
                        selectedCategories,
                        showOnlyImportant
                    )
                }
            }
            selectedCategoriesContainer.addView(view)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun getImageResource(imageName: String): Int {
        return resources.getIdentifier(imageName, "drawable", requireContext().packageName)
    }

    private fun splitDescription(text: String): Pair<String, String> {
        val sentences = text.split(".")
        return if (sentences.size > 5) {
            val mid = sentences.size / 3
            sentences.take(mid).joinToString(".") to sentences.drop(mid).joinToString(".")
        } else {
            text to ""
        }
    }
}