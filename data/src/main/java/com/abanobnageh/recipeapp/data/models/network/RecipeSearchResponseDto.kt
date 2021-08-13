package com.abanobnageh.recipeapp.data.models.network

import com.abanobnageh.recipeapp.data.models.domain.Recipe
import com.abanobnageh.recipeapp.data.models.domain.RecipeSearchResponse
import com.google.gson.annotations.SerializedName

data class RecipeSearchResponseDto(
    @SerializedName("count")
    val count: Int?,
    @SerializedName("next")
    val next: String?,
    @SerializedName("previous")
    val previous: String?,
    @SerializedName("results")
    val results: ArrayList<RecipeDto>?
) {
    fun mapToNetworkModel(): RecipeSearchResponse {
        return RecipeSearchResponse(
            count = this.count,
            next = this.next,
            previous = this.previous,
            results = this.results?.map { recipe -> recipe.mapToDomainModel() } as ArrayList<Recipe>,
        )
    }
}