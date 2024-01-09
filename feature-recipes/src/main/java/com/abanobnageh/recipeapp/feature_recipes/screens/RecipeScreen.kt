package com.abanobnageh.recipeapp.feature_recipes.screens

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.hilt.getViewModel
import com.abanobnageh.recipeapp.core.constants.RECIPE_SCREEN_KEY
import com.abanobnageh.recipeapp.core.theme.RecipeAppTheme
import com.abanobnageh.recipeapp.data.models.domain.Recipe
import com.abanobnageh.recipeapp.feature_recipes.R
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeScreenState
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeScreenViewModel
import com.abanobnageh.recipeapp.feature_recipes.views.RECIPE_IMAGE_HEIGHT
import com.abanobnageh.recipeapp.feature_recipes.views.ShimmerRecipeView
import com.bumptech.glide.request.RequestOptions
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch

class RecipeScreen(
    val recipeId: Int,
) : Screen {
    override val key: ScreenKey = RECIPE_SCREEN_KEY

    @Composable
    override fun Content() {
        val viewModel = getViewModel<RecipeScreenViewModel>()

        val coroutineScope = rememberCoroutineScope()

        val screenState = viewModel.screenState.value
        val recipe = viewModel.recipe

        LifecycleEffect(
            onStarted = {
                coroutineScope.launch {
                    viewModel.recipeId = recipeId
                    viewModel.getRecipe()
                }
            }
        )

        RecipeScreenContent(
            screenState = screenState,
            recipe = recipe,
        )
    }
}

@Composable
fun RecipeScreenContent(
    screenState: RecipeScreenState,
    recipe: Recipe?,
) {
    Scaffold() { padding ->
        when (screenState) {
            RecipeScreenState.LOADING -> {
                ShimmerRecipeView(
                    listPadding = padding,
                )
            }
            RecipeScreenState.ERROR -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "An Error has occurred",
                        color = MaterialTheme.colors.error,
                    )
                }
            }
            RecipeScreenState.NORMAL -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.background)
                        .verticalScroll(state = ScrollState(0))
                        .padding(padding)
                ) {
                    if (recipe != null) {
                        recipe.featuredImage?.let { featuredImage ->
                            GlideImage(
                                imageModel = { featuredImage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(RECIPE_IMAGE_HEIGHT),
                                requestOptions = {
                                    RequestOptions()
                                        .error(R.drawable.empty_plate)
                                        .placeholder(R.drawable.empty_plate)
                                },
                                imageOptions = ImageOptions(
                                    contentScale = ContentScale.Crop,
                                ),
                                previewPlaceholder = R.drawable.empty_plate,
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            recipe.title?.let { title ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,

                                    ) {
                                    Text(
                                        text = title,
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .wrapContentWidth(Alignment.Start),
                                        style = MaterialTheme.typography.h3,
                                    )
                                    recipe.rating?.let { rating ->
                                        Text(
                                            text = rating.toString(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentWidth(Alignment.End)
                                                .align(Alignment.CenterVertically),
                                            style = MaterialTheme.typography.h5,
                                        )
                                    }
                                }
                                recipe.publisher?.let { publisher ->
                                    val updateData = recipe.dateAdded
                                    var updateText = "By $publisher"

                                    if (updateData != null && publisher.isNotBlank()) {
                                        updateText = "Updated $updateData by $publisher"
                                    }

                                    Text(
                                        text = updateText,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        style = MaterialTheme.typography.caption,
                                    )
                                }
                                recipe.ingredients?.let {
                                    for (ingredient in recipe.ingredients!!) {
                                        Text(
                                            text = ingredient,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp),
                                            style = MaterialTheme.typography.body1,
                                        )
                                    }
                                }
                            }
                        }
                    }
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
fun RecipeScreenContentPreview() {
    val recipe = Recipe(
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

    RecipeAppTheme {
        RecipeScreenContent(
            screenState = RecipeScreenState.NORMAL,
            recipe = recipe,
        )
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
fun RecipeScreenContentLoadingPreview() {
    RecipeAppTheme {
        RecipeScreenContent(
            screenState = RecipeScreenState.LOADING,
            recipe = null,
        )
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
fun RecipeScreenContentErrorPreview() {
    RecipeAppTheme {
        RecipeScreenContent(
            screenState = RecipeScreenState.ERROR,
            recipe = null,
        )
    }
}