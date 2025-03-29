package com.example.studentportal

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.TouchDelegate
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.registerForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupsSettingsActivity : ComponentActivity() {
    private lateinit var selectedGroupsAdapter: SelectedGroupsAdapter
    private lateinit var selectedGroups: MutableList<group>

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        updateGroupsList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups_settings)

        selectedGroups = SelectedGroupsManager.getSelectedGroups(this).toMutableList()

        val selectedGroupsRecyclerView = findViewById<RecyclerView>(R.id.list_of_choosen_groups)
        selectedGroupsRecyclerView.layoutManager = LinearLayoutManager(this)

        selectedGroupsAdapter = SelectedGroupsAdapter(
            selectedGroups,
            onItemClick = { clickedGroup ->
                // Делаем кликнутую группу активной, остальные - неактивными
                selectedGroups.forEach { it.isActive = false }
                clickedGroup.isActive = true
                SelectedGroupsManager.saveSelectedGroups(this, selectedGroups)
                selectedGroupsAdapter.updateList(selectedGroups)
            },
            onDeleteClick = { groupToDelete ->
                selectedGroups.remove(groupToDelete)
                if (groupToDelete.isActive && selectedGroups.isNotEmpty()) {
                    // Если удалили активную группу, делаем следующую активной
                    selectedGroups[0].isActive = true
                }
                SelectedGroupsManager.saveSelectedGroups(this, selectedGroups)
                selectedGroupsAdapter.updateList(selectedGroups)
            }
        )

        selectedGroupsRecyclerView.adapter = selectedGroupsAdapter

        // Настройка кнопки "Назад"
        val backButton = findViewById<ImageView>(R.id.back_to_all_settings)
        val parent = backButton.parent as ViewGroup

        parent.post {
            val hitRect = Rect()
            backButton.getHitRect(hitRect)
            val extendBy = (20 * resources.displayMetrics.density).toInt()
            hitRect.inset(-extendBy, -extendBy)
            parent.touchDelegate = TouchDelegate(hitRect, backButton)
        }

        backButton.setOnClickListener {
            finish()
        }

        // Настройка кнопки добавления группы
        val buttonToList = findViewById<Button>(R.id.button_to_list_of_groups)
        buttonToList.setOnClickListener {
            val intent = Intent(this, GroupsAllListActivity::class.java)
            startForResult.launch(intent)
        }
    }

    private fun updateGroupsList() {
        selectedGroups = SelectedGroupsManager.getSelectedGroups(this).toMutableList()
        selectedGroupsAdapter.updateList(selectedGroups)
    }
}