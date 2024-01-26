package com.abanobnageh.recipeapp.core.viewmodel

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

const val PREFERENCE_FILE_KEY = "com.abanobnageh.recipeapp.PREFERENCE_FILE"
const val PREFERENCE_DARK_THEME_KEY = "dark_theme"

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val application: Application) : AndroidViewModel(application) {
    val isDarkTheme: MutableState<Boolean> = mutableStateOf(false)

    init {
        loadDarkTheme()
    }

    fun toggleDarkTheme() {
        setIsDarkTheme(!isDarkTheme.value)
    }

    private fun setIsDarkTheme(isDarkTheme: Boolean) {
        val sharedPref = application.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE) ?: return

        with (sharedPref.edit()) {
            putBoolean(PREFERENCE_DARK_THEME_KEY, isDarkTheme)
            apply()
        }
        this.isDarkTheme.value = isDarkTheme
    }

    private fun loadDarkTheme() {
        val isSystemInDarkTheme = isSystemInDarkTheme()
        val sharedPref = application.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        isDarkTheme.value = sharedPref?.getBoolean(PREFERENCE_DARK_THEME_KEY, isSystemInDarkTheme) ?: isSystemInDarkTheme
    }

    private fun isSystemInDarkTheme(): Boolean {
        return application.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }
}