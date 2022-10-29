package com.abanobnageh.recipeapp.feature_recipes.screens

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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.hilt.getViewModel
import com.abanobnageh.recipeapp.core.theme.RecipeAppTheme
import com.abanobnageh.recipeapp.core.utils.getActivity
import com.abanobnageh.recipeapp.core.viewmodel.MainActivityViewModel
import com.abanobnageh.recipeapp.feature_recipes.R
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeScreenState
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeScreenViewModel
import com.abanobnageh.recipeapp.feature_recipes.views.RECIPE_IMAGE_HEIGHT
import com.abanobnageh.recipeapp.feature_recipes.views.ShimmerRecipeView
import com.bumptech.glide.request.RequestOptions
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.imageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.placeholder.PlaceholderPlugin

class RecipeScreen(
    val recipeId: Int,
) : AndroidScreen() {

    @Composable
    override fun Content() {
        val viewModel = getViewModel<RecipeScreenViewModel>()

        val localContext = LocalContext.current

        LifecycleEffect(
            onStarted = {
                viewModel.recipeId = recipeId
                viewModel.getRecipe()
            }
        )

        RecipeScreenContent(
            viewModel
        )
    }
}

@Composable
fun RecipeScreenContent(
    viewModel: RecipeScreenViewModel
) {
    val screenState = viewModel.screenState.value
    val recipe = viewModel.recipe

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