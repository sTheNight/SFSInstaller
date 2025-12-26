package com.example.sfsinstaller.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sfsinstaller.ui.states.ActionCheckedState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ActionOptionStore(
    private val states: SavedStateHandle
): ViewModel() {
    private val _actionOptionState = MutableStateFlow(
        ActionCheckedState(
            isModPatchSelected = states.get<Boolean>("isModPatchSelected") ?: false,
            isTranslationSelected = states.get<Boolean>("isTranslationSelected") ?: false
        )
    )
    val actionOptionState: StateFlow<ActionCheckedState> = _actionOptionState.asStateFlow()
    fun setModPatchSelected(checked: Boolean) {
        _actionOptionState.update { current ->
            states["isModPatchSelected"] = checked
            current.copy(isModPatchSelected = checked)
        }
    }

    fun setTranslationSelected(checked: Boolean) {
        _actionOptionState.update { current ->
            states["isTranslationSelected"] = checked
            current.copy(isTranslationSelected = checked)
        }
    }
}