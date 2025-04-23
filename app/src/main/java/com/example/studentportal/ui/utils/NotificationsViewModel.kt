package com.example.studentportal.ui.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData

class NotificationsViewModel : ViewModel() {
    private val _settingsChanged = MutableLiveData<Unit>()
    val settingsChanged: LiveData<Unit> = _settingsChanged

    fun notifySettingsChanged() {
        _settingsChanged.postValue(Unit)
    }
}