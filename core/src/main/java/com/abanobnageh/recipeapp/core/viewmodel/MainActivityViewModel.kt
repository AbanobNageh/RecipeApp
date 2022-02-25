package com.abanobnageh.recipeapp.core.viewmodel

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

const val PREFERENCE_FILE_KEY = "com.abanobnageh.recipeapp.PREFERENCE_FILE"
const val PREFERENCE_DARK_THEME_KEY = "dark_theme"

@HiltViewModel
class MainActivityViewModel @Inject constructor() : ViewModel() {

    fun setIsDarkTheme(activity: Activity?, isDarkTheme: Boolean) {
        val sharedPref = activity?.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE) ?: return

        with (sharedPref.edit()) {
            putBoolean(PREFERENCE_DARK_THEME_KEY, isDarkTheme)
            apply()
        }
    }

    fun loadDarkTheme(activity: Activity?): Boolean {
        val sharedPref = activity?.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        return sharedPref?.getBoolean(PREFERENCE_DARK_THEME_KEY, false) ?: false
    }
}