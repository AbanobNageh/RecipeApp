package com.abanobnageh.recipeapp.data.models.network

import com.abanobnageh.recipeapp.data.models.domain.RecipeSearchItem
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RecipeSearchItemDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("publisher")
    val publisher: String?,
    @SerializedName("image_url")
    val imageUrl: String?
): Serializable {
    fun mapToDomainModel(): RecipeSearchItem {
        return RecipeSearchItem(
            id = this.id,
            title = this.title,
            publisher = this.publisher,
            imageUrl = this.imageUrl
        )
    }
}

