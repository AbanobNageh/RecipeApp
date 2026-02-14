package com.abanobnageh.recipeapp.core.network

import com.abanobnageh.recipeapp.data.models.network.RecipeDto
import com.abanobnageh.recipeapp.data.models.network.RecipeDetailResponseDto
import com.abanobnageh.recipeapp.data.models.network.RecipeDetailDataDto
import com.abanobnageh.recipeapp.data.models.network.RecipeSearchResponseDto
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import java.net.HttpURLConnection

class RecipeRetrofitServiceTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var service: RecipeRetrofitService

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        service = RetrofitServiceBuilder.buildService(
            RecipeRetrofitService::class.java,
            mockWebServer.url("/").toString()
        )
    }

    @After
    fun clear() {
        mockWebServer.shutdown()
    }

    @Test
    fun `read sample success json file`() {
        val reader = MockResponseFileReader("get_recipe_success.json")
        assertThat(reader.content).isNotNull()
    }

    @Test
    fun `search recipes with empty query and check received data is equal to expected data`() {
        runTest {
            val mockResponseBody = Gson().fromJson(MockResponseFileReader("get_recipes_success.json").content, RecipeSearchResponseDto::class.java)
            val mockResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(mockResponseBody))
            mockWebServer.enqueue(mockResponse)

            val actualResponse = service.searchRecipe(
                query = "",
            ).execute()

            val mockBodyString = Gson().toJson(mockResponseBody)
            val actualBodyString = Gson().toJson(actualResponse.body())

            assertThat(mockBodyString).isEqualTo(actualBodyString)
        }
    }

    @Test
    fun `fetch recipe with id = 1 and check received data is equal to expected data`() {
        runTest {
            val mockResponseBody = Gson().fromJson(MockResponseFileReader("get_recipe_success.json").content, RecipeDetailResponseDto::class.java)
            val mockResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(mockResponseBody))
            mockWebServer.enqueue(mockResponse)

            val actualResponse = service.getRecipe(
                id = "1",
            ).execute()

            val mockBodyString = Gson().toJson(mockResponseBody)
            val actualBosyString = Gson().toJson(actualResponse.body())

            assertThat(mockBodyString).isEqualTo(actualBosyString)
        }
    }
}












