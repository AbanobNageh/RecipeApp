package com.abanobnageh.recipeapp.feature_recipes.usecases

import com.abanobnageh.recipeapp.core.network.RecipeRetrofitService
import com.abanobnageh.recipeapp.core.network.RetrofitServiceBuilder
import com.abanobnageh.recipeapp.data.models.network.RecipeDto
import com.abanobnageh.recipeapp.data.models.network.RecipeSearchResponseDto
import com.abanobnageh.recipeapp.feature_recipes.datasources.RecipeRemoteDataSourceImpl
import com.abanobnageh.recipeapp.feature_recipes.datasources.RecipeRemoteDataSourceTestImpl
import com.abanobnageh.recipeapp.feature_recipes.repositories.NetworkInfoTestImpl
import com.abanobnageh.recipeapp.feature_recipes.repositories.RecipeRepositoryImpl
import com.abanobnageh.recipeapp.feature_recipes.repositories.RecipeRepositoryTestImpl
import com.abanobnageh.recipeapp.feature_recipes.utils.MockResponseFileReader
import com.google.common.truth.Truth
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import java.net.HttpURLConnection

class SearchRecipesTest {
    private lateinit var actualRemoteDataSource: RecipeRemoteDataSourceImpl
    private lateinit var mockRemoteDataSource: RecipeRemoteDataSourceTestImpl
    private lateinit var mockWebServer: MockWebServer

    private lateinit var actualRepository: RecipeRepositoryImpl
    private lateinit var mockRepository: RecipeRepositoryTestImpl

    private lateinit var mockNetworkInfo: NetworkInfoTestImpl

    private lateinit var actualSearchRecipesUsecase: SearchRecipes
    private lateinit var mockSearchRecipesUsecase: SearchRecipes

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val service: RecipeRetrofitService = RetrofitServiceBuilder.buildService(
            RecipeRetrofitService::class.java,
            mockWebServer.url("/").toString()
        )
        actualRemoteDataSource = RecipeRemoteDataSourceImpl(service)
        mockRemoteDataSource = RecipeRemoteDataSourceTestImpl()

        mockNetworkInfo = NetworkInfoTestImpl()

        actualRepository = RecipeRepositoryImpl(actualRemoteDataSource, mockNetworkInfo)
        mockRepository = RecipeRepositoryTestImpl(mockRemoteDataSource, mockNetworkInfo)

        actualSearchRecipesUsecase = SearchRecipes(actualRepository)
        mockSearchRecipesUsecase = SearchRecipes(mockRepository)
    }

    @After
    fun clear() {
        mockWebServer.shutdown()
    }

    @Test
    fun `calling SearchRecipes usecase returns the correct information`() {
        runBlocking {
            val mockResponseBody = Gson().fromJson(MockResponseFileReader("get_recipes_success.json").content, RecipeSearchResponseDto::class.java)
            val mockHTTPResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(mockResponseBody))
            mockWebServer.enqueue(mockHTTPResponse)

            mockNetworkInfo.setIsConnected(true)

            val actualResponse = actualSearchRecipesUsecase.call(
                SearchRecipesParams(
                    query = "",
                    pageNumber = 1,
                )
            )
            val mockResponse = mockSearchRecipesUsecase.call(
                SearchRecipesParams(
                    query = "",
                    pageNumber = 1,
                )
            )

            Truth.assertThat(mockResponse.response).isNotNull()
            Truth.assertThat(actualResponse.response).isNotNull()
            Truth.assertThat(mockResponse.error).isNull()
            Truth.assertThat(actualResponse.error).isNull()
            Truth.assertThat(mockResponse.response).isEqualTo(actualResponse.response)
        }
    }
}