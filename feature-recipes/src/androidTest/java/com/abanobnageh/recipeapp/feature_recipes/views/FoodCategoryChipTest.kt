package com.abanobnageh.recipeapp.feature_recipes.views

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.abanobnageh.recipeapp.data.models.domain.FoodCategory
import com.google.common.truth.Truth
import org.junit.Rule
import org.junit.Test

class FoodCategoryChipTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun foodCategoryChipVisibilityTest() {
        composeTestRule.setContent {
            FoodCategoryChip(
                foodCategory = FoodCategory.CHICKEN.foodCategory,
                isSelected = false,
                onSelected = {},
                onSearch = {},
            )
        }

        composeTestRule.onNodeWithTag("food_category_chip_${FoodCategory.CHICKEN.foodCategory}_text_tag", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun foodCategoryChipNotSelectedTest() {
        composeTestRule.setContent {
            FoodCategoryChip(
                foodCategory = FoodCategory.CHICKEN.foodCategory,
                isSelected = false,
                onSelected = {},
                onSearch = {},
            )
        }

        composeTestRule.onNodeWithTag("food_category_chip_surface_tag", useUnmergedTree = true)
            .assertIsDisplayed()

        for ((key, value) in composeTestRule.onNodeWithTag("food_category_chip_surface_tag").fetchSemanticsNode().config) {
            if (key.name == "color") {
                Truth.assertThat(value.toString()).isEqualTo(Color.LightGray.toString())
            }
        }

        composeTestRule.onNodeWithTag("food_category_chip_${FoodCategory.CHICKEN.foodCategory}_text_tag", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("food_category_chip_${FoodCategory.CHICKEN.foodCategory}_text_tag", useUnmergedTree = true)
            .assertTextEquals(FoodCategory.CHICKEN.foodCategory)
    }

    @Test
    fun foodCategoryChipSelectedTest() {
        composeTestRule.setContent {
            FoodCategoryChip(
                foodCategory = FoodCategory.CHICKEN.foodCategory,
                isSelected = false,
                onSelected = {},
                onSearch = {},
            )
        }

        composeTestRule.onNodeWithTag("food_category_chip_surface_tag", useUnmergedTree = true)
            .assertIsDisplayed()

        for ((key, value) in composeTestRule.onNodeWithTag("food_category_chip_surface_tag").fetchSemanticsNode().config) {
            if (key.name == "color") {
                Truth.assertThat(value.toString()).isEqualTo(Color(0xFF1976D2).toString())
            }
        }

        composeTestRule.onNodeWithTag("food_category_chip_${FoodCategory.CHICKEN.foodCategory}_text_tag", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("food_category_chip_${FoodCategory.CHICKEN.foodCategory}_text_tag", useUnmergedTree = true)
            .assertTextEquals(FoodCategory.CHICKEN.foodCategory)
    }

    @Test
    fun foodCategoryChipOnSelectedTest() {
        var isSelected = false
        composeTestRule.setContent {
            FoodCategoryChip(
                foodCategory = FoodCategory.CHICKEN.foodCategory,
                isSelected = false,
                onSelected = {
                    isSelected = true
                },
                onSearch = {},
            )
        }

        composeTestRule.onNodeWithTag("food_category_chip_row_tag", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        Truth.assertThat(isSelected).isTrue()
    }

    @Test
    fun foodCategoryChipOnSearchTest() {
        var isSelected = false
        composeTestRule.setContent {
            FoodCategoryChip(
                foodCategory = FoodCategory.CHICKEN.foodCategory,
                isSelected = false,
                onSelected = {},
                onSearch = {
                    isSelected = true
                },
            )
        }

        composeTestRule.onNodeWithTag("food_category_chip_row_tag", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        Truth.assertThat(isSelected).isTrue()
    }
}