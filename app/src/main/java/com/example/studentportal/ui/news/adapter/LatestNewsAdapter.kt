package com.example.studentportal.ui.news.adapter

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

class LatestNewsAdapter(
    private val newsList: List<News>,
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val onItemClick: (News) -> Unit
) : RecyclerView.Adapter<LatestNewsAdapter.LatestNewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestNewsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.news_in_description, parent, false)
        return LatestNewsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LatestNewsViewHolder, position: Int) {
        val currentItem = newsList[position]

        holder.newsTitle.text = currentItem.title
        holder.newsDate.text = currentItem.date
        holder.categoriesContainer.removeAllViews()

        currentItem.categories.forEach { category ->
            val categoryView = LayoutInflater.from(context)
                .inflate(R.layout.category_item, holder.categoriesContainer, false)
            categoryView.findViewById<TextView>(R.id.category_text).text = category
            holder.categoriesContainer.addView(categoryView)
        }

        holder.importantIcon.visibility = if (currentItem.isImportant) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener { onItemClick(currentItem) }
    }

    override fun getItemCount() = newsList.size

    class LatestNewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsTitle: TextView = itemView.findViewById(R.id.news_desc_name_list)
        val newsDate: TextView = itemView.findViewById(R.id.news_desc_date_list)
        val categoriesContainer: LinearLayout = itemView.findViewById(R.id.categories_container)
        val importantIcon: ImageView = itemView.findViewById(R.id.important_icon)
    }
}