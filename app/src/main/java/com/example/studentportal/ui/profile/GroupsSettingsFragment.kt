package com.example.studentportal.ui.profile

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.R
import com.example.studentportal.data.model.Group
import com.example.studentportal.ui.profile.adapter.SelectedGroupsAdapter
import com.example.studentportal.ui.profile.managers.SelectedGroupsManager

class GroupsSettingsFragment : Fragment() {
    private lateinit var selectedGroupsAdapter: SelectedGroupsAdapter
    private lateinit var selectedGroups: MutableList<Group>
    private lateinit var recyclerView: RecyclerView

        private val startForResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                updateGroupsList()
                requireActivity().setResult(android.app.Activity.RESULT_OK)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_groups_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedGroups = SelectedGroupsManager.getSelectedGroups(requireContext()).toMutableList()

        initRecyclerView(view)
        setupBackButton(view)
        setupAddButton(view)
    }

    private fun initRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.list_of_choosen_groups)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        selectedGroupsAdapter = SelectedGroupsAdapter(
            selectedGroups,
            onItemClick = { clickedGroup ->
                selectedGroups.forEach { it.isActive = false }
                clickedGroup.isActive = true
                SelectedGroupsManager.saveSelectedGroups(requireContext(), selectedGroups)
                selectedGroupsAdapter.updateList(selectedGroups)
                requireActivity().setResult(android.app.Activity.RESULT_OK)
            },
            onDeleteClick = { groupToDelete ->
                val isDeletingActive = groupToDelete.isActive
                selectedGroups.remove(groupToDelete)

                if (isDeletingActive && selectedGroups.isNotEmpty()) {
                    selectedGroups[0].isActive = true
                }

                SelectedGroupsManager.saveSelectedGroups(requireContext(), selectedGroups)
                updateGroupsList()
                requireActivity().setResult(android.app.Activity.RESULT_OK)
            }
        )

        recyclerView.adapter = selectedGroupsAdapter
        selectedGroupsAdapter.setupSwipeToDelete(recyclerView)
    }

    private fun setupBackButton(view: View) {
        val backButton = view.findViewById<ImageView>(R.id.back_to_all_settings)
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

    private fun setupAddButton(view: View) {
        val buttonToList = view.findViewById<Button>(R.id.button_to_list_of_groups)
        buttonToList.setOnClickListener {
            findNavController().navigate(R.id.action_groupsSettings_to_groupsAllList)
        }
    }

    private fun updateGroupsList() {
        selectedGroups = SelectedGroupsManager.getSelectedGroups(requireContext()).toMutableList()

        if (selectedGroups.isNotEmpty() && !selectedGroups.any { it.isActive }) {
            selectedGroups[0].isActive = true
            SelectedGroupsManager.saveSelectedGroups(requireContext(), selectedGroups)
        }

        selectedGroupsAdapter.updateList(selectedGroups)
    }
}