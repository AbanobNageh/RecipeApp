package com.abanobnageh.recipeapp.data.models.domain

import com.abanobnageh.recipeapp.data.models.network.RecipeDto
import java.io.Serializable

data class Recipe(
    val id: String?,
    val title: String?,
    val publisher: String?,
    val imageUrl: String?,
    val sourceUrl: String?,
    val servings: Int?,
    val cookingTime: Int?,
    val ingredients: ArrayList<Ingredient>?,
): Serializable {
    fun mapToNetworkModel(): RecipeDto {
        return RecipeDto(
            id = this.id,
            title = this.title,
            publisher = this.publisher,
            imageUrl = this.imageUrl,
            sourceUrl = this.sourceUrl,
            servings = this.servings,
            cookingTime = this.cookingTime,
            ingredients = this.ingredients?.map { it.mapToNetworkModel() } as? ArrayList,
        )
    }
}