package com.abanobnageh.recipeapp.feature_recipes.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.abanobnageh.recipeapp.data.models.domain.Recipe
import org.junit.Rule
import org.junit.Test

class RecipeCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun recipeCardVisibleTest() {
        val testRecipe = Recipe(
            cookingInstructions = null,
            dateAdded = "November 11 2020",
            dateUpdated = "November 11 2020",
            description = "N/A",
            featuredImage = "https://nyc3.digitaloceanspaces.com/food2fork/food2fork-static/featured_images/1/featured_image.png",
            ingredients = arrayListOf(
                "12",
                "cupcakes",
                "devil's food",
                "Chocolate Glaze",
                "Edible gold glitter",
                "4 tablespoons butter",
                "1 recipe for Chocolate Glaze (below)",
                "Approximately 1/2 cup chocolate chips",
                "1 recipe for Marshmallow Filling (below)",
                "6 ounces (1 cup) semi-sweet chocolate chips"
            ),
            longDateAdded = 1606348709,
            longDateUpdated = 1606348709,
            pk = 1,
            publisher = "blake",
            rating = 22,
            sourceUrl = "http://www.thepastryaffair.com/blog/2011/7/12/cauldron-cakes.html",
            title = "Cauldron&nbsp;Cakes - Home - Pastry Affair",
        )

        composeTestRule.setContent {
            RecipeCard(
                recipe = testRecipe
            )
        }

        composeTestRule.onNodeWithTag("recipe_card_title_tag", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("recipe_card_rating_tag", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun recipeCardTitleTest() {
        val testRecipe = Recipe(
            cookingInstructions = null,
            dateAdded = "November 11 2020",
            dateUpdated = "November 11 2020",
            description = "N/A",
            featuredImage = "https://nyc3.digitaloceanspaces.com/food2fork/food2fork-static/featured_images/1/featured_image.png",
            ingredients = arrayListOf(
                "12",
                "cupcakes",
                "devil's food",
                "Chocolate Glaze",
                "Edible gold glitter",
                "4 tablespoons butter",
                "1 recipe for Chocolate Glaze (below)",
                "Approximately 1/2 cup chocolate chips",
                "1 recipe for Marshmallow Filling (below)",
                "6 ounces (1 cup) semi-sweet chocolate chips"
            ),
            longDateAdded = 1606348709,
            longDateUpdated = 1606348709,
            pk = 1,
            publisher = "blake",
            rating = 22,
            sourceUrl = "http://www.thepastryaffair.com/blog/2011/7/12/cauldron-cakes.html",
            title = "Cauldron&nbsp;Cakes - Home - Pastry Affair",
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

    @Test
    fun recipeCardRatingTest() {
        val testRecipe = Recipe(
            cookingInstructions = null,
            dateAdded = "November 11 2020",
            dateUpdated = "November 11 2020",
            description = "N/A",
            featuredImage = "https://nyc3.digitaloceanspaces.com/food2fork/food2fork-static/featured_images/1/featured_image.png",
            ingredients = arrayListOf(
                "12",
                "cupcakes",
                "devil's food",
                "Chocolate Glaze",
                "Edible gold glitter",
                "4 tablespoons butter",
                "1 recipe for Chocolate Glaze (below)",
                "Approximately 1/2 cup chocolate chips",
                "1 recipe for Marshmallow Filling (below)",
                "6 ounces (1 cup) semi-sweet chocolate chips"
            ),
            longDateAdded = 1606348709,
            longDateUpdated = 1606348709,
            pk = 1,
            publisher = "blake",
            rating = 22,
            sourceUrl = "http://www.thepastryaffair.com/blog/2011/7/12/cauldron-cakes.html",
            title = "Cauldron&nbsp;Cakes - Home - Pastry Affair",
        )

        composeTestRule.setContent {
            RecipeCard(
                recipe = testRecipe
            )
        }

        composeTestRule.onNodeWithTag("recipe_card_rating_tag", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("recipe_card_rating_tag", useUnmergedTree = true)
            .assertTextEquals(testRecipe.rating.toString())
    }
}