package com.example.studentportal.ui.profile

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.example.studentportal.R
import com.example.studentportal.ui.profile.managers.SelectedBrsManager
import com.example.studentportal.ui.profile.managers.SelectedGroupsManager
import com.example.studentportal.ui.utils.InputFilterMinMax

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private lateinit var emptyLessonsSwitch: Switch
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var notificationsSwitch: Switch
    private lateinit var notificationsSettingsContainer: ConstraintLayout
    private lateinit var numberOfGroup: TextView
    private lateinit var numberOfBrs: TextView
    private lateinit var hoursEditText: EditText
    private lateinit var minutesEditText: EditText
    private lateinit var compactViewSwitch: Switch

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        updateCounters()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupSharedPreferences()
        setupClickListeners(view)
        setupNotificationSwitch()
        setupTimeInputs()
        setupFragmentResultListeners()
        updateCounters()
        setupCompactViewSwitch()
        setupEmptyLessonsSwitch()
    }

    private fun initializeViews(view: View) {
        emptyLessonsSwitch = view.findViewById(R.id.switch_empty_lesson)
        compactViewSwitch = view.findViewById(R.id.switch_small_lesson)
        numberOfGroup = view.findViewById(R.id.number_of_group)
        numberOfBrs = view.findViewById(R.id.number_of_brs)
        notificationsSwitch = view.findViewById(R.id.switch_notifications_lesson)
        notificationsSettingsContainer = view.findViewById(R.id.notifications_lesson_container_settings)
        hoursEditText = view.findViewById(R.id.hours_before_notification_lesson)
        minutesEditText = view.findViewById(R.id.minutes_before_notification_lesson)
    }

    private fun setupSharedPreferences() {
        sharedPrefs = requireContext().getSharedPreferences(
            "AppSettings",
            android.content.Context.MODE_PRIVATE
        )
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<ConstraintLayout>(R.id.group_container).setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_groupsSettings)
        }

        view.findViewById<ConstraintLayout>(R.id.brs_container).setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_brsSettings)
        }
    }

    private fun setupNotificationSwitch() {
        val isNotificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", false)
        notificationsSwitch.isChecked = isNotificationsEnabled
        notificationsSettingsContainer.visibility = if (isNotificationsEnabled) View.VISIBLE else View.GONE

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            handleNotificationsContainerVisibility(isChecked)
        }
    }

    private fun setupTimeInputs() {
        hoursEditText.filters = arrayOf(InputFilterMinMax(0, 24))
        minutesEditText.filters = arrayOf(InputFilterMinMax(0, 60))

        val savedHours = sharedPrefs.getInt("notification_hours", 0)
        val savedMinutes = sharedPrefs.getInt("notification_minutes", 0)
        hoursEditText.setText(savedHours.toString())
        minutesEditText.setText(savedMinutes.toString())

        setupTextWatchers()
    }

    private fun setupTextWatchers() {
        hoursEditText.addTextChangedListener(createTimeTextWatcher("notification_hours"))
        minutesEditText.addTextChangedListener(createTimeTextWatcher("notification_minutes"))
    }

    private fun createTimeTextWatcher(preferenceKey: String): TextWatcher {
        return object : TextWatcher {
            private var isEditing = false

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                val value = s.toString().toIntOrNull() ?: 0

                isEditing = true
                when {
                    s.isNullOrEmpty() -> {
                        s?.append("0")
                        s?.let { updateSelection(it) }
                    }
                    s.startsWith("0") && s.length > 1 -> {
                        s.delete(0, 1)
                        updateSelection(s)
                    }
                }
                isEditing = false

                sharedPrefs.edit().putInt(preferenceKey, value).apply()
            }

            private fun updateSelection(s: Editable) {
                when (s) {
                    hoursEditText.text -> hoursEditText.setSelection(s.length)
                    minutesEditText.text -> minutesEditText.setSelection(s.length)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    private fun setupFragmentResultListeners() {
        setFragmentResultListener("groups_update") { _, _ ->
            updateGroupsCount()
        }

        setFragmentResultListener("brs_update") { _, _ ->
            updateBrsCount()
        }
    }

    private fun updateCounters() {
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

    override fun onPause() {
        super.onPause()
        saveCurrentTimeValues()
    }

    private fun saveCurrentTimeValues() {
        val hours = hoursEditText.text.toString().toIntOrNull() ?: 0
        val minutes = minutesEditText.text.toString().toIntOrNull() ?: 0

        sharedPrefs.edit()
            .putInt("notification_hours", hours)
            .putInt("notification_minutes", minutes)
            .apply()
    }

    private fun setupCompactViewSwitch() {
        val isCompactView = sharedPrefs.getBoolean("compact_view_enabled", false)
        compactViewSwitch.isChecked = isCompactView

        compactViewSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("compact_view_enabled", isChecked).apply()
        }
    }

    private fun setupEmptyLessonsSwitch() {
        val showEmptyLessons = sharedPrefs.getBoolean("show_empty_lessons", false)
        emptyLessonsSwitch.isChecked = showEmptyLessons

        emptyLessonsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("show_empty_lessons", isChecked).apply()
        }
    }
}