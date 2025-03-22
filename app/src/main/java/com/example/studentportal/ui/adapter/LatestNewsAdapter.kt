package com.example.studentportal.ui.adapter

import com.example.studentportal.R
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.NewsDescActivity
import com.example.studentportal.data.news

class LatestNewsAdapter(
    private val newsList: List<news>,
    private val context: Context,
    private val recyclerView: RecyclerView // передаем RecyclerView
) : RecyclerView.Adapter<LatestNewsAdapter.LatestNewsViewHolder>() {

    private var maxHeight = 0
    private val itemHeights = mutableMapOf<Int, Int>() // храним высоты элементов

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestNewsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.news_in_description, parent, false)

        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val itemWidth = (screenWidth * 0.88).toInt() // 88% ширины экрана
        itemView.layoutParams.width = itemWidth

        return LatestNewsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LatestNewsViewHolder, position: Int) {
        val currentItem = newsList[position]

        // устанавливаем заголовок и дату
        holder.newsTitle.text = currentItem.title
        holder.newsDate.text = currentItem.date

        // очищаем контейнер категорий перед добавлением новых
        holder.categoriesContainer.removeAllViews()

        // если новость важная, отображаем иконку
        if (currentItem.isImportant) {
            holder.importantIcon.visibility = View.VISIBLE
        } else {
            holder.importantIcon.visibility = View.GONE
        }

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

        // обработчик нажатия на элемент списка
        holder.itemView.setOnClickListener {
            // создаем Intent для перехода на NewsDescActivity
            val intent = Intent(context, NewsDescActivity::class.java).apply {
                // передаем данные выбранной новости
                putExtra("newsTitle", currentItem.title)
                putExtra("newsDate", currentItem.date)
                putExtra("newsDescription", currentItem.description)
                putExtra("newsAuthor", currentItem.author)
                putExtra("newsCategories", ArrayList(currentItem.categories))
                putExtra("newsGalleryImages", ArrayList(currentItem.galleryImages))
            }
            context.startActivity(intent) // запускаем NewsDescActivity
        }

        // измеряем высоту элемента после его отрисовки
        holder.itemView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // убедимся, что этот listener вызывается только один раз
                holder.itemView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val height = holder.itemView.height
                itemHeights[position] = height // сохраняем высоту элемента

                // если текущая высота больше максимальной, обновляем maxHeight
                if (height > maxHeight) {
                    maxHeight = height
                    // устанавливаем новую высоту для всех элементов
                    updateAllItemsHeight()
                }

                // устанавливаем высоту текущего элемента
                holder.itemView.layoutParams.height = maxHeight
                holder.itemView.requestLayout() // принудительно перерисовываем элемент
            }
        })

        // если максимальная высота уже известна, устанавливаем её для текущего элемента
        if (maxHeight > 0) {
            holder.itemView.layoutParams.height = maxHeight
            holder.itemView.requestLayout()
        }
    }

    override fun getItemCount() = newsList.size

    private fun updateAllItemsHeight() {
        // проходим по всем элементам и устанавливаем им максимальную высоту
        for (i in 0 until itemCount) {
            val holder = recyclerView.findViewHolderForAdapterPosition(i) as? LatestNewsViewHolder
            holder?.itemView?.let { view ->
                view.layoutParams.height = maxHeight
                view.requestLayout()
            }
        }
    }

    class LatestNewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsTitle: TextView = itemView.findViewById(R.id.news_desc_name_list)
        val newsDate: TextView = itemView.findViewById(R.id.news_desc_date_list)
        val categoriesContainer: LinearLayout = itemView.findViewById(R.id.categories_container)
        val importantIcon: ImageView = itemView.findViewById(R.id.important_icon)
    }
}