package com.example.sfsinstaller

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sfsinstaller.viewmodels.MainViewModel
import com.example.sfsinstaller.views.MainView

@Composable
fun MainNavigation(
    navHostController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel
) {
    Box() {
        NavHost(modifier = Modifier.fillMaxSize(), navController = navHostController, startDestination = "main") {
            composable("main") {
                MainView(mainViewModel)
            }
        }
    }
}