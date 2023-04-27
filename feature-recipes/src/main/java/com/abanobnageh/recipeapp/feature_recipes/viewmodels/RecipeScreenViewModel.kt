package com.abanobnageh.recipeapp.feature_recipes.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abanobnageh.recipeapp.core.error.Error
import com.abanobnageh.recipeapp.core.usecase.Response
import com.abanobnageh.recipeapp.data.models.domain.Recipe
import com.abanobnageh.recipeapp.feature_recipes.usecases.GetRecipe
import com.abanobnageh.recipeapp.feature_recipes.usecases.GetRecipeParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

enum class RecipeScreenState {
    LOADING,
    ERROR,
    NORMAL,
}

@HiltViewModel
class RecipeScreenViewModel @Inject constructor(val getRecipe: GetRecipe): ViewModel() {
    val screenState: MutableState<RecipeScreenState> = mutableStateOf(RecipeScreenState.NORMAL)

    var recipeId: Int? = null
    var recipe: Recipe? = null
    var error: Error? = null

    suspend fun getRecipe(): Unit {
        if (screenState.value == RecipeScreenState.LOADING) {
            return
        }

        val asyncJob = viewModelScope.async(Dispatchers.IO) {
            screenState.value = RecipeScreenState.LOADING

            val recipeResponse: Response<Error, Recipe> = getRecipe.call(
                GetRecipeParams(
                    recipeId = recipeId!!
                )
            )

            if (!recipeResponse.isResponse()) {
                error = recipeResponse.error
                screenState.value = RecipeScreenState.ERROR
            }
            else {
                recipe = recipeResponse.response
                screenState.value = RecipeScreenState.NORMAL
            }
        }

        return asyncJob.await()
    }
}