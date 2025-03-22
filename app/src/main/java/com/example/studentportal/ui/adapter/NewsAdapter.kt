package com.example.studentportal.ui.adapter

import com.example.studentportal.R
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.NewsDescActivity
import com.example.studentportal.data.news

class NewsAdapter(
    private var newsList: List<news>,
    private val selectedCategories: List<String>,
    var context: Context
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    // фильтруем список новостей на основе выбранных категорий и важности
    private fun filterNews(newsList: List<news>, selectedCategories: List<String>, showOnlyImportant: Boolean): List<news> {
        return newsList.filter { news ->
            // фильтр по важности
            val matchesImportance = !showOnlyImportant || news.isImportant
            // фильтр по категориям
            val matchesCategories = selectedCategories.isEmpty() || news.categories.any { it in selectedCategories }
            matchesImportance && matchesCategories
        }
    }

    // обновляем список новостей
    fun updateNews(newsList: List<news>, selectedCategories: List<String>, showOnlyImportant: Boolean = false) {
        this.newsList = filterNews(newsList, selectedCategories, showOnlyImportant)
        notifyDataSetChanged() // уведомляем адаптер об изменениях
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.news_in_list, parent, false)
        return NewsViewHolder(itemView)
    }

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
            val categoryView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.category_item, holder.categoriesContainer, false)
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
            val intent = Intent(context, NewsDescActivity::class.java)

            intent.putExtra("newsTitle", currentItem.title)
            intent.putExtra("newsDate", currentItem.date)
            intent.putExtra("newsDescription", currentItem.description)
            intent.putExtra("newsAuthor", currentItem.author)
            intent.putExtra("newsMainPhoto", imageResource)
            intent.putStringArrayListExtra("newsCategories", ArrayList(currentItem.categories))
            intent.putStringArrayListExtra("newsGalleryImages", ArrayList(currentItem.galleryImages))

            context.startActivity(intent)
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