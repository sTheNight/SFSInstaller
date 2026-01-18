package io.github.sthenight.sfsinstaller.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.sthenight.sfsinstaller.ui.screens.ActionScreen
import io.github.sthenight.sfsinstaller.ui.screens.MainScreen
import io.github.sthenight.sfsinstaller.ui.viewmodels.ActionViewModel
import io.github.sthenight.sfsinstaller.ui.viewmodels.MainViewModel

@Composable
fun MainNavigation(
    navHostController: NavHostController = rememberNavController()
) {
    val mainViewModel = hiltViewModel<MainViewModel>()
    val actionViewModel = hiltViewModel<ActionViewModel>()
    Box() {
        NavHost(modifier = Modifier.fillMaxSize(), navController = navHostController, startDestination = "main") {
            composable("main") {
                MainScreen(
                    mainViewModel = mainViewModel,
                    switchToActionScreen = {
                        navHostController.navigate("action")
                    })
            }
            composable("action") {
                ActionScreen(
                    actionViewModel = actionViewModel,
                    back = {
                        navHostController.popBackStack()
                    }
                )
            }
        }
    }
}