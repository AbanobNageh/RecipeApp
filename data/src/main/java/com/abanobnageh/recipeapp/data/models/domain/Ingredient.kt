package com.abanobnageh.recipeapp.data.models.domain

import com.abanobnageh.recipeapp.data.models.network.IngredientDto
import java.io.Serializable

data class Ingredient(
    val quantity: Double?,
    val unit: String?,
    val description: String?
): Serializable {
    fun toDisplayString(): String {
        val quantityStr = quantity?.let {
            if (it % 1.0 == 0.0) it.toInt().toString()
            else it.toString()
        } ?: ""
        val unitStr = unit?.takeIf { it.isNotEmpty() } ?: ""
        val descStr = description ?: ""

        return listOf(quantityStr, unitStr, descStr)
            .filter { it.isNotEmpty() }
            .joinToString(" ")
    }

    fun mapToNetworkModel(): IngredientDto {
        return IngredientDto(
            quantity = this.quantity,
            unit = this.unit,
            description = this.description
        )
    }
}

