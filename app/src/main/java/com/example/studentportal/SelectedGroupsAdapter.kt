package com.example.studentportal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectedGroupsAdapter(
    private var groups: List<group>,
    private val onItemClick: (group) -> Unit,
    private val onDeleteClick: (group) -> Unit
) : RecyclerView.Adapter<SelectedGroupsAdapter.SelectedGroupViewHolder>() {

    inner class SelectedGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val directionText: TextView = itemView.findViewById(R.id.group_list_text1)
        val groupText: TextView = itemView.findViewById(R.id.group_list_text2)
        val onButton: ImageView = itemView.findViewById(R.id.group_list_button_on)
        val offButton: ImageView = itemView.findViewById(R.id.group_list_button_off)
        val activeLine: ImageView = itemView.findViewById(R.id.group_list_line_active)
        val inactiveLine: ImageView = itemView.findViewById(R.id.group_list_line_not_active)

        init {
            // Обработчик только для кнопки offButton
            offButton.setOnClickListener {
                onItemClick(groups[adapterPosition])
            }

            // Для остальной области элемента - обработчик удаления (если нужно)
            itemView.setOnClickListener {
                // Можно добавить другую логику или оставить пустым
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedGroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_in_list_settings, parent, false)
        return SelectedGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedGroupViewHolder, position: Int) {
        val currentItem = groups[position]

        holder.directionText.text = currentItem.direction
        holder.groupText.text = currentItem.group

        if (currentItem.isActive) {
            holder.onButton.visibility = View.VISIBLE
            holder.offButton.visibility = View.GONE
            holder.activeLine.visibility = View.VISIBLE
            holder.inactiveLine.visibility = View.GONE
        } else {
            holder.onButton.visibility = View.GONE
            holder.offButton.visibility = View.VISIBLE
            holder.activeLine.visibility = View.GONE
            holder.inactiveLine.visibility = View.VISIBLE
        }
    }

    override fun getItemCount() = groups.size

    fun getGroupAtPosition(position: Int): group? {
        return if (position in groups.indices) groups[position] else null
    }

    fun removeGroup(groupToRemove: group) {
        val newList = groups.toMutableList().apply {
            remove(groupToRemove)
            if (groupToRemove.isActive && isNotEmpty()) {
                first().isActive = true
            }
        }
        groups = newList
        notifyDataSetChanged()
        onDeleteClick(groupToRemove)
    }

    fun updateList(newList: List<group>) {
        groups = newList.sortedByDescending { it.isActive }
        notifyDataSetChanged()
    }
}