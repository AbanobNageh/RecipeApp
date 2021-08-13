package com.abanobnageh.recipeapp.feature_recipes.usecases

import com.abanobnageh.recipeapp.core.error.Error
import com.abanobnageh.recipeapp.core.usecase.Response
import com.abanobnageh.recipeapp.core.usecase.Usecase
import com.abanobnageh.recipeapp.data.models.domain.Recipe
import com.abanobnageh.recipeapp.feature_recipes.repositories.RecipeRepository

class GetRecipe(val recipeRepository: RecipeRepository): Usecase<Recipe, GetRecipeParams> {
    override suspend fun call(params: GetRecipeParams): Response<Error, Recipe> {
        return recipeRepository.getRecipe(params.recipeId)
    }
}

class GetRecipeParams(
    val recipeId: Int
)