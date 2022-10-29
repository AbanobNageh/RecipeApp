package com.abanobnageh.recipeapp.feature_recipes.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abanobnageh.recipeapp.data.models.domain.Recipe
import com.abanobnageh.recipeapp.feature_recipes.R
import com.bumptech.glide.request.RequestOptions
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit = {}) {
    Card(
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .padding(bottom = 6.dp, top = 6.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            recipe.featuredImage?.let { featuredImage ->
                GlideImage(
                    imageModel = { featuredImage },
                    requestOptions = {
                        RequestOptions()
                            .error(R.drawable.empty_plate)
                            .placeholder(R.drawable.empty_plate)
                    },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(RECIPE_IMAGE_HEIGHT),
                )
            }
            recipe.title?.let { title ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 12.dp,
                            bottom = 12.dp,
                            start = 8.dp,
                            end = 8.dp,
                        )
                ) {
                    Text(
                        text = title,
                        modifier = Modifier
                            .testTag("recipe_card_title_tag")
                            .fillMaxWidth(.85F)
                            .wrapContentWidth(Alignment.Start),
                        style = MaterialTheme.typography.h5
                    )
                    Text(
                        text = recipe.rating.toString(),
                        modifier = Modifier
                            .testTag("recipe_card_rating_tag")
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.End)
                            .align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.h6
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewRecipeCard() {
    RecipeCard(
        Recipe(
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
    )
}