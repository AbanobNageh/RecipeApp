package com.abanobnageh.recipeapp.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object RecipeList

@Serializable
data class RecipeDetail(val recipeId: String)
