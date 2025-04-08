package com.example.studentportal

import android.graphics.Rect
import android.widget.Toast
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.TouchDelegate
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.registerForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupsAllListActivity : ComponentActivity() {
    private lateinit var adapter: GroupsAllAdapter
    private lateinit var allGroups: List<group>
    private lateinit var selectedGroups: MutableList<group>

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedGroups = SelectedGroupsManager.getSelectedGroups(this).toMutableList()
            filterGroups("")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_group_all)

        val backButton = findViewById<ImageView>(R.id.back_to_group_settings)
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

        allGroups = GroupsData.groupsList.map { group(it.direction, it.group) }
        selectedGroups = SelectedGroupsManager.getSelectedGroups(this).toMutableList()

        val recyclerView = findViewById<RecyclerView>(R.id.list_of_all_groups)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = GroupsAllAdapter(
            allGroups.filter { group ->
                !selectedGroups.any { it.direction == group.direction && it.group == group.group }
            }
        ) { group ->
            if (selectedGroups.any { it.direction == group.direction && it.group == group.group }) {
                Toast.makeText(this, "Эта группа уже добавлена", Toast.LENGTH_SHORT).show()
            } else {
                selectedGroups.forEach { it.isActive = false }
                val newGroup = group.copy(isActive = true)
                selectedGroups.add(newGroup)
                SelectedGroupsManager.saveSelectedGroups(this, selectedGroups)

                adapter.updateList(allGroups.filter { g ->
                    !selectedGroups.any { it.direction == g.direction && it.group == g.group }
                })

                Toast.makeText(this, "Группа добавлена", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
            }
        }
        recyclerView.adapter = adapter

        val searchInput = findViewById<EditText>(R.id.searchInput)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterGroups(s.toString())
            }
        })
    }

    private fun filterGroups(query: String) {
        val filteredList = if (query.isEmpty()) {
            allGroups.filter { group ->
                !selectedGroups.any { it.direction == group.direction && it.group == group.group }
            }
        } else {
            allGroups.filter {
                (it.direction.contains(query, ignoreCase = true) ||
                        it.group.contains(query, ignoreCase = true)) &&
                        !selectedGroups.any { selected ->
                            selected.direction == it.direction && selected.group == it.group
                        }
            }
        }
        adapter.updateList(filteredList)
    }
}