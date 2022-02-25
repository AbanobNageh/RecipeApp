package com.abanobnageh.recipeapp.feature_recipes.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeListScreenState
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeListScreenViewModel
import com.abanobnageh.recipeapp.feature_recipes.views.AppBar
import com.abanobnageh.recipeapp.feature_recipes.views.RECIPE_IMAGE_HEIGHT
import com.abanobnageh.recipeapp.feature_recipes.views.RecipeCard
import com.abanobnageh.recipeapp.feature_recipes.views.ShimmerRecipeList

class RecipeListScreen(
    val onToggleTheme: () -> Unit,
): AndroidScreen() {

    @Composable
    override fun Content() {
        val viewModel = getViewModel<RecipeListScreenViewModel>()

        LifecycleEffect(
            onStarted = {
                viewModel.getRecipesList()
            }
        )

        RecipeListScreenContent(
            viewModel = viewModel,
            onToggleTheme = onToggleTheme,
        )
    }
}

@Composable
fun RecipeListScreenContent(
    viewModel: RecipeListScreenViewModel,
    onToggleTheme: () -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow

    val screenState = viewModel.screenState.value
    val recipes = viewModel.recipes
    val selectedFoodCategory = viewModel.selectedFoodCategory.value

    Scaffold(
        topBar = {
            AppBar(
                searchText = viewModel.searchText.value,
                onTextChanged = { text -> viewModel.setSearchText(text) },
                onSearch = {
                    viewModel.resetSearch()
                    viewModel.getRecipesList()
                },
                foodCategoryListState = viewModel.foodCategoryListState,
                selectedFoodCategory = selectedFoodCategory,
                setSelectedFoodCategory = { selectedFoodCategory ->
                    viewModel.setSelectedFoodCategory(selectedFoodCategory)
                },
                setSelectedFoodCategoryIndex = { selectedIndex ->
                    viewModel.setSelectedFoodCategoryIndex(selectedIndex)
                },
                onToggleTheme = onToggleTheme,
            )
        },
    ) {
        when (screenState) {
            RecipeListScreenState.LOADING -> {
                ShimmerRecipeList(
                    imageHeight = RECIPE_IMAGE_HEIGHT,
                )
            }
            RecipeListScreenState.ERROR -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "An Error has occurred",
                        color = MaterialTheme.colors.error,
                    )
                }
            }
            RecipeListScreenState.NO_RECIPES -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "No recipes to show")
                }
            }
            else -> {
                Column {
                    LazyColumn(
                        modifier = Modifier
                            .background(MaterialTheme.colors.background),
                        state = viewModel.recipeListState,
                    ) {
                        itemsIndexed(
                            items = recipes
                        ) { index, recipe ->
                            if (viewModel.recipes.size - 1 == index) {
                                viewModel.incrementPageNumber()
                                viewModel.getRecipesList()
                            }

                            RecipeCard(
                                recipe = recipe,
                                onClick = {
                                    recipe.pk?.let {
                                        navigator.push(RecipeScreen(recipeId = it))
                                    }
                                }
                            )
                            if (viewModel.recipes.size - 1 == index &&
                                screenState == RecipeListScreenState.NORMAL_PAGINATION_LOADING
                            ) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}