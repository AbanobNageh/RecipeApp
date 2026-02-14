package com.abanobnageh.recipeapp.feature_recipes.screens

import android.content.res.Configuration
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.abanobnageh.recipeapp.core.theme.RecipeAppTheme
import com.abanobnageh.recipeapp.core.utils.getActivity
import com.abanobnageh.recipeapp.core.viewmodel.MainActivityViewModel
import com.abanobnageh.recipeapp.data.models.domain.FoodCategory
import com.abanobnageh.recipeapp.data.models.domain.RecipeSearchItem
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeListScreenState
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeListScreenViewModel
import com.abanobnageh.recipeapp.feature_recipes.views.AppBar
import com.abanobnageh.recipeapp.feature_recipes.views.RECIPE_IMAGE_HEIGHT
import com.abanobnageh.recipeapp.feature_recipes.views.RecipeCard
import com.abanobnageh.recipeapp.feature_recipes.views.ShimmerRecipeList
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun RecipeListScreen(
    openRecipeScreen: (recipeId: String) -> Unit,
) {
    val viewModel = hiltViewModel<RecipeListScreenViewModel>()

    val localContext = LocalContext.current
    val activity = localContext.getActivity()
    val activityViewModel: MainActivityViewModel by activity?.viewModels() ?: return
    val coroutineScope = rememberCoroutineScope()

    val searchText = viewModel.searchText.value
    val screenState = viewModel.screenState.value
    val recipes = viewModel.recipes
    val selectedFoodCategory = viewModel.selectedFoodCategory.value
    val isRefreshing = viewModel.isRefreshing

    LaunchedEffect(Unit) {
        if (screenState === RecipeListScreenState.UNINITIALIZED) {
            viewModel.getRecipesList()
        }
    }

    RecipeListScreenContent(
        searchText = searchText,
        screenState = screenState,
        recipes = recipes,
        selectedFoodCategory = selectedFoodCategory,
        isRefreshing = isRefreshing,
        recipeListState = viewModel.recipeListState,
        foodCategoryListState = viewModel.foodCategoryListState,
        onToggleTheme = {
            activityViewModel.toggleDarkTheme()
        },
        getRecipesList = { viewModel.getRecipesList() },
        refreshRecipesList = {
            viewModel.isRefreshing = true
            viewModel.recipes.clear()
            viewModel.getRecipesList()
        },
        resetSearch = { viewModel.resetSearch() },
        setSearchText = { viewModel.setSearchText(it) },
        setSelectedFoodCategory = { viewModel.setSelectedFoodCategory(it) },
        setSelectedFoodCategoryIndex = { viewModel.setSelectedFoodCategoryIndex(it) },
        openRecipeScreen = openRecipeScreen,
    )
}

@ExperimentalMaterialApi
@Composable
fun RecipeListScreenContent(
    searchText: String,
    screenState: RecipeListScreenState,
    recipes: ArrayList<RecipeSearchItem>,
    selectedFoodCategory: FoodCategory?,
    isRefreshing: Boolean,
    recipeListState: LazyListState,
    foodCategoryListState: LazyListState,
    onToggleTheme: () -> Unit,
    getRecipesList: suspend () -> Unit,
    refreshRecipesList: suspend () -> Unit,
    resetSearch: () -> Unit,
    setSearchText: (searchText: String) -> Unit,
    setSelectedFoodCategory: (foodCategory: String) -> Unit,
    setSelectedFoodCategoryIndex: (selectedFoodCategoryIndex: Int) -> Unit,
    openRecipeScreen: (recipeId: String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
        coroutineScope.launch {
            refreshRecipesList()
        }
    })

    Scaffold(
        topBar = {
            AppBar(
                searchText = searchText,
                onTextChanged = { text -> setSearchText(text) },
                onSearch = {
                    coroutineScope.launch {
                        resetSearch()
                        getRecipesList()
                    }
                },
                foodCategoryListState = foodCategoryListState,
                selectedFoodCategory = selectedFoodCategory,
                setSelectedFoodCategory = { selectedFoodCategory ->
                    setSelectedFoodCategory(selectedFoodCategory)
                },
                setSelectedFoodCategoryIndex = { selectedIndex ->
                    setSelectedFoodCategoryIndex(selectedIndex)
                },
                onToggleTheme = onToggleTheme,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .pullRefresh(pullToRefreshState)
                .padding(padding)
        ) {
            when (screenState) {
                RecipeListScreenState.LOADING -> {
                    ShimmerRecipeList(
                        imageHeight = RECIPE_IMAGE_HEIGHT,
                        listPadding = padding,
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
                        modifier = Modifier
                            .fillMaxSize(),
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
                            state = recipeListState,
                        ) {
                            itemsIndexed(
                                items = recipes
                            ) { index, recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = {
                                        recipe.id?.let {
                                            openRecipeScreen(it)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter),
                refreshing = isRefreshing,
                state = pullToRefreshState,
            )
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
@ExperimentalMaterialApi
@Composable
fun RecipeListScreenContentPreview() {
    val recipe = RecipeSearchItem(
        id = "664c8f193e7aa067e94e8297",
        title = "Cauldron Cakes - Home - Pastry Affair",
        publisher = "blake",
        imageUrl = "https://nyc3.digitaloceanspaces.com/food2fork/food2fork-static/featured_images/1/featured_image.png",
    )

    RecipeAppTheme {
        RecipeListScreenContent(
            searchText = "",
            screenState = RecipeListScreenState.NORMAL,
            recipes = arrayListOf(recipe),
            selectedFoodCategory = null,
            isRefreshing = false,
            recipeListState = LazyListState(),
            foodCategoryListState = LazyListState(),
            onToggleTheme = {},
            getRecipesList = {},
            refreshRecipesList = {},
            resetSearch = {},
            setSearchText = {},
            setSelectedFoodCategory = {},
            setSelectedFoodCategoryIndex = {},
            openRecipeScreen = {},
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
@ExperimentalMaterialApi
@Composable
fun RecipeListScreenContentLoadingPreview() {
    RecipeAppTheme {
        RecipeListScreenContent(
            searchText = "",
            screenState = RecipeListScreenState.LOADING,
            recipes = arrayListOf(),
            selectedFoodCategory = null,
            isRefreshing = false,
            recipeListState = LazyListState(),
            foodCategoryListState = LazyListState(),
            onToggleTheme = {},
            getRecipesList = {},
            refreshRecipesList = {},
            resetSearch = {},
            setSearchText = {},
            setSelectedFoodCategory = {},
            setSelectedFoodCategoryIndex = {},
            openRecipeScreen = {},
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
@ExperimentalMaterialApi
@Composable
fun RecipeListScreenContentNoRecipesPreview() {
    RecipeAppTheme {
        RecipeListScreenContent(
            searchText = "",
            screenState = RecipeListScreenState.NO_RECIPES,
            recipes = arrayListOf(),
            selectedFoodCategory = null,
            isRefreshing = false,
            recipeListState = LazyListState(),
            foodCategoryListState = LazyListState(),
            onToggleTheme = {},
            getRecipesList = {},
            refreshRecipesList = {},
            resetSearch = {},
            setSearchText = {},
            setSelectedFoodCategory = {},
            setSelectedFoodCategoryIndex = {},
            openRecipeScreen = {},
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
@ExperimentalMaterialApi
@Composable
fun RecipeListScreenContentErrorPreview() {
    RecipeAppTheme {
        RecipeListScreenContent(
            searchText = "",
            screenState = RecipeListScreenState.ERROR,
            recipes = arrayListOf(),
            selectedFoodCategory = null,
            isRefreshing = false,
            recipeListState = LazyListState(),
            foodCategoryListState = LazyListState(),
            onToggleTheme = {},
            getRecipesList = {},
            refreshRecipesList = {},
            resetSearch = {},
            setSearchText = {},
            setSelectedFoodCategory = {},
            setSelectedFoodCategoryIndex = {},
            openRecipeScreen = {},
        )
    }
}
