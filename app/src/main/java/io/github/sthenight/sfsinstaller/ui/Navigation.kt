package io.github.sthenight.sfsinstaller.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.sthenight.sfsinstaller.ui.screens.ActionScreen
import io.github.sthenight.sfsinstaller.ui.screens.MainScreen
import io.github.sthenight.sfsinstaller.ui.viewmodels.ActionViewModel
import io.github.sthenight.sfsinstaller.ui.viewmodels.MainViewModel
import kotlinx.serialization.Serializable

@Serializable
private data object MainRoute : NavKey

@Serializable
private data object ActionRoute : NavKey

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(MainRoute)
    val mainViewModel = hiltViewModel<MainViewModel>()
    val actionViewModel = hiltViewModel<ActionViewModel>()

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = backStack,
        onBack = { navigateBack() },
        entryProvider = entryProvider {
            entry<MainRoute> {
                MainScreen(
                    mainViewModel = mainViewModel,
                    switchToActionScreen = { backStack.add(ActionRoute) }
                )
            }
            entry<ActionRoute> {
                ActionScreen(
                    actionViewModel = actionViewModel,
                    back = { navigateBack() }
                )
            }
        },
        transitionSpec = {
            slideInHorizontally { it -> it } togetherWith fadeOut()
        },
        popTransitionSpec = {
            fadeIn() togetherWith slideOutHorizontally { it -> -it }
        },
        predictivePopTransitionSpec = {
            fadeIn() togetherWith scaleOut(
                transformOrigin = TransformOrigin(0.2f, 0.8f),
                targetScale = 0.8f
            )
        }
    )
}
