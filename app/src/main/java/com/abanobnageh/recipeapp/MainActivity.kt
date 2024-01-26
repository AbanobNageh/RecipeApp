package com.abanobnageh.recipeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.abanobnageh.recipeapp.core.theme.RecipeAppTheme
import com.abanobnageh.recipeapp.core.viewmodel.MainActivityViewModel
import com.abanobnageh.recipeapp.utils.getScreenListFromDeepLink
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterialApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val activityViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data: Uri? = intent?.data

        setContent {
            val isDarkTheme = activityViewModel.isDarkTheme.value

            App(
                screenList = getScreenListFromDeepLink(data),
                isDarkTheme = isDarkTheme,
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val data: Uri? = intent?.data

        setContent {
            val isDarkTheme = activityViewModel.isDarkTheme.value

            App(
                screenList = getScreenListFromDeepLink(data),
                isDarkTheme = isDarkTheme,
            )
        }
    }
}

@Composable
fun App(
    screenList: List<Screen>,
    isDarkTheme: Boolean = false,
) {
    RecipeAppTheme(darkTheme = isDarkTheme) {
        Navigator(
            screenList
        )
    }
}