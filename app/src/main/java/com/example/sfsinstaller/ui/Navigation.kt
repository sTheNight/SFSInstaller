package com.example.sfsinstaller.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sfsinstaller.ui.screens.ActionScreen
import com.example.sfsinstaller.ui.viewmodels.MainViewModel
import com.example.sfsinstaller.ui.screens.MainScreen
import com.example.sfsinstaller.ui.viewmodels.ActionOptionStore
import com.example.sfsinstaller.ui.viewmodels.ActionViewModel

@Composable
fun MainNavigation(
    navHostController: NavHostController = rememberNavController()
) {
    val mainViewModel: MainViewModel = viewModel()
    val actionOptionStore: ActionOptionStore = viewModel()
    val actionViewModel = ActionViewModel(actionOptionStore = actionOptionStore)
    Box() {
        NavHost(modifier = Modifier.fillMaxSize(), navController = navHostController, startDestination = "main") {
            composable("main") {
                MainScreen(
                    mainViewModel = mainViewModel,
                    actionOptionStore = actionOptionStore,
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