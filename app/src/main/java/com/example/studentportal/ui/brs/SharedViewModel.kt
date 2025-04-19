package com.example.studentportal.ui.brs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _indicatorVisibility = MutableLiveData<Boolean>()
    val indicatorVisibility: LiveData<Boolean> = _indicatorVisibility
    private val _refreshBrs = MutableLiveData<Boolean>()
    val refreshBrs: LiveData<Boolean> = _refreshBrs

    fun updateIndicatorVisibility(visible: Boolean) {
        _indicatorVisibility.value = visible
    }

    fun triggerBrsRefresh() {
        _refreshBrs.value = true
    }
}