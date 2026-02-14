package com.abanobnageh.recipeapp.data.models.domain

import java.io.Serializable

data class RecipeSearchResponse(
    val status: String?,
    val resultsCount: Int?,
    val recipes: ArrayList<RecipeSearchItem>,
): Serializable