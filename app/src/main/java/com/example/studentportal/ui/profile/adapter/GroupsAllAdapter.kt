package com.example.studentportal.ui.profile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.R
import com.example.studentportal.data.model.Group

class GroupsAllAdapter(
    private var groups: List<Group>,
    private val onAddClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupsAllAdapter.GroupViewHolder>() {

    // Для отслеживания анимации, чтобы избежать множественных нажатий
    private var isAnimating = false

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupText: TextView = itemView.findViewById(R.id.group_list_text1)
        val directionText: TextView = itemView.findViewById(R.id.group_list_text2)
        val addButton: ImageView = itemView.findViewById(R.id.group_list_button_add)
        val container: View = itemView.findViewById(R.id.group_item_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_in_list_all, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val currentItem = groups[position]

        holder.directionText.text = currentItem.direction
        holder.groupText.text = currentItem.group

        // Сброс анимации для переиспользуемого ViewHolder
        holder.itemView.alpha = 1f
        holder.itemView.translationX = 0f
        holder.container.scaleX = 1f
        holder.container.scaleY = 1f

        holder.addButton.setOnClickListener {
            if (isAnimating) return@setOnClickListener

            isAnimating = true

            // 1. Анимация "нажатия" (уменьшение и затемнение)
            holder.container.animate()
                .alpha(0.7f)
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    // 2. Возврат к исходному состоянию
                    holder.container.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .setInterpolator(AccelerateInterpolator())
                        .withEndAction {
                            // 3. Анимация "удаления" элемента
                            holder.itemView.animate()
                                .alpha(0f)
                                .translationX(-holder.itemView.width * 0.5f)
                                .setDuration(250)
                                .setInterpolator(AccelerateInterpolator())
                                .withEndAction {
                                    onAddClick(currentItem)
                                    isAnimating = false
                                }
                                .start()
                        }
                        .start()
                }
                .start()
        }
    }

    override fun getItemCount() = groups.size

    fun updateList(newList: List<Group>) {
        // Оптимизация с DiffUtil для плавных анимаций
        val diffCallback = object : androidx.recyclerview.widget.DiffUtil.Callback() {
            override fun getOldListSize() = groups.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                groups[oldPos].id == newList[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                groups[oldPos] == newList[newPos]
        }

        val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(diffCallback)
        groups = newList
        diffResult.dispatchUpdatesTo(this)
    }

    fun getGroups() = groups
}