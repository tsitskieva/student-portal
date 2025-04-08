package com.example.studentportal

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.TouchDelegate
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupsSettingsActivity : ComponentActivity() {
    private lateinit var selectedGroupsAdapter: SelectedGroupsAdapter
    private lateinit var selectedGroups: MutableList<group>
    private lateinit var recyclerView: RecyclerView

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            updateGroupsList()
            setResult(RESULT_OK)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups_settings)

        selectedGroups = SelectedGroupsManager.getSelectedGroups(this).toMutableList()

        recyclerView = findViewById<RecyclerView>(R.id.list_of_choosen_groups)
        recyclerView.layoutManager = LinearLayoutManager(this)

        selectedGroupsAdapter = SelectedGroupsAdapter(
            selectedGroups,
            onItemClick = { clickedGroup ->
                selectedGroups.forEach { it.isActive = false }
                clickedGroup.isActive = true
                SelectedGroupsManager.saveSelectedGroups(this, selectedGroups)
                selectedGroupsAdapter.updateList(selectedGroups)
                setResult(RESULT_OK)
            },
            onDeleteClick = { groupToDelete ->
                val isDeletingActive = groupToDelete.isActive
                selectedGroups.remove(groupToDelete)

                if (isDeletingActive && selectedGroups.isNotEmpty()) {
                    selectedGroups[0].isActive = true
                }

                SelectedGroupsManager.saveSelectedGroups(this, selectedGroups)
                updateGroupsList()
                setResult(RESULT_OK)
            }
        )

        recyclerView.adapter = selectedGroupsAdapter
        selectedGroupsAdapter.setupSwipeToDelete(recyclerView)

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

        val buttonToList = findViewById<Button>(R.id.button_to_list_of_groups)
        buttonToList.setOnClickListener {
            val intent = Intent(this, GroupsAllListActivity::class.java)
            startForResult.launch(intent)
        }
    }

    private fun updateGroupsList() {
        selectedGroups = SelectedGroupsManager.getSelectedGroups(this).toMutableList()

        if (selectedGroups.isNotEmpty() && !selectedGroups.any { it.isActive }) {
            selectedGroups[0].isActive = true
            SelectedGroupsManager.saveSelectedGroups(this, selectedGroups)
        }

        selectedGroupsAdapter.updateList(selectedGroups)
    }
}