package com.abanobnageh.recipeapp.core.network

import com.google.gson.GsonBuilder
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitServiceBuilder {
    private const val RECIPE_API_URL = "https://food2fork.ca/api/recipe/"
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    fun <T> buildService(serviceType: Class<T>, baseUrl: String = RECIPE_API_URL): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl.toHttpUrl())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        return retrofit.create(serviceType)
    }
}
