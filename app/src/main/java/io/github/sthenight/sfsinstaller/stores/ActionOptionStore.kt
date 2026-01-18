package io.github.sthenight.sfsinstaller.stores

import io.github.sthenight.sfsinstaller.ui.states.ActionCheckedState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionOptionStore @Inject constructor() {
    private val _actionOptionState = MutableStateFlow(
        ActionCheckedState()
    )
    val actionOptionState: StateFlow<ActionCheckedState> = _actionOptionState.asStateFlow()
    fun setModPatchSelected(checked: Boolean) {
        _actionOptionState.update { current ->
            current.copy(isModPatchSelected = checked)
        }
    }
    fun setTranslationSelected(checked: Boolean) {
        _actionOptionState.update { current ->
            current.copy(isTranslationSelected = checked)
        }
    }
}