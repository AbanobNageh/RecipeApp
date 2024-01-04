package com.abanobnageh.recipeapp.feature_recipes.viewmodels

import com.abanobnageh.recipeapp.core.network.RecipeRetrofitService
import com.abanobnageh.recipeapp.core.network.RetrofitServiceBuilder
import com.abanobnageh.recipeapp.data.models.domain.FoodCategory
import com.abanobnageh.recipeapp.data.models.network.RecipeSearchResponseDto
import com.abanobnageh.recipeapp.feature_recipes.datasources.RecipeRemoteDataSourceImpl
import com.abanobnageh.recipeapp.feature_recipes.datasources.RecipeRemoteDataSourceTestImpl
import com.abanobnageh.recipeapp.feature_recipes.repositories.NetworkInfoTestImpl
import com.abanobnageh.recipeapp.feature_recipes.repositories.RecipeRepositoryImpl
import com.abanobnageh.recipeapp.feature_recipes.repositories.RecipeRepositoryTestImpl
import com.abanobnageh.recipeapp.feature_recipes.usecases.GetRecipe
import com.abanobnageh.recipeapp.feature_recipes.usecases.SearchRecipes
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

class RecipeListScreenViewModelTest {
    private lateinit var actualRemoteDataSource: RecipeRemoteDataSourceImpl
    private lateinit var mockRemoteDataSource: RecipeRemoteDataSourceTestImpl
    private lateinit var mockWebServer: MockWebServer

    private lateinit var actualRepository: RecipeRepositoryImpl
    private lateinit var mockRepository: RecipeRepositoryTestImpl

    private lateinit var mockNetworkInfo: NetworkInfoTestImpl

    private lateinit var actualSearchRecipesUsecase: SearchRecipes
    private lateinit var mockSearchRecipesUsecase: SearchRecipes

    private lateinit var actualRecipeListScreenViewModel: RecipeListScreenViewModel
    private lateinit var mockRecipeListScreenViewModel: RecipeListScreenViewModel

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

        actualSearchRecipesUsecase = SearchRecipes(actualRepository)
        mockSearchRecipesUsecase = SearchRecipes(mockRepository)

        actualRecipeListScreenViewModel = RecipeListScreenViewModel(actualSearchRecipesUsecase)
        mockRecipeListScreenViewModel = RecipeListScreenViewModel(mockSearchRecipesUsecase)
    }

    @After
    fun clear() {
        mockWebServer.shutdown()
    }

    @Test
    fun `Calling getRecipesList on the view model returns the correct data`() {
        runTest {
            val mockResponseBody = Gson().fromJson(MockResponseFileReader("get_recipes_success.json").content, RecipeSearchResponseDto::class.java)
            val mockHTTPResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(mockResponseBody))
            mockWebServer.enqueue(mockHTTPResponse)

            mockNetworkInfo.setIsConnected(true)

            actualRecipeListScreenViewModel.getRecipesList()
            mockRecipeListScreenViewModel.getRecipesList()

            Truth.assertThat(actualRecipeListScreenViewModel.recipes).isNotEmpty()
            Truth.assertThat(mockRecipeListScreenViewModel.recipes).isNotEmpty()
            Truth.assertThat(actualRecipeListScreenViewModel.error).isNull()
            Truth.assertThat(mockRecipeListScreenViewModel.error).isNull()
            Truth.assertThat(actualRecipeListScreenViewModel.recipes).isEqualTo(mockRecipeListScreenViewModel.recipes)
        }
    }

    @Test
    fun `Calling incrementPageNumber increases the page number by one`() {
        actualRecipeListScreenViewModel.paginationPageNumber = 1
        mockRecipeListScreenViewModel.paginationPageNumber = 1

        actualRecipeListScreenViewModel.incrementPageNumber()
        mockRecipeListScreenViewModel.incrementPageNumber()

        Truth.assertThat(actualRecipeListScreenViewModel.paginationPageNumber).isEqualTo(2)
        Truth.assertThat(mockRecipeListScreenViewModel.paginationPageNumber).isEqualTo(2)
    }

    @Test
    fun `Calling setSearchText nulls food category and sets search text when a non food category search text is passed`() {
        actualRecipeListScreenViewModel.setSearchText("test")
        mockRecipeListScreenViewModel.setSearchText("test")

        Truth.assertThat(actualRecipeListScreenViewModel.selectedFoodCategory.value).isNull()
        Truth.assertThat(mockRecipeListScreenViewModel.selectedFoodCategory.value).isNull()
        Truth.assertThat(actualRecipeListScreenViewModel.searchText.value).isEqualTo("test")
        Truth.assertThat(mockRecipeListScreenViewModel.searchText.value).isEqualTo("test")
    }

    @Test
    fun `Calling setSearchText sets both search text and food category when a food category search text is passed`() {
        actualRecipeListScreenViewModel.setSearchText("Chicken")
        mockRecipeListScreenViewModel.setSearchText("Chicken")

        Truth.assertThat(actualRecipeListScreenViewModel.selectedFoodCategory.value).isEqualTo(FoodCategory.CHICKEN)
        Truth.assertThat(mockRecipeListScreenViewModel.selectedFoodCategory.value).isEqualTo(FoodCategory.CHICKEN)
        Truth.assertThat(actualRecipeListScreenViewModel.searchText.value).isEqualTo("Chicken")
        Truth.assertThat(mockRecipeListScreenViewModel.searchText.value).isEqualTo("Chicken")
    }

    @Test
    fun `Calling setSelectedFoodCategory set the food category and search text when passing a correct food category`() {
        actualRecipeListScreenViewModel.setSelectedFoodCategory("Chicken")
        mockRecipeListScreenViewModel.setSelectedFoodCategory("Chicken")

        Truth.assertThat(actualRecipeListScreenViewModel.selectedFoodCategory.value).isEqualTo(FoodCategory.CHICKEN)
        Truth.assertThat(mockRecipeListScreenViewModel.selectedFoodCategory.value).isEqualTo(FoodCategory.CHICKEN)
        Truth.assertThat(actualRecipeListScreenViewModel.searchText.value).isEqualTo("Chicken")
        Truth.assertThat(mockRecipeListScreenViewModel.searchText.value).isEqualTo("Chicken")
    }

    @Test
    fun `Calling setSelectedFoodCategory nulls food category and empties search text when passing an incorrect food category`() {
        actualRecipeListScreenViewModel.setSelectedFoodCategory("test")
        mockRecipeListScreenViewModel.setSelectedFoodCategory("test")

        Truth.assertThat(actualRecipeListScreenViewModel.selectedFoodCategory.value).isNull()
        Truth.assertThat(mockRecipeListScreenViewModel.selectedFoodCategory.value).isNull()
        Truth.assertThat(actualRecipeListScreenViewModel.searchText.value).isEqualTo("")
        Truth.assertThat(mockRecipeListScreenViewModel.searchText.value).isEqualTo("")
    }

    @Test
    fun `Calling resetSearch resets all search attributes`() {
        runTest {
            actualRecipeListScreenViewModel.getRecipesList()
            mockRecipeListScreenViewModel.getRecipesList()

            actualRecipeListScreenViewModel.isPaginationDone = true
            actualRecipeListScreenViewModel.paginationPageNumber = 2
            mockRecipeListScreenViewModel.isPaginationDone = true
            mockRecipeListScreenViewModel.paginationPageNumber = 2

            actualRecipeListScreenViewModel.resetSearch()
            mockRecipeListScreenViewModel.resetSearch()

            Truth.assertThat(actualRecipeListScreenViewModel.isPaginationDone).isEqualTo(false)
            Truth.assertThat(actualRecipeListScreenViewModel.paginationPageNumber).isEqualTo(1)
            Truth.assertThat(actualRecipeListScreenViewModel.recipes).isEmpty()
            Truth.assertThat(mockRecipeListScreenViewModel.isPaginationDone).isEqualTo(false)
            Truth.assertThat(mockRecipeListScreenViewModel.paginationPageNumber).isEqualTo(1)
            Truth.assertThat(mockRecipeListScreenViewModel.recipes).isEmpty()
        }
    }

    @Test
    fun `Calling setSelectedFoodCategoryIndex sets the setSelectedFoodCategoryIndex`() {
        actualRecipeListScreenViewModel.setSelectedFoodCategoryIndex(2)
        mockRecipeListScreenViewModel.setSelectedFoodCategoryIndex(2)

        Truth.assertThat(actualRecipeListScreenViewModel.selectedFoodCategoryIndex).isEqualTo(2)
        Truth.assertThat(mockRecipeListScreenViewModel.selectedFoodCategoryIndex).isEqualTo(2)
    }
}