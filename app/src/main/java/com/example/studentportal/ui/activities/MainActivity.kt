package com.example.studentportal.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.studentportal.R
import com.example.studentportal.ui.utils.NotificationService
import com.example.studentportal.ui.utils.NotificationsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import androidx.preference.PreferenceManager
import com.example.studentportal.ui.brs.BrsCheckWorker

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView
    private val notificationsViewModel: NotificationsViewModel by viewModels()

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (intent?.getBooleanExtra("OPEN_SCHEDULE_FRAGMENT", false) == true) {
            val navView = findViewById<BottomNavigationView>(R.id.bottom_nav)
            navView.selectedItemId = R.id.nav_schedule
        }

        NotificationService(this).scheduleNotifications()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        bottomNav = findViewById(R.id.bottom_nav)
        setupNavigation()

        notificationsViewModel.settingsChanged.observe(this) {
            NotificationService(this).scheduleNotifications()
        }

    }

    fun scheduleBrsCheck() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val intervalHours = sharedPrefs.getInt("brs_check_interval", 3).toLong()

        val workRequest = PeriodicWorkRequestBuilder<BrsCheckWorker>(
            intervalHours, TimeUnit.HOURS
        )
            .setInitialDelay(10, TimeUnit.MINUTES) // Добавьте начальную задержку
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "brsCheckWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun setupNavigation() {
        // Настройка отступов для иконок
        bottomNav.menu.forEach { item ->
            val iconSize = resources.getDimensionPixelSize(R.dimen.nav_icon_size)
            val params = FrameLayout.LayoutParams(iconSize, iconSize)
            params.gravity = Gravity.CENTER
            item.actionView?.layoutParams = params
        }

        bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.disciplineDetailFragment -> bottomNav.visibility = View.GONE
                else -> bottomNav.visibility = View.VISIBLE
            }
        }
    }
}