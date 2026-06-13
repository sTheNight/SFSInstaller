package io.github.sthenight.sfsinstaller.di

import io.github.sthenight.sfsinstaller.stores.ActionOptionStore
import io.github.sthenight.sfsinstaller.ui.viewmodels.ActionViewModel
import io.github.sthenight.sfsinstaller.ui.viewmodels.MainViewModel
import io.github.sthenight.sfsinstaller.utils.AndroidStringProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { ActionOptionStore() }
    single { AndroidStringProvider(androidContext()) }
    viewModelOf(::MainViewModel)
    viewModelOf(::ActionViewModel)
}
