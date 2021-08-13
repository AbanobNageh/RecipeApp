package com.abanobnageh.recipeapp.feature_recipes.di

import com.abanobnageh.recipeapp.core.network.NetworkInfo
import com.abanobnageh.recipeapp.core.network.RecipeRetrofitService
import com.abanobnageh.recipeapp.feature_recipes.datasources.RecipeRemoteDataSource
import com.abanobnageh.recipeapp.feature_recipes.datasources.RecipeRemoteDataSourceImpl
import com.abanobnageh.recipeapp.feature_recipes.repositories.RecipeRepository
import com.abanobnageh.recipeapp.feature_recipes.repositories.RecipeRepositoryImpl
import com.abanobnageh.recipeapp.feature_recipes.usecases.GetRecipe
import com.abanobnageh.recipeapp.feature_recipes.usecases.SearchRecipes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object RecipeModule {

    @Provides
    fun provideRecipeRemoteDataSource(recipeRetrofitService: RecipeRetrofitService): RecipeRemoteDataSource {
        return RecipeRemoteDataSourceImpl(recipeRetrofitService)
    }

    @Provides
    fun provideRecipeRepository(recipeRemoteDataSource: RecipeRemoteDataSource, networkInfo: NetworkInfo): RecipeRepository {
        return RecipeRepositoryImpl(recipeRemoteDataSource, networkInfo)
    }

    @Provides
    fun provideSearchRecipesUseCase(recipeRepository: RecipeRepository): SearchRecipes {
        return SearchRecipes(recipeRepository)
    }

    @Provides
    fun provideGetRecipeUseCase(recipeRepository: RecipeRepository): GetRecipe {
        return GetRecipe(recipeRepository)
    }
}