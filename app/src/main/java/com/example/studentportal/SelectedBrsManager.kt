// SelectedBrsManager.kt
package com.example.studentportal

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SelectedBrsManager {
    private const val PREFS_NAME = "SelectedBrsPrefs"
    private const val SELECTED_BRS_KEY = "selected_brs"
    private const val ACTIVE_BRS_KEY = "active_brs"

    fun saveSelectedBrs(context: Context, brsList: List<brs>) {
        val json = Gson().toJson(brsList)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(SELECTED_BRS_KEY, json)
            .apply()

        // Сохраняем активный БРС
        val activeBrs = brsList.find { it.isActive }
        activeBrs?.let {
            saveActiveBrs(context, it.name, it.code)
        } ?: run {
            // Если нет активного БРС, но есть БРС, делаем первый активным
            if (brsList.isNotEmpty()) {
                saveActiveBrs(context, brsList[0].name, brsList[0].code)
            }
        }
    }

    fun getSelectedBrs(context: Context): List<brs> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(SELECTED_BRS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<brs>>() {}.type
            val brsList = Gson().fromJson<List<brs>>(json, type)

            // Восстанавливаем активный БРС
            val activeBrs = getActiveBrs(context)
            brsList.forEach {
                it.isActive = (it.name == activeBrs?.first && it.code == activeBrs.second)
            }

            // Если нет активного БРС, но есть БРС, делаем первый активным
            if (brsList.isNotEmpty() && !brsList.any { it.isActive }) {
                brsList[0].isActive = true
            }

            brsList.sortedByDescending { it.isActive }
        } else {
            emptyList()
        }
    }

    private fun saveActiveBrs(context: Context, name: String, code: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(ACTIVE_BRS_KEY, "$name|$code")
            .apply()
    }

    private fun getActiveBrs(context: Context): Pair<String, String>? {
        val value = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(ACTIVE_BRS_KEY, null)
        return value?.split("|")?.let {
            if (it.size == 2) Pair(it[0], it[1]) else null
        }
    }
}