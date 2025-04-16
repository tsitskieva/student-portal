package com.example.studentportal.ui.brs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _refreshBrs = MutableLiveData<Boolean>()
    val refreshBrs: LiveData<Boolean> = _refreshBrs

    fun triggerBrsRefresh() {
        _refreshBrs.value = true
    }
}