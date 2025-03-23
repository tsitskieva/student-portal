package com.example.studentportal.ui.news.adapter

import android.annotation.SuppressLint
import com.example.studentportal.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.data.model.News
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsAdapter(
    private var newsList: List<News>,
    private val selectedCategories: List<String>,
    var context: Context
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private var onItemClickListener: ((News) -> Unit)? = null

    fun setOnItemClickListener(listener: (News) -> Unit) {
        onItemClickListener = listener
    }

    // фильтруем список новостей на основе выбранных категорий и важности
    private fun filterNews(
        newsList: List<News>,
        selectedCategories: List<String>,
        showOnlyImportant: Boolean
    ): List<News> {
        return newsList.filter { news ->
            // фильтр по важности
            val matchesImportance = !showOnlyImportant || news.isImportant
            // фильтр по категориям
            val matchesCategories =
                selectedCategories.isEmpty() || news.categories.any { it in selectedCategories }
            matchesImportance && matchesCategories
        }
    }

    // обновляем список новостей
    @SuppressLint("NotifyDataSetChanged")
    fun updateNews(
        newsList: List<News>,
        selectedCategories: List<String>,
        showOnlyImportant: Boolean = false
    ) {
        this.newsList = filterNews(newsList, selectedCategories, showOnlyImportant)
            .sortedByDescending { parseDate(it.date) }
            .toMutableList()
        notifyDataSetChanged()
    }

    private fun parseDate(dateString: String): Date {
        return try {
            SimpleDateFormat("dd MMM. yyyy г.", Locale("ru")).parse(dateString) ?: Date(0)
        } catch (e: Exception) {
            Date(0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.news_in_list, parent, false)
        return NewsViewHolder(itemView)
    }

    @SuppressLint("DiscouragedApi")
    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = newsList[position]

        // устанавливаем заголовок и дату
        holder.newsTitle.text = currentItem.title
        holder.newsDate.text = currentItem.date

        // загружаем изображение из ресурсов
        val imageResource = holder.itemView.context.resources.getIdentifier(
            currentItem.image, // имя файла без расширения
            "drawable",
            holder.itemView.context.packageName
        )
        holder.newsImage.setImageResource(imageResource)

        // очищаем контейнер категорий перед добавлением новых
        holder.categoriesContainer.removeAllViews()

        // добавляем категории
        currentItem.categories.forEach { category ->
            val categoryView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.category_item, holder.categoriesContainer, false)
            val categoryText = categoryView.findViewById<TextView>(R.id.category_text)
            categoryText.text = category
            holder.categoriesContainer.addView(categoryView)

            // добавляем отступ между категориями
            val layoutParams = categoryView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(0, 0, 10, 0)
            categoryView.layoutParams = layoutParams
        }

        // отображаем иконку важности, если новость важная
        if (currentItem.isImportant) {
            holder.importantIcon.visibility = View.VISIBLE
        } else {
            holder.importantIcon.visibility = View.GONE
        }

        holder.btn.setOnClickListener {
            onItemClickListener?.invoke(currentItem)
        }
    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsImage: ImageView = itemView.findViewById(R.id.news_list_photo)
        val newsTitle: TextView = itemView.findViewById(R.id.news_list_name)
        val newsDate: TextView = itemView.findViewById(R.id.news_list_date)
        val categoriesContainer: LinearLayout = itemView.findViewById(R.id.categories_container)
        val importantIcon: ImageView = itemView.findViewById(R.id.important_icon)
        val btn: LinearLayout = itemView.findViewById(R.id.news_button)
    }

    override fun getItemCount() = newsList.size
}