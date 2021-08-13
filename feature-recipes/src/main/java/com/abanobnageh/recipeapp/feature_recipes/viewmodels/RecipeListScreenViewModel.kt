package com.abanobnageh.recipeapp.feature_recipes.viewmodels

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abanobnageh.recipeapp.core.error.Error
import com.abanobnageh.recipeapp.core.usecase.Response
import com.abanobnageh.recipeapp.data.models.domain.FoodCategory
import com.abanobnageh.recipeapp.data.models.domain.Recipe
import com.abanobnageh.recipeapp.data.models.domain.RecipeSearchResponse
import com.abanobnageh.recipeapp.feature_recipes.usecases.SearchRecipes
import com.abanobnageh.recipeapp.feature_recipes.usecases.SearchRecipesParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

enum class RecipeListScreenState {
    LOADING,
    ERROR,
    NO_RECIPES,
    NORMAL,
    NORMAL_PAGINATION_LOADING,
}

@HiltViewModel
class RecipeListScreenViewModel @Inject constructor(val searchRecipes: SearchRecipes) : ViewModel() {
    val searchText: MutableState<String> = mutableStateOf("")
    val selectedFoodCategory: MutableState<FoodCategory?> = mutableStateOf(null)
    val screenState: MutableState<RecipeListScreenState> = mutableStateOf(RecipeListScreenState.NORMAL)

    val recipes: ArrayList<Recipe> = arrayListOf()
    var error: Error? = null

    var selectedFoodCategoryIndex: Int? = null
    var foodCategoryListState: LazyListState = LazyListState(0, 0)
    var recipeListState: LazyListState = LazyListState(0, 0)

    var paginationPageNumber = 1
    var isPaginationDone = false

    fun getRecipesList() {
        if (isPaginationDone ||
            screenState.value == RecipeListScreenState.LOADING ||
            screenState.value == RecipeListScreenState.NORMAL_PAGINATION_LOADING
        ) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (recipes.isEmpty()) {
                screenState.value = RecipeListScreenState.LOADING
            } else {
                screenState.value = RecipeListScreenState.NORMAL_PAGINATION_LOADING
            }

            val recipeResponse: Response<Error, RecipeSearchResponse> = searchRecipes.call(
                SearchRecipesParams(
                    query = searchText.value,
                    pageNumber = paginationPageNumber,
                )
            )

            if (!recipeResponse.isResponse()) {
                error = recipeResponse.error
                screenState.value = RecipeListScreenState.ERROR
            } else {
                recipes.addAll(recipeResponse.response!!.results)
                if (recipeResponse.response!!.next == null) {
                    isPaginationDone = true
                }
                screenState.value = RecipeListScreenState.NORMAL
            }
        }
        this.scrollToCurrentItem()
    }

    fun incrementPageNumber() {
        if (!isPaginationDone) {
            this.paginationPageNumber = this.paginationPageNumber + 1
        }
    }

    fun setSearchText(searchText: String) {
        if (FoodCategory.isFoodCategory(searchText)) {
            setSelectedFoodCategory(searchText)
        } else {
            if (this.selectedFoodCategory.value != null) {
                this.selectedFoodCategory.value = null
            }

            this.searchText.value = searchText
        }
    }

    fun setSelectedFoodCategory(foodCategory: String) {
        if (screenState.value == RecipeListScreenState.LOADING) {
            return
        }

        this.selectedFoodCategory.value = FoodCategory.getFoodCategory(foodCategory)

        if (this.selectedFoodCategory.value != null) {
            this.searchText.value = this.selectedFoodCategory.value?.foodCategory!!
        } else {
            this.searchText.value = ""
        }
    }

    fun resetSearch() {
        this.isPaginationDone = false
        this.paginationPageNumber = 1
        this.recipeListState = LazyListState(0, 0)
        this.recipes.clear()
    }

    private fun scrollToCurrentItem() {
        if (this.selectedFoodCategoryIndex != null) {
            viewModelScope.launch {
                try {
                    if (!foodCategoryListState.isScrollInProgress) {
                        foodCategoryListState.scrollToItem(selectedFoodCategoryIndex!!)
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }
        }
    }

    fun setSelectedFoodCategoryIndex(selectedFoodCategoryIndex: Int) {
        this.selectedFoodCategoryIndex = selectedFoodCategoryIndex
    }
}