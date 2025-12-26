package com.example.sfsinstaller.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sfsinstaller.R
import com.example.sfsinstaller.ui.components.AboutDialog
import com.example.sfsinstaller.ui.components.ToolbarMenu
import com.example.sfsinstaller.ui.components.WarningDialog
import com.example.sfsinstaller.ui.viewmodels.ActionOptionStore
import com.example.sfsinstaller.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    actionOptionStore: ActionOptionStore,
    switchToActionScreen: () -> Unit
) {
    var isAboutDialogShow by rememberSaveable { mutableStateOf(false) }
    var isWarningDialogShow by rememberSaveable { mutableStateOf(true) }
    val state by actionOptionStore.actionOptionState.collectAsState()
    if (isAboutDialogShow)
        AboutDialog { isAboutDialogShow = false }
    if(isWarningDialogShow)
        WarningDialog { isWarningDialogShow = false }
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    actions = {
                        ToolbarMenu(
                            openAboutDialog = {
                                isAboutDialogShow = true
                            }
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            item {
                MainLayout(
                    isTranslationChecked = state.isTranslationSelected,
                    isModPatchChecked = state.isModPatchSelected,
                    onTranslationCheckedChange = { actionOptionStore.setTranslationSelected(it) },
                    onModPatchCheckedChange = { actionOptionStore.setModPatchSelected(it) },
                    switchToActionScreen = { switchToActionScreen() }
                )
            }
        }
    }
}

@Composable
fun MainLayout(
    isTranslationChecked: Boolean,
    isModPatchChecked: Boolean,
    onTranslationCheckedChange: (Boolean) -> Unit,
    switchToActionScreen: () -> Unit,
    onModPatchCheckedChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.install_option),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        )
        SelectionItem(
            title = stringResource(R.string.game_apk),
            isChecked = true,
            onCheckedChange = {},
            isEnabled = false
        )
        SelectionItem(
            title = stringResource(R.string.mod_patch),
            isChecked = isModPatchChecked,
            onCheckedChange = onModPatchCheckedChange
        )
        SelectionItem(
            title = stringResource(R.string.translation),
            isChecked = isTranslationChecked,
            onCheckedChange = onTranslationCheckedChange
        )
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            onClick = { switchToActionScreen() }
        ) {
            Text(stringResource(R.string.start_action))
        }
    }
}

@Composable
fun SelectionItem(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isEnabled: Boolean = true
) {
    Card(
        onClick = { if (isEnabled) onCheckedChange(!isChecked) },
        enabled = isEnabled,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = null,
                enabled = isEnabled
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title
            )
        }
    }
}