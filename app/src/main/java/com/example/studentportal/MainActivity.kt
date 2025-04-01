package com.example.studentportal

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : ComponentActivity() {
    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            updateGroupsCount(findViewById(R.id.number_of_group))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val groupContainer = findViewById<ConstraintLayout>(R.id.group_container)
        val numberOfGroup = findViewById<TextView>(R.id.number_of_group)

        updateGroupsCount(numberOfGroup)

        groupContainer.setOnClickListener {
            val intent = Intent(this, GroupsSettingsActivity::class.java)
            startForResult.launch(intent)
        }
    }

    private fun updateGroupsCount(textView: TextView) {
        val count = SelectedGroupsManager.getSelectedGroups(this).size
        textView.text = count.toString()
    }

    companion object {
        private const val GROUPS_SETTINGS_REQUEST_CODE = 1001
    }
}