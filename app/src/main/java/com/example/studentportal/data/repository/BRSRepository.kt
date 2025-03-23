package com.example.studentportal.data.repository

import android.content.Context
import com.example.studentportal.data.model.Discipline
import com.example.studentportal.data.model.DisciplineDetails
import com.example.studentportal.data.model.Module
import com.example.studentportal.data.model.Semester
import com.example.studentportal.data.model.Submodule
import com.example.studentportal.network.BRSApiService
import com.example.studentportal.network.RetrofitClient
import com.example.studentportal.network.response.DisciplineDetailsResponse
import com.example.studentportal.ui.utils.TokenManager
import retrofit2.HttpException

class BRSRepository(private val context: Context) {
    private val apiService: BRSApiService =
        RetrofitClient.instance.create(BRSApiService::class.java)
    private val cache = mutableMapOf<Int, List<Discipline>>()
    private var cachedSemesters: List<Semester> = emptyList()
    private val semesterValidityCache = mutableMapOf<Int, Boolean>()

    private fun getSavedToken(): String {
        return TokenManager.getToken(context) ?: throw Exception("Not authenticated")
    }

    suspend fun getSemesters(): List<Semester> {
        return if (cachedSemesters.isEmpty()) {
            val token = getSavedToken()
            val response = apiService.getSemesters(token)
            cachedSemesters = response.semesters
            semesterValidityCache.clear()
            cachedSemesters
        } else {
            cachedSemesters
        }
    }

    suspend fun isSemesterValid(semesterId: Int): Boolean {
        return semesterValidityCache[semesterId] ?: run {
            val result = try {
                loadFromNetwork(semesterId).isNotEmpty()
            } catch (e: Exception) {
                false
            }
            semesterValidityCache[semesterId] = result
            result
        }
    }

    suspend fun login(login: String, password: String): String {
        val response = apiService.login(login, password)
        if (!response.response.success) throw Exception("Ошибка авторизации")
        return response.response.token
    }

    suspend fun getDisciplines(semesterId: Int): List<Discipline> {
        return cache[semesterId] ?: loadFromNetwork(semesterId).also {
            cache[semesterId] = it
        }
    }

    private suspend fun loadFromNetwork(semesterId: Int): List<Discipline> {
        return try {
            val token = getSavedToken()
            val response = apiService.getDisciplines(token, semesterId)
            response.mappedDisciplines
        } catch (e: HttpException) {
            if (e.code() == 401) {
                cache.clear()
                TokenManager.saveToken(context, "")
                throw Exception("Сессия истекла")
            }
            throw e
        }
    }

    fun clearCache() {
        cachedSemesters = emptyList()
        cache.clear()
    }

    suspend fun getDisciplineDetails(disciplineId: Int): DisciplineDetails {
        val token = getSavedToken()
        val response = apiService.getDisciplineDetails(token, disciplineId)
        return parseDetails(response)
    }

    private fun parseDetails(response: DisciplineDetailsResponse): DisciplineDetails {
        val discipline = response.response.Discipline
        val submodules = response.response.Submodules
        val disciplineMap = response.response.DisciplineMap

        val examSubmoduleId = disciplineMap?.Exam
        var examModule: Module? = null

        val (totalScore, maxScore) = if (disciplineMap == null) {
            Pair(
                discipline.Rate?.toIntOrNull() ?: 0,
                discipline.MaxCurrentRate?.toIntOrNull() ?: 0
            )
        } else {
            val allSubmoduleIds = mutableListOf<Int>().apply {
                addAll(disciplineMap.Modules?.values?.flatMap { it.Submodules } ?: emptyList())
                examSubmoduleId?.let { add(it) }
            }

            val relevantSubmodules = submodules.filterKeys { key ->
                allSubmoduleIds.contains(key.toIntOrNull())
            }.values

            Pair(
                relevantSubmodules.sumOf { it.Rate ?: 0 },
                relevantSubmodules.sumOf { it.MaxRate ?: 0 }
            )
        }

        val modules = if (disciplineMap == null) {
            listOf(
                Module(
                    title = "Информация",
                    submodules = listOf(
                        Submodule(
                            title = "Эта дисциплина еще не заполнена",
                            score = 0,
                            maxScore = 0
                        )
                    )
                )
            )
        } else {
            val modulesList = disciplineMap.Modules?.map { (_, module) ->
                Module(
                    title = module.Title,
                    submodules = module.Submodules.mapNotNull { subId ->
                        submodules[subId.toString()]?.let {
                            Submodule(
                                title = it.Title,
                                score = it.Rate ?: 0,
                                maxScore = it.MaxRate ?: 0
                            )
                        }
                    }
                )
            }?.toMutableList() ?: mutableListOf()

            examSubmoduleId?.let { examId ->
                submodules[examId.toString()]?.let { examSubmodule ->
                    examModule = Module(
                        title = "Экзамен",
                        submodules = listOf(
                            Submodule(
                                title = examSubmodule.Title,
                                score = examSubmodule.Rate ?: 0,
                                maxScore = examSubmodule.MaxRate ?: 0
                            )
                        ),
                        type = "EXAM"
                    )
                }
            }

            modulesList
        }

        return DisciplineDetails(
            id = discipline.ID,
            name = discipline.SubjectName,
            score = totalScore,
            maxScore = maxScore,
            modules = modules,
            exam = examModule,
            isEmpty = disciplineMap == null
        )
    }
}