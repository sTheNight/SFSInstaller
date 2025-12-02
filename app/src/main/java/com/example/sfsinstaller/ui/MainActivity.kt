package com.example.sfsinstaller.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.sfsinstaller.ui.theme.SFSInstallerTheme
import com.example.sfsinstaller.ui.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SFSInstallerTheme {
                MainNavigation()
            }
        }
    }
}