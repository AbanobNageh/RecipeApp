package com.abanobnageh.recipeapp.data.models.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RecipeDetailResponseDto(
    @SerializedName("status")
    val status: String?,
    @SerializedName("data")
    val data: RecipeDetailDataDto?
): Serializable

data class RecipeDetailDataDto(
    @SerializedName("recipe")
    val recipe: RecipeDto?
): Serializable

