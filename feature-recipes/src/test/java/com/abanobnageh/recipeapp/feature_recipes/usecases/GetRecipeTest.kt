package com.abanobnageh.recipeapp.feature_recipes.usecases

import com.abanobnageh.recipeapp.core.network.RecipeRetrofitService
import com.abanobnageh.recipeapp.core.network.RetrofitServiceBuilder
import com.abanobnageh.recipeapp.data.models.network.RecipeDto
import com.abanobnageh.recipeapp.data.models.network.RecipeDetailResponseDto
import com.abanobnageh.recipeapp.data.models.network.RecipeDetailDataDto
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
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import java.net.HttpURLConnection

class GetRecipeTest {
    private lateinit var actualRemoteDataSource: RecipeRemoteDataSourceImpl
    private lateinit var mockRemoteDataSource: RecipeRemoteDataSourceTestImpl
    private lateinit var mockWebServer: MockWebServer

    private lateinit var actualRepository: RecipeRepositoryImpl
    private lateinit var mockRepository: RecipeRepositoryTestImpl

    private lateinit var mockNetworkInfo: NetworkInfoTestImpl

    private lateinit var actualGetRecipeUsecase: GetRecipe
    private lateinit var mockGetRecipeUsecase: GetRecipe

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

        mockNetworkInfo = NetworkInfoTestImpl()

        actualRepository = RecipeRepositoryImpl(actualRemoteDataSource, mockNetworkInfo)
        mockRepository = RecipeRepositoryTestImpl(mockRemoteDataSource, mockNetworkInfo)

        actualGetRecipeUsecase = GetRecipe(actualRepository)
        mockGetRecipeUsecase = GetRecipe(mockRepository)
    }

    @After
    fun clear() {
        mockWebServer.shutdown()
    }

    @Test
    fun `calling GetRecipe usecase returns the correct information`() {
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

            mockNetworkInfo.setIsConnected(true)

            val actualResponse = actualGetRecipeUsecase.call(
                GetRecipeParams(
                    recipeId = "1",
                )
            )
            val mockResponse = mockGetRecipeUsecase.call(
                GetRecipeParams(
                    recipeId = "1",
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