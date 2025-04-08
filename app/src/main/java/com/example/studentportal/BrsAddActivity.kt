// BrsAddActivity.kt
package com.example.studentportal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class BrsAddActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_brs_all)

        val backButton = findViewById<ImageView>(R.id.back_to_group_settings)
        backButton.setOnClickListener {
            finish()
        }

        val nameInput = findViewById<EditText>(R.id.name_of_brs)
        val codeInput = findViewById<EditText>(R.id.code_of_brs)
        val addButton = findViewById<Button>(R.id.button_to_list_of_brs)

        addButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val code = codeInput.text.toString().trim()

            if (name.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedBrs = SelectedBrsManager.getSelectedBrs(this)
            if (selectedBrs.any { it.code == code }) {
                Toast.makeText(this, "Этот код БРС уже добавлен", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newBrs = brs(name, code, isActive = selectedBrs.isEmpty())
            val updatedList = selectedBrs.toMutableList().apply { add(newBrs) }
            SelectedBrsManager.saveSelectedBrs(this, updatedList)

            setResult(RESULT_OK)
            finish()
        }
    }
}