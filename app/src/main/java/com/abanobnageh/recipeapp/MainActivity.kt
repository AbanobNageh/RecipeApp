package com.abanobnageh.recipeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.abanobnageh.recipeapp.core.navigation.RecipeDetail
import com.abanobnageh.recipeapp.core.navigation.RecipeList
import com.abanobnageh.recipeapp.core.theme.RecipeAppTheme
import com.abanobnageh.recipeapp.core.viewmodel.MainActivityViewModel
import com.abanobnageh.recipeapp.feature_recipes.screens.RecipeListScreen
import com.abanobnageh.recipeapp.feature_recipes.screens.RecipeScreen
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
                initialData = data,
                isDarkTheme = isDarkTheme,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Handle deep link if needed when activity is already running
        // For simplicity in this migration, we are just resetting the content
        val data: Uri? = intent.data

        setContent {
            val isDarkTheme = activityViewModel.isDarkTheme.value

            App(
                initialData = data,
                isDarkTheme = isDarkTheme,
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun App(
    initialData: Uri?,
    isDarkTheme: Boolean = false,
) {
    val navController = rememberNavController()

    RecipeAppTheme(darkTheme = isDarkTheme) {
        NavHost(
            navController = navController,
            startDestination = RecipeList
        ) {
            composable<RecipeList> {
                RecipeListScreen(
                    openRecipeScreen = { recipeId ->
                        navController.navigate(RecipeDetail(recipeId = recipeId))
                    }
                )
            }
            composable<RecipeDetail> { backStackEntry ->
                val detail: RecipeDetail = backStackEntry.toRoute()
                RecipeScreen(recipeId = detail.recipeId)
            }
        }
    }

    // Basic DeepLink handling for migration
    LaunchedEffect(initialData) {
        initialData?.let { uri ->
            val pathSegments = uri.pathSegments
            if (pathSegments.isNotEmpty() && pathSegments[0] == "recipes") {
                if (pathSegments.size == 2) {
                    val recipeId = pathSegments[1]
                    navController.navigate(RecipeDetail(recipeId = recipeId))
                }
            }
        }
    }
}
