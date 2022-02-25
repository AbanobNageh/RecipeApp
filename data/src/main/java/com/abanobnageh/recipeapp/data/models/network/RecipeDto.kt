package com.abanobnageh.recipeapp.data.models.network

import com.abanobnageh.recipeapp.data.models.domain.Recipe
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RecipeDto(
    @SerializedName("cooking_instructions")
    val cookingInstructions: String?,
    @SerializedName("date_added")
    val dateAdded: String?,
    @SerializedName("date_updated")
    val dateUpdated: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("featured_image")
    val featuredImage: String?,
    @SerializedName("ingredients")
    val ingredients: ArrayList<String>?,
    @SerializedName("long_date_added")
    val longDateAdded: Int?,
    @SerializedName("long_date_updated")
    val longDateUpdated: Int?,
    @SerializedName("pk")
    val pk: Int?,
    @SerializedName("publisher")
    val publisher: String?,
    @SerializedName("rating")
    val rating: Int?,
    @SerializedName("source_url")
    val sourceUrl: String?,
    @SerializedName("title")
    val title: String?
): Serializable {
    fun mapToDomainModel(): Recipe {
        return Recipe(
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