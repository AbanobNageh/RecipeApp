package com.abanobnageh.recipeapp.feature_recipes.repositories

import com.abanobnageh.recipeapp.core.error.Error
import com.abanobnageh.recipeapp.core.error.NoInternetError
import com.abanobnageh.recipeapp.core.error.UnknownError
import com.abanobnageh.recipeapp.core.network.NetworkInfo
import com.abanobnageh.recipeapp.core.usecase.Response
import com.abanobnageh.recipeapp.data.models.domain.Recipe
import com.abanobnageh.recipeapp.data.models.domain.RecipeSearchResponse
import com.abanobnageh.recipeapp.data.models.network.RecipeDto
import com.abanobnageh.recipeapp.data.models.network.RecipeSearchResponseDto
import com.abanobnageh.recipeapp.feature_recipes.datasources.RecipeRemoteDataSource
import javax.inject.Inject

interface RecipeRepository {
    suspend fun searchRecipes(query: String, pageNumber: Int): Response<Error, RecipeSearchResponse>
    suspend fun getRecipe(recipeId: Int): Response<Error, Recipe>
}

class RecipeRepositoryImpl @Inject constructor(val recipeRemoteDataSource: RecipeRemoteDataSource, val networkInfo: NetworkInfo): RecipeRepository {
    override suspend fun searchRecipes(query: String, pageNumber: Int): Response<Error, RecipeSearchResponse> {
        if (networkInfo.internetConnected()) {
            try {
                val recipeSearchResponse = recipeRemoteDataSource.searchRecipes(query, pageNumber)
                return Response(null, recipeSearchResponse.mapToNetworkModel())
            } catch (exception: Exception) {
                return Response(UnknownError(), null)
            }
        }
        else {
            return Response(NoInternetError(), null)
        }
    }

    override suspend fun getRecipe(recipeId: Int): Response<Error, Recipe> {
        if (networkInfo.internetConnected()) {
            try {
                val recipe = recipeRemoteDataSource.getRecipe(recipeId)
                return Response(null, recipe.mapToDomainModel())
            } catch (exception: Exception) {
                return Response(UnknownError(), null)
            }
        }
        else {
            return Response(NoInternetError(), null)
        }
    }

}