package com.abanobnageh.recipeapp.feature_recipes.datasources

import com.abanobnageh.recipeapp.core.error.UnknownException
import com.abanobnageh.recipeapp.core.network.RecipeRetrofitService
import com.abanobnageh.recipeapp.data.models.network.RecipeDto
import com.abanobnageh.recipeapp.data.models.network.RecipeSearchResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

interface RecipeRemoteDataSource {
    suspend fun searchRecipes(query: String, pageNumber: Int): RecipeSearchResponseDto
    suspend fun getRecipe(recipeId: Int): RecipeDto
}

class RecipeRemoteDataSourceImpl @Inject constructor(private val recipeRetrofitService: RecipeRetrofitService):
    RecipeRemoteDataSource {
    override suspend fun searchRecipes(query: String, pageNumber: Int): RecipeSearchResponseDto {
        var responseDto: RecipeSearchResponseDto? = null

        try {
            responseDto = withContext(Dispatchers.IO) { recipeRetrofitService.searchRecipe(page = pageNumber, query = query).execute().body() }
        } catch (throwable: Throwable) {
            if (throwable is IOException) {
                throw UnknownException()
            }
            else if (throwable is HttpException) {
                val errorJson: String? = withContext(Dispatchers.IO) { throwable.response()?.errorBody()?.string() }
                throw UnknownException()
            }
            else {
                throw UnknownException()
            }
        }

        return responseDto!!
    }

    override suspend fun getRecipe(recipeId: Int): RecipeDto {
        var response: RecipeDto? = null

        try {
            response = withContext(Dispatchers.IO) { recipeRetrofitService.getRecipe(id = recipeId).execute().body() }
        } catch (throwable: Throwable) {
            if (throwable is IOException) {
                throw UnknownException()
            }
            else if (throwable is HttpException) {
                val errorJson: String? = withContext(Dispatchers.IO) { throwable.response()?.errorBody()?.string() }
                throw UnknownException()
            }
            else {
                throw UnknownException()
            }
        }

        return response!!
    }
}