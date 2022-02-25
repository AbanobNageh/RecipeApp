package com.abanobnageh.recipeapp

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.viewModels
import cafe.adriel.voyager.navigator.Navigator
import com.abanobnageh.recipeapp.feature_recipes.screens.RecipeListScreen
import com.abanobnageh.recipeapp.ui.theme.RecipeAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val activityViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isDarkTheme by remember { mutableStateOf(activityViewModel.loadDarkTheme(this)) }

            RecipeAppTheme(darkTheme = isDarkTheme) {
                Navigator(
                    RecipeListScreen(
                        onToggleTheme = {
                            isDarkTheme = !isDarkTheme
                            activityViewModel.setIsDarkTheme(this, isDarkTheme)
                        }
                    )
                )
            }
        }
    }
}
