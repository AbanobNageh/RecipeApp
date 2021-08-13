package com.abanobnageh.recipeapp.feature_recipes.usecases

import com.abanobnageh.recipeapp.core.usecase.Response
import com.abanobnageh.recipeapp.core.usecase.Usecase
import com.abanobnageh.recipeapp.data.models.domain.RecipeSearchResponse
import com.abanobnageh.recipeapp.feature_recipes.repositories.RecipeRepository
import com.abanobnageh.recipeapp.core.error.Error
import javax.inject.Inject

class SearchRecipes @Inject constructor(val recipeRepository: RecipeRepository): Usecase<RecipeSearchResponse, SearchRecipesParams> {
    override suspend fun call(params: SearchRecipesParams): Response<Error, RecipeSearchResponse> {
        return recipeRepository.searchRecipes(params.query, params.pageNumber)
    }
}

class SearchRecipesParams(
    val query: String,
    val pageNumber: Int,
)