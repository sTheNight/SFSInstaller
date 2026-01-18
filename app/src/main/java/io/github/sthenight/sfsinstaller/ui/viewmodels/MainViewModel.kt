package io.github.sthenight.sfsinstaller.ui.viewmodels

import androidx.lifecycle.ViewModel
import io.github.sthenight.sfsinstaller.ui.stores.ActionOptionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val actionOptionStore: ActionOptionStore
) : ViewModel() {
    val actionState = actionOptionStore.actionOptionState
    fun setModPatchSelected(checked: Boolean) {
        actionOptionStore.setModPatchSelected(checked)
    }
    fun setTranslationSelected(checked: Boolean) {
        actionOptionStore.setTranslationSelected(checked)
    }
}