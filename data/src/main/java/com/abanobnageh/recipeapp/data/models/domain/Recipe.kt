package com.abanobnageh.recipeapp.data.models.domain

import com.abanobnageh.recipeapp.data.models.network.RecipeDto
import java.io.Serializable

data class Recipe(
    val cookingInstructions: String?,
    val dateAdded: String?,
    val dateUpdated: String?,
    val description: String?,
    val featuredImage: String?,
    val ingredients: ArrayList<String>?,
    val longDateAdded: Int?,
    val longDateUpdated: Int?,
    val pk: Int?,
    val publisher: String?,
    val rating: Int?,
    val sourceUrl: String?,
    val title: String?,
): Serializable {
    fun mapToNetworkModel(): RecipeDto {
        return RecipeDto(
            cookingInstructions = this.cookingInstructions,
            dateAdded = this.dateAdded,
            dateUpdated = this.dateUpdated,
            description = this.description,
            featuredImage = this.featuredImage,
            ingredients = this.ingredients,
            longDateAdded = this.longDateAdded,
            longDateUpdated = this.longDateUpdated,
            pk = this.pk,
            publisher = this.publisher,
            rating = this.rating,
            sourceUrl = this.sourceUrl,
            title = this.title,
        )
    }
}