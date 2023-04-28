package com.abanobnageh.recipeapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.abanobnageh.recipeapp.core.constants.ACTION_THEME_TOGGLED
import com.abanobnageh.recipeapp.core.theme.RecipeAppTheme
import com.abanobnageh.recipeapp.core.viewmodel.MainActivityViewModel
import com.abanobnageh.recipeapp.feature_recipes.screens.RecipeListScreen
import com.abanobnageh.recipeapp.feature_recipes.screens.RecipeScreen
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    val activityViewModel: MainActivityViewModel by viewModels()

    private val onToggleTheme = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            activityViewModel.setIsDarkTheme(this@MainActivity, !activityViewModel.isDarkTheme.value)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.registerReceiver(onToggleTheme, IntentFilter(ACTION_THEME_TOGGLED))
        val data: Uri? = intent?.data
        activityViewModel.loadDarkTheme(this)

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
        activityViewModel.loadDarkTheme(this)

        setContent {
            val isDarkTheme = activityViewModel.isDarkTheme.value

            App(
                screenList = getScreenListFromDeepLink(data),
                isDarkTheme = isDarkTheme,
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(onToggleTheme)
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

fun getScreenListFromDeepLink(deepLinkData: Uri?): List<Screen> {
    val pathSegments = deepLinkData?.pathSegments

    if (pathSegments.isNullOrEmpty()) {
        return listOf(RecipeListScreen())
    }
    else {
        val screenList = mutableListOf<Screen>()
        val primarySegment = pathSegments[0]

        when (primarySegment) {
            PrimaryPathSegment.RECIPES.segmentValue -> {
                screenList.add(RecipeListScreen())

                if (pathSegments.size == 2) {
                    val recipeId = pathSegments[1].toIntOrNull()

                    recipeId?.let {
                        screenList.add(RecipeScreen(it))
                    }
                }
            }
            else -> {
                screenList.add(RecipeListScreen())
            }
        }

        return screenList
    }
}

enum class PrimaryPathSegment(val segmentValue: String) {
    RECIPES("recipes")
}