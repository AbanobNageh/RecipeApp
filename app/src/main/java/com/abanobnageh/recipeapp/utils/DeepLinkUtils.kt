package com.abanobnageh.recipeapp.utils

import android.net.Uri
import androidx.compose.material.ExperimentalMaterialApi
import cafe.adriel.voyager.core.screen.Screen
import com.abanobnageh.recipeapp.feature_recipes.screens.RecipeListScreen
import com.abanobnageh.recipeapp.feature_recipes.screens.RecipeScreen

@ExperimentalMaterialApi
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