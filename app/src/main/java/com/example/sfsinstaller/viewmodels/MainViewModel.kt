package com.example.sfsinstaller.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _appState = MutableStateFlow(AppState())
    var appState: StateFlow<AppState> = _appState.asStateFlow()
    fun appendInfoText() {
        _appState.update {
            it.copy(
                InfoText = it.InfoText + "Hello World\n"
            )
        }
    }
}

data class AppState(
    var InfoText: String = ""
)