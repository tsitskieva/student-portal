package com.example.studentportal.ui.brs

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentportal.data.model.Discipline
import com.example.studentportal.data.model.Semester
import com.example.studentportal.data.repository.BRSRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.example.studentportal.ui.profile.managers.SelectedBrsManager

class BRSViewModel(private val context: Context) : ViewModel() {
    private val repository = BRSRepository(context)

    private val _semesters = MutableLiveData<List<Semester>>()
    val semesters: MutableLiveData<List<Semester>> = _semesters

    private val _spinnerPosition = MutableLiveData<Int>()
    val spinnerPosition: LiveData<Int> = _spinnerPosition

    private val _disciplines = MutableLiveData<List<Discipline>>()
    val disciplines: MutableLiveData<List<Discipline>> = _disciplines

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: MutableLiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> = _isLoading

    var selectedSemesterId: Int? = null

    private var currentDisciplineJob: Job? = null

    fun saveSpinnerPosition(position: Int) {
        _spinnerPosition.postValue(position)
    }
    private fun getActiveToken(): String {
        return SelectedBrsManager.getSelectedBrs(context)
            .find { it.isActive }
            ?.code ?: throw Exception("No active BRS selected")
    }

    suspend fun getSemestersWithData(): List<Semester> {
        val token = getActiveToken()
        val allSemesters = repository.getSemesters(token)
        val sortedSemesters = allSemesters.sortedWith(
            compareByDescending<Semester> { it.year }.thenByDescending { it.num }
        )

        return coroutineScope {
            val deferredResults = sortedSemesters.map { semester ->
                async {
                    try {
                        Pair(semester, repository.isSemesterValid(semester.id))
                    } catch (e: Exception) {
                        Pair(semester, false)
                    }
                }
            }

            deferredResults.awaitAll()
                .filter { it.second }
                .map { it.first }
        }
    }

    fun loadSemesters() {
        viewModelScope.launch {
            try {
                if (!isOnline()) {
                    _errorMessage.postValue("Нет интернет-соединения")
                    return@launch
                }


                _isLoading.postValue(true)
                val filteredSemesters = getSemestersWithData()

                _semesters.postValue(filteredSemesters)

                if (filteredSemesters.isNotEmpty()) {
                    selectedSemesterId = filteredSemesters.first().id
                    saveSpinnerPosition(0)
                    loadDisciplines()
                }

            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка загрузки семестров: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                repository.clearCache()
                loadSemesters()
                selectedSemesterId?.let { loadDisciplines() }
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка обновления: ${e.message}")
            }
        }
    }

    fun clearData() {
        repository.clearCache()
        selectedSemesterId = null
        _semesters.postValue(emptyList())
        _disciplines.postValue(emptyList())
    }

    fun loadDisciplines() {
        currentDisciplineJob?.cancel()
        selectedSemesterId?.let { semesterId ->
            _isLoading.postValue(true)
            viewModelScope.launch {
                try {
                    Log.d("BRSViewModel", "Loading disciplines for semester: $semesterId")
                    val response = repository.getDisciplines(semesterId)
                    _disciplines.postValue(response)
                    Log.d("BRSViewModel", "Disciplines loaded: ${response.size}")
                } catch (e: Exception) {
                    Log.e("BRSViewModel", "Error loading disciplines", e)
                    _errorMessage.postValue("Ошибка загрузки дисциплин: ${e.message}")
                } finally {
                    _isLoading.postValue(false)
                }
            }
        } ?: run {
            _errorMessage.postValue("Семестр не выбран")
            _isLoading.postValue(false)
        }
    }

    @Suppress("DEPRECATION")
    private fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnected == true
    }
}