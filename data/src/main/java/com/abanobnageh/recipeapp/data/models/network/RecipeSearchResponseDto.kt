package com.abanobnageh.recipeapp.data.models.network

import com.abanobnageh.recipeapp.data.models.domain.RecipeSearchResponse
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RecipeSearchResponseDto(
    @SerializedName("status")
    val status: String?,
    @SerializedName("results")
    val results: Int?,
    @SerializedName("data")
    val data: RecipeSearchDataDto?
): Serializable {
    fun mapToDomainModel(): RecipeSearchResponse {
        return RecipeSearchResponse(
            status = this.status,
            resultsCount = this.results,
            recipes = this.data?.recipes?.map { it.mapToDomainModel() } as? ArrayList ?: arrayListOf()
        )
    }
}

data class RecipeSearchDataDto(
    @SerializedName("recipes")
    val recipes: ArrayList<RecipeSearchItemDto>?
): Serializable
