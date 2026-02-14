package com.abanobnageh.recipeapp.feature_recipes.viewmodels

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abanobnageh.recipeapp.core.error.Error
import com.abanobnageh.recipeapp.core.usecase.Response
import com.abanobnageh.recipeapp.core.usecase.Usecase
import com.abanobnageh.recipeapp.data.models.domain.FoodCategory
import com.abanobnageh.recipeapp.data.models.domain.RecipeSearchItem
import com.abanobnageh.recipeapp.data.models.domain.RecipeSearchResponse
import com.abanobnageh.recipeapp.feature_recipes.usecases.SearchRecipes
import com.abanobnageh.recipeapp.feature_recipes.usecases.SearchRecipesParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.lang.Exception
import javax.inject.Inject

enum class RecipeListScreenState {
    UNINITIALIZED,
    LOADING,
    ERROR,
    NO_RECIPES,
    NORMAL,
    NORMAL_PAGINATION_LOADING,
}

@HiltViewModel
class RecipeListScreenViewModel @Inject constructor(val searchRecipes: SearchRecipes) : ViewModel() {
    val searchText: MutableState<String> = mutableStateOf("pizza")
    val selectedFoodCategory: MutableState<FoodCategory?> = mutableStateOf(null)
    val screenState: MutableState<RecipeListScreenState> = mutableStateOf(RecipeListScreenState.UNINITIALIZED)

    val recipes: ArrayList<RecipeSearchItem> = arrayListOf()
    var error: Error? = null

    var selectedFoodCategoryIndex: Int? = null
    var foodCategoryListState: LazyListState = LazyListState(0, 0)
    var recipeListState: LazyListState = LazyListState(0, 0)

    var isRefreshing = false

    suspend fun getRecipesList(): Unit {
        if (screenState.value == RecipeListScreenState.LOADING) {
            return
        }

        val asyncJob = viewModelScope.async(Dispatchers.IO) {
            screenState.value = RecipeListScreenState.LOADING

            // Use a default search term if searchText is empty
            val queryText = if (searchText.value.isBlank()) "pizza" else searchText.value

            val recipeResponse: Response<Error, RecipeSearchResponse> = searchRecipes.call(
                SearchRecipesParams(
                    query = queryText,
                )
            )

            if (!recipeResponse.isResponse()) {
                error = recipeResponse.error
                screenState.value = RecipeListScreenState.ERROR
                isRefreshing = false
            } else {
                recipes.clear()
                recipes.addAll(recipeResponse.response!!.recipes)
                if (recipes.isEmpty()) {
                    screenState.value = RecipeListScreenState.NO_RECIPES
                } else {
                    screenState.value = RecipeListScreenState.NORMAL
                }
                isRefreshing = false
            }
        }
        this.scrollToCurrentItem()
        return asyncJob.await()
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