package com.abanobnageh.recipeapp.feature_recipes.views

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.abanobnageh.recipeapp.data.models.domain.FoodCategory
import com.google.common.truth.Truth
import org.junit.Rule
import org.junit.Test

class AppBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appBarVisibleTest() {
        composeTestRule.setContent {
            AppBar(
                searchText = "",
                onTextChanged = { /*TODO*/ },
                onSearch = { /*TODO*/ },
                foodCategoryListState = LazyListState(),
                selectedFoodCategory = null,
                setSelectedFoodCategory = { /*TODO*/ },
                setSelectedFoodCategoryIndex = { /*TODO*/ },
                onToggleTheme = { /*TODO*/ },
            )
        }

        composeTestRule.onNodeWithTag("app_bar_text_field_tag")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("app_bar_food_categories_row")
            .assertIsDisplayed()
    }

    @Test
    fun appBarTextTest() {
        composeTestRule.setContent {
            AppBar(
                searchText = "test",
                onTextChanged = { /*TODO*/ },
                onSearch = { /*TODO*/ },
                foodCategoryListState = LazyListState(),
                selectedFoodCategory = null,
                setSelectedFoodCategory = { /*TODO*/ },
                setSelectedFoodCategoryIndex = { /*TODO*/ },
                onToggleTheme = { /*TODO*/ },
            )
        }

        composeTestRule.onNodeWithTag("app_bar_text_field_tag")
            .assertIsDisplayed()

        for ((key, value) in composeTestRule.onNodeWithTag("app_bar_text_field_tag").fetchSemanticsNode().config) {
            if (key.name == "EditableText") {
                Truth.assertThat(value.toString()).isEqualTo("test")
            }
        }
    }

    @Test
    fun appBarFoodCategoryTest() {
        composeTestRule.setContent {
            AppBar(
                searchText = "",
                onTextChanged = { /*TODO*/ },
                onSearch = { /*TODO*/ },
                foodCategoryListState = LazyListState(),
                selectedFoodCategory = FoodCategory.CHICKEN,
                setSelectedFoodCategory = { /*TODO*/ },
                setSelectedFoodCategoryIndex = { /*TODO*/ },
                onToggleTheme = { /*TODO*/ },
            )
        }

        composeTestRule.onNodeWithTag("food_category_chip_${FoodCategory.CHICKEN.foodCategory}_text_tag", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("food_category_chip_${FoodCategory.CHICKEN.foodCategory}_text_tag", useUnmergedTree = true)
            .assertTextEquals(FoodCategory.CHICKEN.foodCategory)
    }

    @Test
    fun appBarFoodTextChangeTest() {
        var isTextChanged = false
        composeTestRule.setContent {
            AppBar(
                searchText = "",
                onTextChanged = {
                    isTextChanged = true
                },
                onSearch = { /*TODO*/ },
                foodCategoryListState = LazyListState(),
                selectedFoodCategory = FoodCategory.CHICKEN,
                setSelectedFoodCategory = { /*TODO*/ },
                setSelectedFoodCategoryIndex = { /*TODO*/ },
                onToggleTheme = { /*TODO*/ },
            )
        }

        composeTestRule.onNodeWithTag("app_bar_text_field_tag")
            .assertIsDisplayed()
            .performTextInput("111")

        Truth.assertThat(isTextChanged).isTrue()
    }

    @Test
    fun appBarFoodTextSearchTest() {
        var isSearchStarted = false
        composeTestRule.setContent {
            AppBar(
                searchText = "",
                onTextChanged = { /*TODO*/ },
                onSearch = {
                    isSearchStarted = true
                },
                foodCategoryListState = LazyListState(),
                selectedFoodCategory = FoodCategory.CHICKEN,
                setSelectedFoodCategory = { /*TODO*/ },
                setSelectedFoodCategoryIndex = { /*TODO*/ },
                onToggleTheme = { /*TODO*/ },
            )
        }

        composeTestRule.onNodeWithTag("app_bar_text_field_tag")
            .assertIsDisplayed()
            .performTextInput("111")

        composeTestRule.onNodeWithTag("app_bar_text_field_tag")
            .assertIsDisplayed()
            .performImeAction()

        Truth.assertThat(isSearchStarted).isTrue()
    }
}