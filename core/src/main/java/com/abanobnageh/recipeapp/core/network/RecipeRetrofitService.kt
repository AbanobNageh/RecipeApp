package com.abanobnageh.recipeapp.core.network

import com.abanobnageh.recipeapp.data.models.network.RecipeDto
import com.abanobnageh.recipeapp.data.models.network.RecipeSearchResponseDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

const val RECIPE_API_TOKEN = "Token 9c8b06d329136da358c2d00e76946b0111ce2c48"

interface RecipeRetrofitService {
    @GET("search")
    fun searchRecipe(
        @Header("Authorization") token: String = RECIPE_API_TOKEN,
        @Query("page") page: Int,
        @Query("query") query: String,
    ): Call<RecipeSearchResponseDto>

    @GET("get")
    fun getRecipe(
        @Header("Authorization") token: String = RECIPE_API_TOKEN,
        @Query("id") id: Int,
    ): Call<RecipeDto>
}