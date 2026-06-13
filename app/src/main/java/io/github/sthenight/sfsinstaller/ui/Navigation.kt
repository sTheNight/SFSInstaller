package io.github.sthenight.sfsinstaller.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.sthenight.sfsinstaller.ui.screens.ActionScreen
import io.github.sthenight.sfsinstaller.ui.screens.MainScreen
import io.github.sthenight.sfsinstaller.ui.viewmodels.ActionViewModel
import io.github.sthenight.sfsinstaller.ui.viewmodels.MainViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
private data object MainRoute : NavKey

@Serializable
private data object ActionRoute : NavKey

private const val NAV_ANIMATION_DURATION = 280
private const val NAV_FADE_DURATION = 180
private const val NAV_SLIDE_DISTANCE_FACTOR = 5

private fun navEnterTransition(direction: Int): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(
            durationMillis = NAV_ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    ) { width -> direction * width / NAV_SLIDE_DISTANCE_FACTOR } +
        fadeIn(animationSpec = tween(durationMillis = NAV_FADE_DURATION)) +
        scaleIn(
            animationSpec = tween(
                durationMillis = NAV_ANIMATION_DURATION,
                easing = FastOutSlowInEasing
            ),
            initialScale = 0.98f
        )

private fun navExitTransition(direction: Int): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(
            durationMillis = NAV_ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    ) { width -> direction * width / NAV_SLIDE_DISTANCE_FACTOR } +
        fadeOut(animationSpec = tween(durationMillis = NAV_FADE_DURATION)) +
        scaleOut(
            animationSpec = tween(
                durationMillis = NAV_ANIMATION_DURATION,
                easing = FastOutSlowInEasing
            ),
            targetScale = 0.98f
        )

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(MainRoute)
    val mainViewModel = koinViewModel<MainViewModel>()
    val actionViewModel = koinViewModel<ActionViewModel>()

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
            navEnterTransition(direction = 1) togetherWith navExitTransition(direction = -1)
        },
        popTransitionSpec = {
            navEnterTransition(direction = -1) togetherWith navExitTransition(direction = 1)
        },
        predictivePopTransitionSpec = {
            navEnterTransition(direction = -1) togetherWith navExitTransition(direction = 1)
        }
    )
}
