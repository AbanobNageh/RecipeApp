package com.abanobnageh.recipeapp.di

import com.abanobnageh.recipeapp.core.network.NetworkInfo
import com.abanobnageh.recipeapp.core.network.NetworkInfoImpl
import com.abanobnageh.recipeapp.core.network.RecipeRetrofitService
import com.abanobnageh.recipeapp.core.network.RetrofitServiceBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideRecipeRetrofitService(): RecipeRetrofitService {
        return RetrofitServiceBuilder.buildService(RecipeRetrofitService::class.java)
    }

    @Singleton
    @Provides
    fun provideNetworkInfo(): NetworkInfo {
        return NetworkInfoImpl()
    }
}