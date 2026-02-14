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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abanobnageh.recipeapp.core.theme.RecipeAppTheme
import com.abanobnageh.recipeapp.data.models.domain.Ingredient
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

@Composable
fun RecipeScreen(
    recipeId: String,
) {
    val viewModel = hiltViewModel<RecipeScreenViewModel>()

    val coroutineScope = rememberCoroutineScope()

    val screenState = viewModel.screenState.value
    val recipe = viewModel.recipe

    LaunchedEffect(recipeId) {
        viewModel.recipeId = recipeId
        viewModel.getRecipe()
    }

    RecipeScreenContent(
        screenState = screenState,
        recipe = recipe,
    )
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
                        recipe.imageUrl?.let { imageUrl ->
                            GlideImage(
                                imageModel = { imageUrl },
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
                                previewPlaceholder = painterResource(id = R.drawable.empty_plate),
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            recipe.title?.let { title ->
                                Text(
                                    text = title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    style = MaterialTheme.typography.h3,
                                )

                                recipe.publisher?.let { publisher ->
                                    Text(
                                        text = "By $publisher",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        style = MaterialTheme.typography.caption,
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    recipe.servings?.let { servings ->
                                        Text(
                                            text = "Servings: $servings",
                                            style = MaterialTheme.typography.body2,
                                        )
                                    }
                                    recipe.cookingTime?.let { cookingTime ->
                                        Text(
                                            text = "Cooking Time: $cookingTime min",
                                            style = MaterialTheme.typography.body2,
                                        )
                                    }
                                }

                                recipe.ingredients?.let {
                                    Text(
                                        text = "Ingredients:",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 8.dp),
                                        style = MaterialTheme.typography.h5,
                                    )
                                    for (ingredient in recipe.ingredients!!) {
                                        Text(
                                            text = "• ${ingredient.toDisplayString()}",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 4.dp, start = 8.dp),
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
        id = "664c8f193e7aa067e94e8297",
        title = "Double Crust Stuffed Pizza",
        publisher = "All Recipes",
        imageUrl = "https://nyc3.digitaloceanspaces.com/food2fork/food2fork-static/featured_images/1/featured_image.png",
        sourceUrl = "http://allrecipes.com/Recipe/Double-Crust-Stuffed-Pizza/Detail.aspx",
        servings = 4,
        cookingTime = 120,
        ingredients = arrayListOf(
            Ingredient(1.5, "tsps", "white sugar"),
            Ingredient(1.0, "cup", "warm water"),
            Ingredient(2.0, "cups", "all-purpose flour"),
            Ingredient(1.0, "", "can crushed tomatoes"),
            Ingredient(3.0, "cups", "shredded mozzarella cheese"),
        ),
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