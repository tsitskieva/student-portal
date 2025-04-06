package com.example.studentportal.ui.news.desc

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.R
import com.example.studentportal.data.model.News
import com.example.studentportal.data.repository.NewsRepository
import com.example.studentportal.ui.news.adapter.GalleryAdapter
import com.example.studentportal.ui.news.adapter.LatestNewsAdapter

class NewsDescFragment : Fragment(R.layout.news_description) {

    private val args: NewsDescFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация элементов
        val title: TextView = view.findViewById(R.id.news_name)
        val date: TextView = view.findViewById(R.id.news_date)
        val author: TextView = view.findViewById(R.id.news_author)
        val categoriesContainer: LinearLayout = view.findViewById(R.id.categories_container_desc)
        val galleryRecyclerView: RecyclerView = view.findViewById(R.id.gallery_recycler_view)
        val btnBack: ImageView = view.findViewById(R.id.back_button)

        // Установка данных
        title.text = args.newsTitle
        date.text = args.newsDate
        author.text = args.newsAuthor
        view.findViewById<ImageView>(R.id.news_main_photo).setImageResource(args.newsMainPhoto)

        // Категории
        args.newsCategories?.forEach { category ->
            val categoryView =
                layoutInflater.inflate(R.layout.category_item, categoriesContainer, false)
            categoryView.findViewById<TextView>(R.id.category_text).text = category
            categoriesContainer.addView(categoryView)

            val layoutParams = categoryView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(0, 0, 10, 0)
            categoryView.layoutParams = layoutParams
        }

        // Галерея
        galleryRecyclerView.adapter = GalleryAdapter(
            args.newsGalleryImages?.toList() ?: emptyList(),
            requireContext()
        )

        // Рекомендуемые новости
        setupRecyclerView(
            view.findViewById(R.id.latest_news_recycler_view),
            NewsRepository.newss.take(3)
        )

//        setupRecyclerView(
//            view.findViewById(R.id.latest_important_news_recycler_view),
//            NewsRepository.newss.filter { it.isImportant }.take(3)
//        )

        // Кнопка назад
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView, data: List<News>) {
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_10dp)
        recyclerView.addItemDecoration(HorizontalSpacingItemDecoration(spacingInPixels))

        recyclerView.adapter = LatestNewsAdapter(
            data,
            requireContext(),
            recyclerView
        ) { news ->
            val action = NewsDescFragmentDirections.actionNewsDescFragmentSelf(
                newsTitle = news.title,
                newsDate = news.date,
                newsAuthor = news.author,
                newsMainPhoto = getImageResource(news.image),
                newsDescriptionPart1 = splitDescription(news.description).first,
                newsDescriptionPart2 = splitDescription(news.description).second,
                newsCategories = news.categories.toTypedArray(),
                newsGalleryImages = news.galleryImages.toTypedArray()
            )
            findNavController().navigate(action as NavDirections)
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