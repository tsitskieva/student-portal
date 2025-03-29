package com.example.studentportal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val groupContainer = findViewById<ConstraintLayout>(R.id.group_container)

        groupContainer.setOnClickListener {
            val intent = Intent(this, GroupsSettingsActivity::class.java)
            startActivity(intent)
        }

    }
}
