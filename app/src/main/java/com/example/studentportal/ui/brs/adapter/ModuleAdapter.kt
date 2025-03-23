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
import com.example.studentportal.data.model.Module

class ModuleAdapter(private val modules: List<Module>) :
    RecyclerView.Adapter<ModuleAdapter.ViewHolder>() {

    private val filteredModules = modules.filter { it.type != "EXAM" }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_module, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredModules[position])
    }

    override fun getItemCount() = modules.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        fun bind(module: Module) {
            val context = itemView.context
            val tvModuleTitle = itemView.findViewById<TextView>(R.id.tvModuleTitle)
            val llSubmodules = itemView.findViewById<LinearLayout>(R.id.llSubmodules)

            tvModuleTitle.visibility = if (module.title.isEmpty()) View.GONE else View.VISIBLE
            tvModuleTitle.text = module.title
            llSubmodules.removeAllViews()

            module.submodules.forEach { sub ->
                if (sub.title == "Эта дисциплина еще не заполнена") {
                    val view = LayoutInflater.from(context)
                        .inflate(R.layout.item_empty_message, llSubmodules, false)
                    llSubmodules.addView(view)
                } else {
                    val view = LayoutInflater.from(context)
                        .inflate(R.layout.item_submodule, llSubmodules, false)
                    view.findViewById<TextView>(R.id.tvTitle).text = sub.title
                    view.findViewById<TextView>(R.id.tvCurrentScore).text = sub.score.toString()
                    view.findViewById<TextView>(R.id.tvMaxScore).text = sub.maxScore.toString()

                    val progressView = view.findViewById<View>(R.id.progressIndicator)
                    val maxScore = sub.maxScore.toFloat()
                    val currentScore = sub.score.toFloat()

                    if (maxScore > 0 && currentScore >= 0) {
                        val progress = if (currentScore == 0f) 0.01f else currentScore / maxScore
                        val progressParams = progressView.layoutParams as LinearLayout.LayoutParams
                        progressParams.weight = progress
                        progressView.layoutParams = progressParams


                        val color = when {
                            currentScore < 0.6 * maxScore -> Color.parseColor("#FF4444")
                            currentScore < 0.7 * maxScore -> Color.parseColor("#FFC107")
                            else -> Color.parseColor("#4CAF50")
                        }
                        progressView.background.setTint(color)
                    } else {
                        progressView.visibility = View.GONE
                    }

                    llSubmodules.addView(view)
                }
            }
        }
    }
}