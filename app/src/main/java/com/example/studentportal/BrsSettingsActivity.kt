// BrsSettingsActivity.kt
package com.example.studentportal

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BrsSettingsActivity : ComponentActivity() {
    private lateinit var selectedBrsAdapter: SelectedBrsAdapter
    private lateinit var selectedBrs: MutableList<brs>
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateContainer: ConstraintLayout

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            updateBrsList()
            setResult(RESULT_OK)
            updateEmptyState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brs_settings)

        emptyStateContainer = findViewById(R.id.empty_state_brs_container)

        selectedBrs = SelectedBrsManager.getSelectedBrs(this).toMutableList()

        recyclerView = findViewById<RecyclerView>(R.id.list_of_choosen_brs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        selectedBrsAdapter = SelectedBrsAdapter(
            selectedBrs,
            onItemClick = { clickedBrs ->
                selectedBrs.forEach { it.isActive = false }
                clickedBrs.isActive = true
                SelectedBrsManager.saveSelectedBrs(this, selectedBrs)
                selectedBrsAdapter.updateList(selectedBrs)
                setResult(RESULT_OK)
                updateEmptyState()
            },
            onDeleteClick = { brsToDelete ->
                val isDeletingActive = brsToDelete.isActive
                selectedBrs.remove(brsToDelete)

                if (isDeletingActive && selectedBrs.isNotEmpty()) {
                    selectedBrs[0].isActive = true
                }

                SelectedBrsManager.saveSelectedBrs(this, selectedBrs)
                updateBrsList()
                setResult(RESULT_OK)
                updateEmptyState()
            }
        )

        recyclerView.adapter = selectedBrsAdapter
        selectedBrsAdapter.setupSwipeToDelete(recyclerView)

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

        val buttonToList = findViewById<Button>(R.id.button_to_list_of_brs)
        buttonToList.setOnClickListener {
            val intent = Intent(this, BrsAddActivity::class.java)
            startForResult.launch(intent)
        }
        updateEmptyState()
    }

    private fun updateBrsList() {
        selectedBrs = SelectedBrsManager.getSelectedBrs(this).toMutableList()

        if (selectedBrs.isNotEmpty() && !selectedBrs.any { it.isActive }) {
            selectedBrs[0].isActive = true
            SelectedBrsManager.saveSelectedBrs(this, selectedBrs)
        }

        selectedBrsAdapter.updateList(selectedBrs)
        updateEmptyState()
    }
    private fun updateEmptyState() {
        if (selectedBrs.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateContainer.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateContainer.visibility = View.GONE
        }
    }
}