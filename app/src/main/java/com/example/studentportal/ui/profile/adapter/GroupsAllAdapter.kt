package com.example.studentportal.ui.profile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.R
import com.example.studentportal.data.model.Group

class GroupsAllAdapter(
    private var groups: List<Group>,
    private val onAddClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupsAllAdapter.GroupViewHolder>() {

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val directionText: TextView = itemView.findViewById(R.id.group_list_text1)
        val groupText: TextView = itemView.findViewById(R.id.group_list_text2)
        val addButton: ImageView = itemView.findViewById(R.id.group_list_button_add)
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

        holder.addButton.setOnClickListener {
            onAddClick(currentItem)
        }
    }

    override fun getItemCount() = groups.size

    fun updateList(newList: List<Group>) {
        groups = newList
        notifyDataSetChanged()
    }

    fun getGroups() = groups
}