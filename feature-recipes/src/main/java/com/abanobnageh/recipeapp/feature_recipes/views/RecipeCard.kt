package com.abanobnageh.recipeapp.feature_recipes.views

import android.content.res.Configuration
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abanobnageh.recipeapp.core.theme.RecipeAppTheme
import com.abanobnageh.recipeapp.data.models.domain.RecipeSearchItem
import com.abanobnageh.recipeapp.feature_recipes.R
import com.bumptech.glide.request.RequestOptions
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun RecipeCard(recipe: RecipeSearchItem, onClick: () -> Unit = {}) {
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
            recipe.imageUrl?.let { imageUrl ->
                GlideImage(
                    imageModel = { imageUrl },
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
                    previewPlaceholder = painterResource(id = R.drawable.empty_plate),
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
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.Start),
                        style = MaterialTheme.typography.h5
                    )
                }
            }
        }
    }
}

@Preview(
    group = "lightTheme",
)
@Preview(
    group = "darkTheme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PreviewRecipeCard() {
    RecipeAppTheme {
        RecipeCard(
            RecipeSearchItem(
                id = "664c8f193e7aa067e94e8297",
                imageUrl = "https://nyc3.digitaloceanspaces.com/food2fork/food2fork-static/featured_images/1/featured_image.png",
                publisher = "blake",
                title = "Cauldron Cakes - Home - Pastry Affair",
            )
        )
    }
}