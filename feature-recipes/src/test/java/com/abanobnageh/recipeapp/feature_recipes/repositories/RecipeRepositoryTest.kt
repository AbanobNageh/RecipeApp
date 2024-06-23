package com.abanobnageh.recipeapp.feature_recipes.repositories

import com.abanobnageh.recipeapp.core.error.Error
import com.abanobnageh.recipeapp.core.error.NoInternetError
import com.abanobnageh.recipeapp.core.network.NetworkInfo
import com.abanobnageh.recipeapp.core.network.RecipeRetrofitService
import com.abanobnageh.recipeapp.core.network.RetrofitServiceBuilder
import com.abanobnageh.recipeapp.core.usecase.Response
import com.abanobnageh.recipeapp.data.models.domain.Recipe
import com.abanobnageh.recipeapp.data.models.domain.RecipeSearchResponse
import com.abanobnageh.recipeapp.data.models.network.RecipeDto
import com.abanobnageh.recipeapp.data.models.network.RecipeSearchResponseDto
import com.abanobnageh.recipeapp.feature_recipes.datasources.RecipeRemoteDataSourceImpl
import com.abanobnageh.recipeapp.feature_recipes.datasources.RecipeRemoteDataSourceTestImpl
import com.abanobnageh.recipeapp.feature_recipes.utils.MockResponseFileReader
import com.google.common.truth.Truth
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import java.net.HttpURLConnection

class RecipeRepositoryTest {
    private lateinit var actualRemoteDataSource: RecipeRemoteDataSourceImpl
    private lateinit var mockRemoteDataSource: RecipeRemoteDataSourceTestImpl
    private lateinit var mockWebServer: MockWebServer

    private lateinit var actualRepository: RecipeRepositoryImpl
    private lateinit var mockRepository: RecipeRepositoryTestImpl

    private lateinit var mockNetworkInfo: NetworkInfoTestImpl

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
    }

    @After
    fun clear() {
        mockWebServer.shutdown()
    }

    @Test
    fun `calling searchRecipes on the repository returns the correct information`() {
        runTest {
            val mockResponseBody = Gson().fromJson(MockResponseFileReader("get_recipes_success.json").content, RecipeSearchResponseDto::class.java)
            val mockHTTPResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(mockResponseBody))
            mockWebServer.enqueue(mockHTTPResponse)

            mockNetworkInfo.setIsConnected(true)

            val actualResponse = actualRepository.searchRecipes(
                pageNumber = 1,
                query = "",
            )
            val mockResponse = mockRepository.searchRecipes(
                pageNumber = 1,
                query = "",
            )

            Truth.assertThat(mockResponse.response).isNotNull()
            Truth.assertThat(actualResponse.response).isNotNull()
            Truth.assertThat(mockResponse.error).isNull()
            Truth.assertThat(actualResponse.error).isNull()
            Truth.assertThat(mockResponse.response).isEqualTo(actualResponse.response)
        }
    }

    @Test
    fun `calling getRecipe on the repository returns the correct information`() {
        runTest {
            val mockResponseBody = Gson().fromJson(MockResponseFileReader("get_recipe_success.json").content, RecipeDto::class.java)
            val mockHTTPResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(mockResponseBody))
            mockWebServer.enqueue(mockHTTPResponse)

            mockNetworkInfo.setIsConnected(true)

            val actualResponse = actualRepository.getRecipe(
                recipeId = 1,
            )
            val mockResponse = mockRepository.getRecipe(
                recipeId = 1,
            )

            Truth.assertThat(mockResponse.response).isNotNull()
            Truth.assertThat(actualResponse.response).isNotNull()
            Truth.assertThat(mockResponse.error).isNull()
            Truth.assertThat(actualResponse.error).isNull()
            Truth.assertThat(mockResponse.response).isEqualTo(actualResponse.response)
        }
    }

    @Test
    fun `calling searchRecipes on the repository when the internet is disconnected returns an error`() {
        runTest {
            val mockResponseBody = Gson().fromJson(MockResponseFileReader("get_recipes_success.json").content, RecipeSearchResponseDto::class.java)
            val mockHTTPResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(mockResponseBody))
            mockWebServer.enqueue(mockHTTPResponse)

            mockNetworkInfo.setIsConnected(false)

            val actualResponse = actualRepository.searchRecipes(
                pageNumber = 1,
                query = "",
            )
            val mockResponse = mockRepository.searchRecipes(
                pageNumber = 1,
                query = "",
            )

            Truth.assertThat(mockResponse.error).isNotNull()
            Truth.assertThat(actualResponse.error).isNotNull()
            Truth.assertThat(mockResponse.response).isNull()
            Truth.assertThat(actualResponse.response).isNull()
            Truth.assertThat(mockResponse.error).isInstanceOf(NoInternetError::class.java)
            Truth.assertThat(actualResponse.error).isInstanceOf(NoInternetError::class.java)
        }
    }

    @Test
    fun `calling getRecipe on the repository when the internet is disconnected returns an error`() {
        runTest {
            val mockResponseBody = Gson().fromJson(MockResponseFileReader("get_recipes_success.json").content, RecipeSearchResponseDto::class.java)
            val mockHTTPResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(mockResponseBody))
            mockWebServer.enqueue(mockHTTPResponse)

            mockNetworkInfo.setIsConnected(false)

            val actualResponse = actualRepository.getRecipe(
                recipeId = 1,
            )
            val mockResponse = mockRepository.getRecipe(
                recipeId = 1,
            )

            Truth.assertThat(mockResponse.error).isNotNull()
            Truth.assertThat(actualResponse.error).isNotNull()
            Truth.assertThat(mockResponse.response).isNull()
            Truth.assertThat(actualResponse.response).isNull()
            Truth.assertThat(mockResponse.error).isInstanceOf(NoInternetError::class.java)
            Truth.assertThat(actualResponse.error).isInstanceOf(NoInternetError::class.java)
        }
    }
}

class RecipeRepositoryTestImpl(
    val recipeRemoteDataSourceTestImpl: RecipeRemoteDataSourceTestImpl,
    val networkInfo: NetworkInfoTestImpl,
) : RecipeRepository {
    override suspend fun searchRecipes(query: String, pageNumber: Int): Response<Error, RecipeSearchResponse> {
        return if (networkInfo.isInternetConnected()) {
            Response(null, recipeRemoteDataSourceTestImpl.searchRecipes(query, pageNumber).mapToNetworkModel())
        } else {
            Response(NoInternetError(), null)
        }
    }

    override suspend fun getRecipe(recipeId: Int): Response<Error, Recipe> {
        return if (networkInfo.isInternetConnected()) {
            Response(null, recipeRemoteDataSourceTestImpl.getRecipe(recipeId).mapToDomainModel())
        } else {
            Response(NoInternetError(), null)
        }
    }
}

class NetworkInfoTestImpl: NetworkInfo {
    private var isConnected: Boolean = false

    override suspend fun isInternetConnected(): Boolean {
        return isConnected
    }

    fun setIsConnected(isConnected: Boolean) {
        this.isConnected = isConnected
    }
}