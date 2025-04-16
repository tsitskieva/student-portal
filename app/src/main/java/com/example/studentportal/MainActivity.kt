package com.example.studentportal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Switch
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
            updateBrsCount(findViewById(R.id.number_of_brs))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val hoursEditText = findViewById<EditText>(R.id.hours_before_notification_lesson)
        val minutesEditText = findViewById<EditText>(R.id.minutes_before_notification_lesson)
        val groupContainer = findViewById<ConstraintLayout>(R.id.group_container)
        val numberOfGroup = findViewById<TextView>(R.id.number_of_group)

        updateGroupsCount(numberOfGroup)

        groupContainer.setOnClickListener {
            val intent = Intent(this, GroupsSettingsActivity::class.java)
            startForResult.launch(intent)
        }

        val brsContainer = findViewById<ConstraintLayout>(R.id.brs_container)
        val numberOfBrs = findViewById<TextView>(R.id.number_of_brs)

        updateBrsCount(numberOfBrs)

        brsContainer.setOnClickListener {
            val intent = Intent(this, BrsSettingsActivity::class.java)
            startForResult.launch(intent)
        }

        // Получаем Switch и контейнер настроек
        val notificationsSwitch = findViewById<Switch>(R.id.switch_notifications_lesson)
        val notificationsSettingsContainer = findViewById<ConstraintLayout>(R.id.notifications_lesson_container_settings)

        // Загружаем сохранённое состояние (по умолчанию false)
        val sharedPrefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val isNotificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", false)
        notificationsSwitch.isChecked = isNotificationsEnabled

        // Устанавливаем видимость контейнера в зависимости от состояния Switch
        notificationsSettingsContainer.visibility = if (isNotificationsEnabled) View.VISIBLE else View.GONE

        // Обработка изменений Switch
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Сохраняем новое состояние
            sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()

            // Меняем видимость контейнера
            notificationsSettingsContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()

            if (isChecked) {
                notificationsSettingsContainer.visibility = View.VISIBLE
                notificationsSettingsContainer.alpha = 0f
                notificationsSettingsContainer.animate().alpha(1f).setDuration(250).start()
            } else {
                notificationsSettingsContainer.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction { notificationsSettingsContainer.visibility = View.GONE }
                    .start()
            }
        }

        hoursEditText.filters = arrayOf(InputFilterMinMax(0, 24))
        minutesEditText.filters = arrayOf(InputFilterMinMax(0, 60))

        // Для часов
        hoursEditText.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return

                if (s.isNullOrEmpty()) {
                    isEditing = true
                    hoursEditText.setText("0")
                    hoursEditText.setSelection(1)
                    isEditing = false
                } else if (s.toString() == "0") {
                    // Оставляем "0", но сохраняем
                } else if (s.toString().startsWith("0") && s.length > 1) {
                    // Удаляем ведущий ноль
                    isEditing = true
                    hoursEditText.setText(s.toString().substring(1))
                    hoursEditText.setSelection(hoursEditText.text.length)
                    isEditing = false
                }

                // Сохраняем значение при любом изменении
                val value = hoursEditText.text.toString().toIntOrNull() ?: 0
                getSharedPreferences("AppSettings", MODE_PRIVATE)
                    .edit()
                    .putInt("notification_hours", value)
                    .apply()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

// Для минут (аналогично)
        minutesEditText.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return

                if (s.isNullOrEmpty()) {
                    isEditing = true
                    minutesEditText.setText("0")
                    minutesEditText.setSelection(1)
                    isEditing = false
                } else if (s.toString() == "0") {
                    // Оставляем "0"
                } else if (s.toString().startsWith("0") && s.length > 1) {
                    // Удаляем ведущий ноль
                    isEditing = true
                    minutesEditText.setText(s.toString().substring(1))
                    minutesEditText.setSelection(minutesEditText.text.length)
                    isEditing = false
                }

                // Сохраняем значение при любом изменении
                val value = minutesEditText.text.toString().toIntOrNull() ?: 0
                getSharedPreferences("AppSettings", MODE_PRIVATE)
                    .edit()
                    .putInt("notification_minutes", value)
                    .apply()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Загружаем сохранённые значения или устанавливаем 0 по умолчанию
        val savedHours = sharedPrefs.getInt("notification_hours", 0)
        val savedMinutes = sharedPrefs.getInt("notification_minutes", 0)
        hoursEditText.setText(savedHours.toString())
        minutesEditText.setText(savedMinutes.toString())

        // Сохраняем значения при изменении
        hoursEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val value = hoursEditText.text.toString().toIntOrNull() ?: 0
                sharedPrefs.edit().putInt("notification_hours", value).apply()
            }
        }

        minutesEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val value = minutesEditText.text.toString().toIntOrNull() ?: 0
                sharedPrefs.edit().putInt("notification_minutes", value).apply()
            }
        }
    }

    private fun updateGroupsCount(textView: TextView) {
        val count = SelectedGroupsManager.getSelectedGroups(this).size
        textView.text = count.toString()
    }

    private fun updateBrsCount(textView: TextView) {
        val count = SelectedBrsManager.getSelectedBrs(this).size
        textView.text = count.toString()
    }

    companion object {
        private const val GROUPS_SETTINGS_REQUEST_CODE = 1001
    }
}