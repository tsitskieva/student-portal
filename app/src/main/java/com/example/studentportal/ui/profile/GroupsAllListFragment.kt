package com.example.studentportal.ui.profile

import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.R
import com.example.studentportal.data.model.Group
import com.example.studentportal.data.repository.GroupsRepository
import com.example.studentportal.ui.profile.adapter.GroupsAllAdapter
import com.example.studentportal.ui.profile.managers.SelectedGroupsManager

class GroupsAllListFragment : Fragment() {

    private lateinit var adapter: GroupsAllAdapter
    private lateinit var allGroups: List<Group>
    private lateinit var selectedGroups: MutableList<Group>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_groups_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                systemBars.top,
                v.paddingRight,
                systemBars.bottom
            )
            insets
        }

        setupBackButton(view)
        setupRecyclerView(view)
        setupSearch(view)
    }

    override fun onResume() {
        super.onResume()
        selectedGroups = SelectedGroupsManager.getSelectedGroups(requireContext()).toMutableList()
        filterGroups("")
    }

    private fun setupBackButton(view: View) {
        val backButton = view.findViewById<ImageView>(R.id.back_to_group_settings)
        val parent = backButton.parent as ViewGroup

        parent.post {
            val hitRect = Rect()
            backButton.getHitRect(hitRect)
            val extendBy = (20 * resources.displayMetrics.density).toInt()
            hitRect.inset(-extendBy, -extendBy)
            parent.touchDelegate = TouchDelegate(hitRect, backButton)
        }

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView(view: View) {
        allGroups = GroupsRepository.groupsList
        selectedGroups = SelectedGroupsManager.getSelectedGroups(requireContext()).toMutableList()

        val recyclerView = view.findViewById<RecyclerView>(R.id.list_of_all_groups)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = GroupsAllAdapter(
            allGroups.filter { group ->
                !selectedGroups.any { it.direction == group.direction && it.group == group.group }
            }
        ) { group ->
            if (selectedGroups.any { it.direction == group.direction && it.group == group.group }) {
                Toast.makeText(requireContext(), "Эта группа уже добавлена", Toast.LENGTH_SHORT).show()
            } else {
                selectedGroups.forEach { it.isActive = false }
                val newGroup = group.copy(isActive = true)
                selectedGroups.add(newGroup)
                SelectedGroupsManager.saveSelectedGroups(requireContext(), selectedGroups)

                adapter.updateList(allGroups.filter { g ->
                    !selectedGroups.any { it.direction == g.direction && it.group == g.group }
                })

                Toast.makeText(requireContext(), "Группа добавлена", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = adapter
    }

    private fun setupSearch(view: View) {
        val searchInput = view.findViewById<EditText>(R.id.searchInput)
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