package com.abanobnageh.recipeapp.feature_recipes.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.abanobnageh.recipeapp.data.models.domain.RecipeSearchItem
import org.junit.Rule
import org.junit.Test

class RecipeCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun recipeCardVisibleTest() {
        val testRecipe = RecipeSearchItem(
            id = "1",
            title = "Cauldron Cakes - Home - Pastry Affair",
            publisher = "blake",
            imageUrl = "https://nyc3.digitaloceanspaces.com/food2fork/food2fork-static/featured_images/1/featured_image.png",
        )

        composeTestRule.setContent {
            RecipeCard(
                recipe = testRecipe
            )
        }

        composeTestRule.onNodeWithTag("recipe_card_title_tag", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun recipeCardTitleTest() {
        val testRecipe = RecipeSearchItem(
            id = "1",
            title = "Cauldron Cakes - Home - Pastry Affair",
            publisher = "blake",
            imageUrl = "https://nyc3.digitaloceanspaces.com/food2fork/food2fork-static/featured_images/1/featured_image.png",
        )

        composeTestRule.setContent {
            RecipeCard(
                recipe = testRecipe
            )
        }

        composeTestRule.onNodeWithTag("recipe_card_title_tag", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("recipe_card_title_tag", useUnmergedTree = true)
            .assertTextEquals(testRecipe.title!!)
    }
}