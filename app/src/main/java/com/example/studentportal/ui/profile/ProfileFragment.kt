package com.example.studentportal.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.example.studentportal.R
import com.example.studentportal.ui.brs.SharedViewModel
import com.example.studentportal.ui.profile.managers.SelectedBrsManager
import com.example.studentportal.ui.profile.managers.SelectedGroupsManager
import com.example.studentportal.ui.utils.InputFilterMinMax
import com.example.studentportal.ui.utils.NotificationService
import android.Manifest
import com.example.studentportal.ui.utils.NotificationsViewModel

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var emptyLessonsSwitch: Switch
    private lateinit var sharedPrefs: SharedPreferences
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var notificationsSwitch: Switch
    private lateinit var notificationsSettingsContainer: ConstraintLayout
    private lateinit var numberOfGroup: TextView
    private lateinit var numberOfBrs: TextView
    private lateinit var hoursEditText: EditText
    private lateinit var minutesEditText: EditText
    private lateinit var notificationService: NotificationService
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var compactViewSwitch: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchIndicatorBrs: Switch
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val notificationsViewModel: NotificationsViewModel by activityViewModels()
    private lateinit var beforeFirstSwitch: Switch


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
        setupIndicatorBrsSwitch()
        setupBeforeFirstSwitch()
        notificationService = NotificationService(requireContext())
        requestNotificationPermission()
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
        switchIndicatorBrs = view.findViewById(R.id.switch_indicator_brs)
        beforeFirstSwitch = view.findViewById(R.id.switch_notifications_lesson_before_first)
    }

    private fun setupSharedPreferences() {
        sharedPrefs = requireContext().getSharedPreferences(
            "AppSettings",
            Context.MODE_PRIVATE
        )

        if (!sharedPrefs.contains("notification_hours")) {
            sharedPrefs.edit()
                .putInt("notification_hours", 8)
                .putInt("notification_minutes", 0)
                .apply()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 100
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<ConstraintLayout>(R.id.group_container).setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_groupsSettings)
        }

        view.findViewById<ConstraintLayout>(R.id.brs_container).setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_brsSettings)
        }

        view.findViewById<ConstraintLayout>(R.id.errors_container).setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_reportProblem)
        }
    }

    @SuppressLint("UseKtx")
    private fun setupNotificationSwitch() {
        val isNotificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", false)
        notificationsSwitch.isChecked = isNotificationsEnabled
        notificationsSettingsContainer.visibility = if (isNotificationsEnabled) View.VISIBLE else View.GONE

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            handleNotificationsContainerVisibility(isChecked)
        }
    }

    @SuppressLint("UseKtx")
    private fun setupIndicatorBrsSwitch() {
        val sharedPrefs = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val showIndicator = sharedPrefs.getBoolean("show_indicator_brs", true)
        switchIndicatorBrs.isChecked = showIndicator

        switchIndicatorBrs.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("show_indicator_brs", isChecked).apply()
            // Уведомляем SharedViewModel о изменении
            sharedViewModel.updateIndicatorVisibility(isChecked)
        }
    }

    private fun setupTimeInputs() {
        hoursEditText.filters = arrayOf(InputFilterMinMax(0, 24))
        minutesEditText.filters = arrayOf(InputFilterMinMax(0, 60))

        // Устанавливаем значения по умолчанию (8 часов и 0 минут)
        val savedHours = sharedPrefs.getInt("notification_hours", 8) // 8 по умолчанию
        val savedMinutes = sharedPrefs.getInt("notification_minutes", 0) // 0 по умолчанию

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

            @SuppressLint("UseKtx")
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

    @SuppressLint("UseKtx")
    private fun saveCurrentTimeValues() {
        val hours = hoursEditText.text.toString().toIntOrNull() ?: 8 // 8 по умолчанию
        val minutes = minutesEditText.text.toString().toIntOrNull() ?: 0 // 0 по умолчанию

        sharedPrefs.edit()
            .putInt("notification_hours", hours)
            .putInt("notification_minutes", minutes)
            .apply()

        NotificationService(requireContext()).scheduleNotifications()
    }

    @SuppressLint("UseKtx")
    private fun setupCompactViewSwitch() {
        val isCompactView = sharedPrefs.getBoolean("compact_view_enabled", false)
        compactViewSwitch.isChecked = isCompactView

        compactViewSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("compact_view_enabled", isChecked).apply()
        }
    }

    @SuppressLint("UseKtx")
    private fun setupEmptyLessonsSwitch() {
        val showEmptyLessons = sharedPrefs.getBoolean("show_empty_lessons", false)
        emptyLessonsSwitch.isChecked = showEmptyLessons

        emptyLessonsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("show_empty_lessons", isChecked).apply()
        }
    }

    private fun setupBeforeFirstSwitch() {
        beforeFirstSwitch.isChecked = sharedPrefs.getBoolean("notify_only_before_first", false)

        beforeFirstSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("notify_only_before_first", isChecked).apply()
            notificationService.scheduleNotifications()
        }
    }
}