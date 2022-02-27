package com.abanobnageh.recipeapp.core.viewmodel

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

const val PREFERENCE_FILE_KEY = "com.abanobnageh.recipeapp.PREFERENCE_FILE"
const val PREFERENCE_DARK_THEME_KEY = "dark_theme"

@HiltViewModel
class MainActivityViewModel @Inject constructor() : ViewModel() {
    val isDarkTheme: MutableState<Boolean> = mutableStateOf(false)

    fun setIsDarkTheme(activity: Activity?, isDarkTheme: Boolean) {
        val sharedPref = activity?.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE) ?: return

        with (sharedPref.edit()) {
            putBoolean(PREFERENCE_DARK_THEME_KEY, isDarkTheme)
            apply()
        }
        this.isDarkTheme.value = isDarkTheme
    }

    fun loadDarkTheme(activity: Activity?) {
        val sharedPref = activity?.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        isDarkTheme.value = sharedPref?.getBoolean(PREFERENCE_DARK_THEME_KEY, false) ?: false
    }
}