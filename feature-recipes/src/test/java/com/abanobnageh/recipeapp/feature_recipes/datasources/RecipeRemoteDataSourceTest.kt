package com.abanobnageh.recipeapp.feature_recipes.datasources

import com.abanobnageh.recipeapp.core.network.RecipeRetrofitService
import com.abanobnageh.recipeapp.core.network.RetrofitServiceBuilder
import com.abanobnageh.recipeapp.data.models.network.RecipeDto
import com.abanobnageh.recipeapp.data.models.network.RecipeDetailResponseDto
import com.abanobnageh.recipeapp.data.models.network.RecipeDetailDataDto
import com.abanobnageh.recipeapp.data.models.network.RecipeSearchResponseDto
import com.abanobnageh.recipeapp.feature_recipes.utils.MockResponseFileReader
import com.google.gson.Gson
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import java.net.HttpURLConnection

class RecipeRemoteDataSourceTest {
    private lateinit var actualRemoteDataSource: RecipeRemoteDataSourceImpl
    private lateinit var mockRemoteDataSource: RecipeRemoteDataSourceTestImpl
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val service: RecipeRetrofitService = RetrofitServiceBuilder.buildService(
            RecipeRetrofitService::class.java,
            mockWebServer.url("/").toString()
        )
        actualRemoteDataSource = RecipeRemoteDataSourceImpl(service)
        mockRemoteDataSource = RecipeRemoteDataSourceTestImpl()
    }

    @After
    fun clear() {
        mockWebServer.shutdown()
    }

    @Test
    fun `calling searchRecipes on the remote data source returns the correct information`() {
        runTest {
            val mockResponseBody = Gson().fromJson(MockResponseFileReader("get_recipes_success.json").content, RecipeSearchResponseDto::class.java)
            val mockHTTPResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(mockResponseBody))
            mockWebServer.enqueue(mockHTTPResponse)

            val actualResponse = actualRemoteDataSource.searchRecipes(
                query = "",
            )
            val mockResponse = mockRemoteDataSource.searchRecipes(
                query = "",
            )

            assertThat(mockResponse).isEqualTo(actualResponse)
        }
    }

    @Test
    fun `calling getRecipe on the remote data source returns the correct information`() {
        runTest {
            val mockRecipeDto = Gson().fromJson(MockResponseFileReader("get_recipe_success.json").content, RecipeDto::class.java)
            // Wrap in RecipeDetailResponseDto as the API does
            val mockResponseBody = RecipeDetailResponseDto(
                status = "success",
                data = RecipeDetailDataDto(recipe = mockRecipeDto)
            )
            val mockHTTPResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(mockResponseBody))
            mockWebServer.enqueue(mockHTTPResponse)

            val actualResponse = actualRemoteDataSource.getRecipe(
                recipeId = "1",
            )
            val mockResponse = mockRemoteDataSource.getRecipe(
                recipeId = "1",
            )

            assertThat(mockResponse).isEqualTo(actualResponse)
        }
    }
}

class RecipeRemoteDataSourceTestImpl: RecipeRemoteDataSource {
    override suspend fun searchRecipes(query: String): RecipeSearchResponseDto {
        return Gson().fromJson(MockResponseFileReader("get_recipes_success.json").content, RecipeSearchResponseDto::class.java)
    }

    override suspend fun getRecipe(recipeId: String): RecipeDto {
        return Gson().fromJson(MockResponseFileReader("get_recipe_success.json").content, RecipeDto::class.java)
    }
}