package com.abanobnageh.recipeapp.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.findNavController
import com.abanobnageh.recipeapp.MainActivityViewModel
import com.abanobnageh.recipeapp.R
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeListScreenState
import com.abanobnageh.recipeapp.feature_recipes.viewmodels.RecipeListScreenViewModel
import com.abanobnageh.recipeapp.feature_recipes.views.*
import com.abanobnageh.recipeapp.ui.theme.RecipeAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecipeListFragment : Fragment() {
    private val activityViewModel: MainActivityViewModel by viewModels()
    private val viewModel: RecipeListScreenViewModel by hiltNavGraphViewModels(R.id.main_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val isDarkTheme = activityViewModel.loadDarkTheme(activity)

                val screenState = viewModel.screenState.value
                val recipes = viewModel.recipes
                val selectedFoodCategory = viewModel.selectedFoodCategory.value

                RecipeAppTheme(darkTheme = isDarkTheme) {
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
                                onToggleTheme = {
                                    activityViewModel.setIsDarkTheme(activity, !isDarkTheme)
                                }
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
                                                    if (recipe.pk != null) {
                                                        val bundle = Bundle()
                                                        bundle.putInt("recipeId", recipe.pk!!)
                                                        findNavController().navigate(R.id.action_recipeListFragment_to_recipeFragment, bundle)
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
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getRecipesList()
    }
}