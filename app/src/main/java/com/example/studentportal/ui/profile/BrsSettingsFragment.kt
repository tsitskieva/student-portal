package com.example.studentportal.ui.profile

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.R
import com.example.studentportal.data.model.Brs
import com.example.studentportal.ui.brs.SharedViewModel
import com.example.studentportal.ui.profile.adapter.SelectedBrsAdapter
import com.example.studentportal.ui.profile.managers.SelectedBrsManager

class BrsSettingsFragment : Fragment() {
    private lateinit var selectedBrsAdapter: SelectedBrsAdapter
    private lateinit var selectedBrs: MutableList<Brs>
    private lateinit var recyclerView: RecyclerView
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_brs_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedBrs = SelectedBrsManager.getSelectedBrs(requireContext()).toMutableList()

        initRecyclerView(view)
        setupBackButton(view)
        setupAddButton(view)
    }

    private fun initRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.list_of_choosen_brs)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        selectedBrsAdapter = SelectedBrsAdapter(
            selectedBrs,
            onItemClick = { clickedBrs ->
                selectedBrs.forEach { it.isActive = false }
                clickedBrs.isActive = true
                SelectedBrsManager.saveSelectedBrs(requireContext(), selectedBrs)
                selectedBrsAdapter.updateList(selectedBrs)
                requireActivity().setResult(android.app.Activity.RESULT_OK)
            },
            onDeleteClick = { brsToDelete ->
                val isDeletingActive = brsToDelete.isActive
                selectedBrs.remove(brsToDelete)

                if (isDeletingActive && selectedBrs.isNotEmpty()) {
                    selectedBrs[0].isActive = true
                }

                SelectedBrsManager.saveSelectedBrs(requireContext(), selectedBrs)
                updateBrsList()
                sharedViewModel.triggerBrsRefresh()
                requireActivity().setResult(android.app.Activity.RESULT_OK)
            }
        )

        recyclerView.adapter = selectedBrsAdapter
        selectedBrsAdapter.setupSwipeToDelete(recyclerView)
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
            requireActivity().onBackPressed()
        }
    }

    private fun setupAddButton(view: View) {
        val buttonToList = view.findViewById<Button>(R.id.button_to_list_of_brs)
        buttonToList.setOnClickListener {
            val action = BrsSettingsFragmentDirections.actionBrsSettingsToBrsAdd()
            findNavController().navigate(action)
        }
    }

    private fun updateBrsList() {
        selectedBrs = SelectedBrsManager.getSelectedBrs(requireContext()).toMutableList()

        if (selectedBrs.isNotEmpty() && !selectedBrs.any { it.isActive }) {
            selectedBrs[0].isActive = true
            SelectedBrsManager.saveSelectedBrs(requireContext(), selectedBrs)
        }

        selectedBrsAdapter.updateList(selectedBrs)
    }
}