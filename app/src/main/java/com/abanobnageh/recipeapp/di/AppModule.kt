package com.abanobnageh.recipeapp.di

import android.app.Application
import com.abanobnageh.recipeapp.BaseApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext application: Application): BaseApplication {
        return application as BaseApplication
    }
}