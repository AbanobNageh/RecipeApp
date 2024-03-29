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
import androidx.compose.foundation.layout.requiredHeight
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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.abanobnageh.recipeapp.core.constants.RECIPE_LIST_SCREEN_KEY
import com.abanobnageh.recipeapp.core.theme.RecipeAppTheme
import com.abanobnageh.recipeapp.core.utils.getActivity
import com.abanobnageh.recipeapp.core.utils.getResult
import com.abanobnageh.recipeapp.core.utils.hideWithResult
import com.abanobnageh.recipeapp.core.utils.isScrolledToEnd
import com.abanobnageh.recipeapp.core.viewmodel.MainActivityViewModel
import com.abanobnageh.recipeapp.data.models.domain.FoodCategory
import com.abanobnageh.recipeapp.data.models.domain.Recipe
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeListScreenState
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeListScreenViewModel
import com.abanobnageh.recipeapp.feature_recipes.views.AppBar
import com.abanobnageh.recipeapp.feature_recipes.views.RECIPE_IMAGE_HEIGHT
import com.abanobnageh.recipeapp.feature_recipes.views.RecipeCard
import com.abanobnageh.recipeapp.feature_recipes.views.ShimmerRecipeList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
class RecipeListScreen : Screen {

    override val key: ScreenKey = RECIPE_LIST_SCREEN_KEY

    @Composable
    override fun Content() {
        val viewModel = getViewModel<RecipeListScreenViewModel>()

        val navigator = LocalNavigator.currentOrThrow
        val localContext = LocalContext.current
        val activity = localContext.getActivity()
        val activityViewModel: MainActivityViewModel by activity?.viewModels() ?: return
        val coroutineScope = rememberCoroutineScope()

        val searchText = viewModel.searchText.value
        val screenState = viewModel.screenState.value
        val recipes = viewModel.recipes
        val selectedFoodCategory = viewModel.selectedFoodCategory.value
        val isRefreshing = viewModel.isRefreshing

        LifecycleEffect(
            onStarted = {
                coroutineScope.launch {
                    if (screenState === RecipeListScreenState.UNINITIALIZED) {
                        viewModel.getRecipesList()
                    }
                }
            }
        )

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
            incrementPageNumber = { viewModel.incrementPageNumber() },
            getRecipesList = { viewModel.getRecipesList() },
            refreshRecipesList = {
                viewModel.isRefreshing = true
                viewModel.isPaginationDone = false
                viewModel.recipes.clear()
                viewModel.getRecipesList()
            },
            resetSearch = { viewModel.resetSearch() },
            setSearchText = { viewModel.setSearchText(it) },
            setSelectedFoodCategory = { viewModel.setSelectedFoodCategory(it) },
            setSelectedFoodCategoryIndex = { viewModel.setSelectedFoodCategoryIndex(it) },
            openRecipeScreen = { recipeId ->
                navigator.push(RecipeScreen(recipeId = recipeId))
            },
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun RecipeListScreenContent(
    searchText: String,
    screenState: RecipeListScreenState,
    recipes: ArrayList<Recipe>,
    selectedFoodCategory: FoodCategory?,
    isRefreshing: Boolean,
    recipeListState: LazyListState,
    foodCategoryListState: LazyListState,
    onToggleTheme: () -> Unit,
    incrementPageNumber: () -> Unit,
    getRecipesList: suspend () -> Unit,
    refreshRecipesList: suspend () -> Unit,
    resetSearch: () -> Unit,
    setSearchText: (searchText: String) -> Unit,
    setSelectedFoodCategory: (foodCategory: String) -> Unit,
    setSelectedFoodCategoryIndex: (selectedFoodCategoryIndex: Int) -> Unit,
    openRecipeScreen: (recipeId: Int) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
        coroutineScope.launch {
            refreshRecipesList()
        }
    })

    LaunchedEffect( recipeListState ) {
        snapshotFlow { recipeListState.isScrolledToEnd() }.collect { isScrolledToEnd ->
            if (isScrolledToEnd) {
                incrementPageNumber()
                getRecipesList()
            }
        }
    }

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
                RecipeListScreenState.NO_RECIPES -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
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
                                        recipe.pk?.let {
                                            openRecipeScreen(it)
                                        }
                                    }
                                )
                                if (recipes.size - 1 == index &&
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
        RecipeListScreenContent(
            searchText = "",
            screenState = RecipeListScreenState.NORMAL,
            recipes = arrayListOf(recipe),
            selectedFoodCategory = null,
            isRefreshing = false,
            recipeListState = LazyListState(),
            foodCategoryListState = LazyListState(),
            onToggleTheme = {},
            incrementPageNumber = {},
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
            incrementPageNumber = {},
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
            incrementPageNumber = {},
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
fun RecipeListScreenContentPaginationPreview() {
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
        RecipeListScreenContent(
            searchText = "",
            screenState = RecipeListScreenState.NORMAL_PAGINATION_LOADING,
            recipes = arrayListOf(recipe, recipe),
            selectedFoodCategory = null,
            isRefreshing = false,
            recipeListState = LazyListState(),
            foodCategoryListState = LazyListState(),
            onToggleTheme = {},
            incrementPageNumber = {},
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
            incrementPageNumber = {},
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