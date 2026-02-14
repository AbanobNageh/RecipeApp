package com.abanobnageh.recipeapp.data.models.network

import com.abanobnageh.recipeapp.data.models.domain.Recipe
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RecipeDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("publisher")
    val publisher: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("source_url")
    val sourceUrl: String?,
    @SerializedName("servings")
    val servings: Int?,
    @SerializedName("cooking_time")
    val cookingTime: Int?,
    @SerializedName("ingredients")
    val ingredients: ArrayList<IngredientDto>?
): Serializable {
    fun mapToDomainModel(): Recipe {
        return Recipe(
            id = this.id,
            title = this.title,
            publisher = this.publisher,
            imageUrl = this.imageUrl,
            sourceUrl = this.sourceUrl,
            servings = this.servings,
            cookingTime = this.cookingTime,
            ingredients = this.ingredients?.map { it.mapToDomainModel() } as? ArrayList,
        )
    }
}