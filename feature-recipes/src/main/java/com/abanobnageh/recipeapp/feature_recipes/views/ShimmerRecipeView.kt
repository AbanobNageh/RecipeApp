package com.abanobnageh.recipeapp.feature_recipes.views

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerRecipeView(
    colors: List<Color> = listOf(
        Color.LightGray.copy(0.9f),
        Color.LightGray.copy(0.2f),
        Color.LightGray.copy(0.9f),
    ),
    imageHeight: Dp = RECIPE_IMAGE_HEIGHT,
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

        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(xCardShimmer.value - gradiantWidth, yCardShimmer.value - gradiantWidth),
            end = Offset(xCardShimmer.value, yCardShimmer.value)
        )

        Column(modifier = Modifier.padding(padding)) {
            Surface(shape = MaterialTheme.shapes.small) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = imageHeight)
                        .background(brush = brush)

                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row() {
                Surface(shape = MaterialTheme.shapes.small) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(height = imageHeight / 10)
                            .background(brush = brush)

                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = MaterialTheme.shapes.small) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(height = imageHeight / 10)
                            .background(brush = brush)

                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            for (index in 1..5) {
                Surface(shape = MaterialTheme.shapes.small) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height = imageHeight / 10)
                            .background(brush = brush)

                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}