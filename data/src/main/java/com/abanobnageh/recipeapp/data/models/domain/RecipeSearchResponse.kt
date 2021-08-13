package com.abanobnageh.recipeapp.data.models.domain

data class RecipeSearchResponse(
    val count: Int?,
    val next: String?,
    val previous: String?,
    val results: ArrayList<Recipe>,
)