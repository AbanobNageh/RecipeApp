package com.abanobnageh.recipeapp.feature_recipes.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun FoodCategoryChip(
    foodCategory: String,
    isSelected: Boolean = false,
    onSelected: (String) -> Unit,
    onSearch: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .testTag("food_category_chip_surface_tag")
            .padding(end = 8.dp),
        elevation = 8.dp, shape = MaterialTheme.shapes.medium,
        color = if (isSelected) Color.LightGray else MaterialTheme.colors.primary,
    ) {
        Row(
            modifier = Modifier
                .testTag("food_category_chip_row_tag")
                .toggleable(
                    value = isSelected,
                    onValueChange = {
                        onSelected(foodCategory)
                        onSearch()
                    },
                ),

            ) {
            Text(
                text = foodCategory,
                style = MaterialTheme.typography.body2,
                color = Color.White,
                modifier = Modifier
                    .testTag("food_category_chip_${foodCategory}_text_tag")
                    .padding(8.dp),
            )
        }
    }
}