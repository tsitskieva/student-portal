package com.example.studentportal

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val groupContainer = findViewById<ConstraintLayout>(R.id.group_container)
        val numberOfGroup = findViewById<TextView>(R.id.number_of_group)

        // Первоначальное обновление счетчика
        updateGroupsCount(numberOfGroup)

        groupContainer.setOnClickListener {
            val intent = Intent(this, GroupsSettingsActivity::class.java)
            startActivityForResult(intent, GROUPS_SETTINGS_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GROUPS_SETTINGS_REQUEST_CODE) {
            // Обновляем счетчик после возврата из GroupsSettingsActivity
            updateGroupsCount(findViewById(R.id.number_of_group))
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
