package com.abanobnageh.recipeapp.feature_recipes.repositories

import com.abanobnageh.recipeapp.core.error.Error
import com.abanobnageh.recipeapp.core.error.NoInternetError
import com.abanobnageh.recipeapp.core.error.UnknownError
import com.abanobnageh.recipeapp.core.network.NetworkInfo
import com.abanobnageh.recipeapp.core.usecase.Response
import com.abanobnageh.recipeapp.data.models.domain.Recipe
import com.abanobnageh.recipeapp.data.models.domain.RecipeSearchResponse
import com.abanobnageh.recipeapp.feature_recipes.datasources.RecipeRemoteDataSource
import javax.inject.Inject

interface RecipeRepository {
    suspend fun searchRecipes(query: String): Response<Error, RecipeSearchResponse>
    suspend fun getRecipe(recipeId: String): Response<Error, Recipe>
}

class RecipeRepositoryImpl @Inject constructor(val recipeRemoteDataSource: RecipeRemoteDataSource, val networkInfo: NetworkInfo): RecipeRepository {
    override suspend fun searchRecipes(query: String): Response<Error, RecipeSearchResponse> {
        return if (networkInfo.isInternetConnected()) {
            try {
                val recipeSearchResponse = recipeRemoteDataSource.searchRecipes(query)
                Response(null, recipeSearchResponse.mapToDomainModel())
            } catch (exception: Exception) {
                Response(UnknownError(), null)
            }
        } else {
            Response(NoInternetError(), null)
        }
    }

    override suspend fun getRecipe(recipeId: String): Response<Error, Recipe> {
        return if (networkInfo.isInternetConnected()) {
            try {
                val recipe = recipeRemoteDataSource.getRecipe(recipeId)
                Response(null, recipe.mapToDomainModel())
            } catch (exception: Exception) {
                Response(UnknownError(), null)
            }
        } else {
            Response(NoInternetError(), null)
        }
    }

}