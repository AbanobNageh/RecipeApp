package com.abanobnageh.recipeapp.core.utils

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator

/**
 * Navigator
 */
private val results = mutableStateMapOf<String, Any?>()

fun Navigator.popWithResult(result: Any? = null) {
    val currentScreen = lastItem
    results[currentScreen.key] = result
    pop()
}

fun Navigator.popUntilWithResult(predicate: (Screen) -> Boolean, result: Any?) {
    val currentScreen = lastItem
    results[currentScreen.key] = result
    popUntil(predicate)
}

fun Navigator.clearResults() {
    results.clear()
}

@Composable
fun <T> Navigator.getResult(screenKey: String): State<T?> {
    val result = results[screenKey] as? T
    val resultState = remember(screenKey, result) {
        derivedStateOf {
            results.remove(screenKey)
            result
        }
    }
    return resultState
}

/**
 * bottom sheet navigator
 */


@OptIn(ExperimentalMaterialApi::class)
fun BottomSheetNavigator.hideWithResult(result: Any?, key: String) {
    results[key] = result
    this.hide()

}

@Composable
fun <T> BottomSheetNavigator.getResult(screenKey: String): State<T?> {
    val result = results[screenKey] as? T
    val resultState = remember(screenKey, result) {
        derivedStateOf {
            results -= screenKey
            result
        }
    }
    return resultState
}