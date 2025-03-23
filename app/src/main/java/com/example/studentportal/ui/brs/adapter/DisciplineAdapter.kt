package com.example.studentportal.ui.brs.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.R
import com.example.studentportal.data.model.Discipline

class DisciplineAdapter(
    private var items: List<Discipline>,
    private val onItemClick: (Int) -> Unit
) :
    RecyclerView.Adapter<DisciplineAdapter.ViewHolder>() {

    private val cache = mutableMapOf<Int, ViewHolder>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<Discipline>) {
        items = newItems
        cache.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val currentScore: TextView = view.findViewById(R.id.tvCurrentScore)
        val maxScore: TextView = view.findViewById(R.id.tvMaxScore)
        val progressIndicator: View = view.findViewById(R.id.progressIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discipline, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.name
        holder.currentScore.text = item.score.toString()
        holder.maxScore.text = item.maxScore.toString()

        val maxScore = item.maxScore.toFloat()
        val currentScore = item.score.toFloat()

        val progress = when {
            maxScore > 0 -> {
                if (currentScore == 0f) 0.03f else currentScore / maxScore
            }

            currentScore > 0 -> 1f
            else -> 0.03f
        }

        val params = holder.progressIndicator.layoutParams as LinearLayout.LayoutParams
        params.weight = progress
        holder.progressIndicator.layoutParams = params
        holder.progressIndicator.requestLayout()

        val color = when {
            maxScore <= 60 -> when {
                currentScore < 38 -> "#FF4444"
                currentScore < 45 -> "#FFC107"
                else -> "#4CAF50"
            }

            else -> when {
                currentScore < 60 -> "#FF4444"
                currentScore < 70 -> "#FFC107"
                else -> "#4CAF50"
            }
        }

        holder.progressIndicator.setBackgroundColor(Color.parseColor(color))
        holder.itemView.setOnClickListener { onItemClick(item.id) }

    }

    override fun getItemCount() = items.size
}