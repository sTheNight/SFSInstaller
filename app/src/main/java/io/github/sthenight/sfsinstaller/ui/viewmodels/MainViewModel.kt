package io.github.sthenight.sfsinstaller.ui.viewmodels

import androidx.lifecycle.ViewModel
import io.github.sthenight.sfsinstaller.stores.ActionOptionStore

class MainViewModel(
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
