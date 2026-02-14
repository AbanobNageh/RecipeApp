package com.abanobnageh.recipeapp.core.network

import com.abanobnageh.recipeapp.data.models.network.RecipeDetailResponseDto
import com.abanobnageh.recipeapp.data.models.network.RecipeSearchResponseDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

const val RECIPE_API_KEY = "25aee0ec-ee61-4350-b79a-9ae97119c43b"

interface RecipeRetrofitService {
    @GET("recipes")
    fun searchRecipe(
        @Query("search") query: String,
        @Query("key") key: String = RECIPE_API_KEY,
    ): Call<RecipeSearchResponseDto>

    @GET("recipes/{id}")
    fun getRecipe(
        @Path("id") id: String,
        @Query("key") key: String = RECIPE_API_KEY,
    ): Call<RecipeDetailResponseDto>
}