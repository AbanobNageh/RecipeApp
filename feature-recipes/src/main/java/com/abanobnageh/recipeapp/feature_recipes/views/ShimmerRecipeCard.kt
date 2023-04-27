package com.abanobnageh.recipeapp.feature_recipes.views

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abanobnageh.recipeapp.core.theme.RecipeAppTheme

@Composable
fun ShimmerRecipeCard(
    colors: List<Color> = listOf(
        Color.LightGray.copy(0.9f),
        Color.LightGray.copy(0.2f),
        Color.LightGray.copy(0.9f),
    ),
    xShimmer: Float,
    yShimmer: Float,
    cardHeight: Dp,
    titleBarHeight: Dp,
    gradientWidth: Float,
    padding: Dp,
) {
    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(xShimmer - gradientWidth, yShimmer - gradientWidth),
        end = Offset(xShimmer, yShimmer)
    )

    Column(modifier = Modifier.padding(padding)) {
        Surface(shape = MaterialTheme.shapes.small) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = cardHeight)
                    .background(brush = brush)

            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(shape = MaterialTheme.shapes.small) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = titleBarHeight)
                    .background(brush = brush)

            )
        }
    }
}

@Composable
fun ShimmerRecipeList(
    imageHeight: Dp,
    padding: Dp = 16.dp,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val cardWidthPx = with(LocalDensity.current){ (maxWidth - (padding * 2)).toPx() }
        val cardHeightPx = with(LocalDensity.current){ (imageHeight - (padding)).toPx() }
        val gradiantWidth: Float = (0.2f * cardHeightPx)

        val infiniteTransition = rememberInfiniteTransition()
        val xCardShimmer = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (cardWidthPx + gradiantWidth),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1300,
                    easing = LinearEasing,
                    delayMillis = 300,
                ),
                repeatMode = RepeatMode.Restart,
            )
        )

        val yCardShimmer = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (cardHeightPx + gradiantWidth),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1300,
                    easing = LinearEasing,
                    delayMillis = 300,
                ),
                repeatMode = RepeatMode.Restart,
            )
        )

        LazyColumn {
            items(3) {
                ShimmerRecipeCard(
                    xShimmer = xCardShimmer.value,
                    yShimmer = yCardShimmer.value,
                    cardHeight = imageHeight,
                    titleBarHeight = imageHeight / 10,
                    gradientWidth = gradiantWidth,
                    padding = padding,
                )
            }
        }
    }
}

@Preview(
    group = "lightTheme",
    showBackground = true
)
@Preview(
    group = "darkTheme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun ShimmerRecipeListPreview() {
    RecipeAppTheme {
        ShimmerRecipeList(
            imageHeight = RECIPE_IMAGE_HEIGHT,
        )
    }
}