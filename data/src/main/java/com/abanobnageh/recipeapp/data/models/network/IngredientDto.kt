package com.abanobnageh.recipeapp.data.models.network

import com.abanobnageh.recipeapp.data.models.domain.Ingredient
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class IngredientDto(
    @SerializedName("quantity")
    val quantity: Double?,
    @SerializedName("unit")
    val unit: String?,
    @SerializedName("description")
    val description: String?
): Serializable {
    fun mapToDomainModel(): Ingredient {
        return Ingredient(
            quantity = this.quantity,
            unit = this.unit,
            description = this.description
        )
    }
}

