package com.example.sfsinstaller.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfsinstaller.models.InfoLevel
import com.example.sfsinstaller.models.InfoMsg
import com.example.sfsinstaller.utils.Constant
import com.example.sfsinstaller.utils.Network
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlin.random.Random

class MainViewModel(
    state: SavedStateHandle
) : ViewModel() {
    private val _appState = MutableStateFlow(
        AppState(
            crackPatchChecked = state["crackPatch"] ?: true,
            translationChecked = state["translation"] ?: true,
            infoList = state["infoList"] ?: emptyList()
        )
    )
    var appState: StateFlow<AppState> = _appState.asStateFlow()
    fun appendInfoText(info: String, level: InfoLevel) {
        _appState.update {
            it.copy(
                infoList = it.infoList + InfoMsg(info = info, level = level)
            )
        }
    }

    fun fetchInfomation() {
        viewModelScope.launch {
            try {
                val networkService = Network()
                val data = networkService.fetchDataAsString(Constant.REMOTE_LINK_URL)
                if (!data.isEmpty())
                    appendInfoText(info = "true", level = InfoLevel.LEVEL_INFO)
            } catch (e: Exception) {
                appendInfoText(info = e.message.toString(), level = InfoLevel.LEVEL_ERROR)
            }
        }
    }

    fun clearInfoText() {
        _appState.update {
            it.copy(
                infoList = emptyList()
            )
        }
    }

    fun toggleCrackPatch() {
        _appState.update {
            it.copy(crackPatchChecked = !it.crackPatchChecked)
        }
    }

    fun toggleTranslation() {
        _appState.update {
            it.copy(translationChecked = !it.translationChecked)
        }
    }
}

data class AppState(
    val infoList: List<InfoMsg> = emptyList(),
    val crackPatchChecked: Boolean = true,
    val translationChecked: Boolean = true,
    val remoteInfo: JsonElement? = null
)