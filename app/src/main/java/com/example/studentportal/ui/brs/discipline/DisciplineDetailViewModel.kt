package com.example.studentportal.ui.brs.discipline

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentportal.data.model.DisciplineDetails
import com.example.studentportal.data.repository.BRSRepository
import kotlinx.coroutines.launch

class DisciplineDetailViewModel(private val context: Context) : ViewModel() {
    private val repository = BRSRepository(context)
    val disciplineDetails = MutableLiveData<DisciplineDetails>()
    val errorMessage = MutableLiveData<String>()

    fun loadDisciplineDetails(disciplineId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getDisciplineDetails(disciplineId)
                disciplineDetails.postValue(response)
            } catch (e: Exception) {
                errorMessage.postValue("Ошибка загрузки данных: ${e.message}")
            }
        }
    }
}