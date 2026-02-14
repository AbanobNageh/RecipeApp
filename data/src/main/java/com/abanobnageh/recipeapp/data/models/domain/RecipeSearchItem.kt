package com.abanobnageh.recipeapp.data.models.domain

import com.abanobnageh.recipeapp.data.models.network.RecipeSearchItemDto
import java.io.Serializable

data class RecipeSearchItem(
    val id: String?,
    val title: String?,
    val publisher: String?,
    val imageUrl: String?
): Serializable {
    fun mapToNetworkModel(): RecipeSearchItemDto {
        return RecipeSearchItemDto(
            id = this.id,
            title = this.title,
            publisher = this.publisher,
            imageUrl = this.imageUrl
        )
    }
}

