package com.abanobnageh.recipeapp.feature_recipes.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.abanobnageh.recipeapp.data.models.domain.FoodCategory

@Composable
fun AppBar(
    searchText: String,
    onTextChanged: (String) -> Unit,
    onSearch: () -> Unit,
    foodCategoryListState: LazyListState,
    selectedFoodCategory: FoodCategory?,
    setSelectedFoodCategory: (String) -> Unit,
    setSelectedFoodCategoryIndex: (Int) -> Unit,
    onToggleTheme: () -> Unit,
) {
    Surface(
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth(),
        color = MaterialTheme.colors.surface,
    ) {
        Column() {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                TextField(
                    value = searchText,
                    onValueChange = onTextChanged,
                    modifier = Modifier
                        .fillMaxWidth(0.9F)
                        .padding(8.dp),
                    label = @Composable {
                        Text(text = "Search")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search,
                    ),
                    leadingIcon = @Composable {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                        )
                    },
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearch()
                        }
                    ),
                    textStyle = TextStyle(MaterialTheme.colors.onSurface),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = MaterialTheme.colors.surface,
                    )
                )
                Icon(
                    modifier = Modifier.clickable {
                        onToggleTheme()
                    },
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "",
                )
            }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, bottom = 8.dp),
                state = foodCategoryListState
            ) {
                itemsIndexed(
                    items = FoodCategory.getAllFoodCategories()
                ) { index, foodCategory ->
                    FoodCategoryChip(
                        foodCategory = foodCategory.foodCategory,
                        isSelected = foodCategory.foodCategory == selectedFoodCategory?.foodCategory,
                        onSelected = {
                            setSelectedFoodCategory(foodCategory.foodCategory)
                            setSelectedFoodCategoryIndex(index)
                        },
                        onSearch = { onSearch() },
                    )
                }
            }
        }
    }
}