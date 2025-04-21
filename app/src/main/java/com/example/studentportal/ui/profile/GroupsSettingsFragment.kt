package com.example.studentportal.ui.profile

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
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
    private lateinit var emptyStateContainer: ConstraintLayout

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            updateGroupsList()
            requireActivity().setResult(Activity.RESULT_OK)
            updateEmptyState()
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

        initViews(view)
        setupRecyclerView()
        setupButtons(view)
        updateEmptyState()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.list_of_choosen_groups)
        emptyStateContainer = view.findViewById(R.id.empty_state_group_container)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        selectedGroupsAdapter = SelectedGroupsAdapter(
            selectedGroups,
            onItemClick = ::handleGroupSelection,
            onDeleteClick = ::handleGroupDeletion
        )

        recyclerView.adapter = selectedGroupsAdapter
        selectedGroupsAdapter.setupSwipeToDelete(recyclerView)
    }

    private fun handleGroupSelection(clickedGroup: Group) {
        selectedGroups.forEach { it.isActive = false }
        clickedGroup.isActive = true

        selectedGroupsAdapter.updateList(selectedGroups)
        SelectedGroupsManager.saveSelectedGroups(requireContext(), selectedGroups)
        requireActivity().setResult(Activity.RESULT_OK)
        updateEmptyState()
        notifyActiveGroupChanged()
    }

    private fun handleGroupDeletion(groupToDelete: Group) {
        val wasActive = groupToDelete.isActive
        selectedGroups.remove(groupToDelete)

        if (wasActive && selectedGroups.isNotEmpty()) {
            selectedGroups[0].isActive = true
        }

        selectedGroupsAdapter.updateList(selectedGroups)
        SelectedGroupsManager.saveSelectedGroups(requireContext(), selectedGroups)
        requireActivity().setResult(Activity.RESULT_OK)
        updateEmptyState()
        notifyActiveGroupChanged()
    }

    private fun notifyActiveGroupChanged() {
        parentFragmentManager.setFragmentResult(
            "active_group_changed",
            bundleOf("group_changed" to true)
        )
    }

    private fun setupButtons(view: View) {
        setupBackButton(view)
        setupAddButton(view)
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
        view.findViewById<Button>(R.id.button_to_list_of_groups).setOnClickListener {
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
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (selectedGroups.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateContainer.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateContainer.visibility = View.GONE
        }
    }
}