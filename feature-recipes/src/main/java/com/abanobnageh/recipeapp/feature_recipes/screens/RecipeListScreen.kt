package com.abanobnageh.recipeapp.feature_recipes.screens

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.abanobnageh.recipeapp.core.constants.ACTION_THEME_TOGGLED
import com.abanobnageh.recipeapp.core.theme.RecipeAppTheme
import com.abanobnageh.recipeapp.core.utils.getActivity
import com.abanobnageh.recipeapp.core.utils.isScrolledToEnd
import com.abanobnageh.recipeapp.core.viewmodel.MainActivityViewModel
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeListScreenState
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeListScreenViewModel
import com.abanobnageh.recipeapp.feature_recipes.views.AppBar
import com.abanobnageh.recipeapp.feature_recipes.views.RECIPE_IMAGE_HEIGHT
import com.abanobnageh.recipeapp.feature_recipes.views.RecipeCard
import com.abanobnageh.recipeapp.feature_recipes.views.ShimmerRecipeList
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RecipeListScreen(): AndroidScreen() {

    @Composable
    override fun Content() {
        val viewModel = getViewModel<RecipeListScreenViewModel>()

        val localContext = LocalContext.current
        val activity = localContext.getActivity()
        val coroutineScope = rememberCoroutineScope()

        LifecycleEffect(
            onStarted = {
                coroutineScope.launch {
                    viewModel.getRecipesList()
                }
            }
        )

        RecipeListScreenContent(
            viewModel = viewModel,
            onToggleTheme = {
                activity?.sendBroadcast(Intent(ACTION_THEME_TOGGLED))
            }
        )
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RecipeListScreenContent(
    viewModel: RecipeListScreenViewModel,
    onToggleTheme: () -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow
    val coroutineScope = rememberCoroutineScope()

    val screenState = viewModel.screenState.value
    val recipes = viewModel.recipes
    val selectedFoodCategory = viewModel.selectedFoodCategory.value

    LaunchedEffect( viewModel.recipeListState ) {
        snapshotFlow { viewModel.recipeListState.isScrolledToEnd() }.collect { isScrolledToEnd ->
            if (isScrolledToEnd) {
                viewModel.incrementPageNumber()
                viewModel.getRecipesList()
            }
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                searchText = viewModel.searchText.value,
                onTextChanged = { text -> viewModel.setSearchText(text) },
                onSearch = {
                    coroutineScope.launch {
                        viewModel.resetSearch()
                        viewModel.getRecipesList()
                    }
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