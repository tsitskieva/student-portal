package com.example.studentportal.ui.profile

import android.app.Activity.RESULT_OK
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.studentportal.R
import com.example.studentportal.ui.profile.managers.SelectedBrsManager
import com.example.studentportal.ui.profile.managers.SelectedGroupsManager

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var notificationsSwitch: Switch
    private lateinit var notificationsSettingsContainer: ConstraintLayout
    private lateinit var numberOfGroup: TextView
    private lateinit var numberOfBrs: TextView

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        updateCounters()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefs = requireContext().getSharedPreferences(
            "AppSettings",
            android.content.Context.MODE_PRIVATE
        )

        // Инициализация элементов
        numberOfGroup = view.findViewById(R.id.number_of_group)
        numberOfBrs = view.findViewById(R.id.number_of_brs)
        notificationsSwitch = view.findViewById(R.id.switch_notifications_lesson)
        notificationsSettingsContainer =
            view.findViewById(R.id.notifications_lesson_container_settings)

        // Настройка групп
        view.findViewById<ConstraintLayout>(R.id.group_container).setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_groupsSettings)
        }

        // Настройка БРС
        view.findViewById<ConstraintLayout>(R.id.brs_container).setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_brsSettings)
        }

        // Загрузка состояния уведомлений
        val isNotificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", false)
        notificationsSwitch.isChecked = isNotificationsEnabled
        notificationsSettingsContainer.visibility =
            if (isNotificationsEnabled) View.VISIBLE else View.GONE

        // Обработчик переключателя
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            handleNotificationsContainerVisibility(isChecked)
        }

        updateCounters()
    }

    override fun onResume() {
        super.onResume()
        updateCounters()
    }

    private fun updateCounters() {
        // Обновление счетчиков групп и БРС
        updateGroupsCount()
        updateBrsCount()
    }

    private fun updateGroupsCount() {
        val count = SelectedGroupsManager.getSelectedGroups(requireContext()).size
        numberOfGroup.text = count.toString()
    }

    private fun updateBrsCount() {
        val count = SelectedBrsManager.getSelectedBrs(requireContext()).size
        numberOfBrs.text = count.toString()
    }

    private fun handleNotificationsContainerVisibility(isEnabled: Boolean) {
        if (isEnabled) {
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