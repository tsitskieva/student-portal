package com.example.studentportal

import android.content.Intent
import android.os.Bundle
import android.view.View
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