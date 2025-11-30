package com.example.sfsinstaller.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sfsinstaller.R
import com.example.sfsinstaller.components.AboutDialog
import com.example.sfsinstaller.components.ToolbarMenu
import com.example.sfsinstaller.viewmodels.MainViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel) {
    val appState = mainViewModel.appState.collectAsState().value
    val scrollState = rememberScrollState()
    var aboutDialogShow by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Text(context.getString(R.string.app_name))
                    },
                    actions = {
                        ToolbarMenu(
                            openAboutDialog = {
                                aboutDialogShow = true
                            }
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            if (aboutDialogShow)
                AboutDialog(closeDialog = { aboutDialogShow = false })
            TestCard(
                modifier = Modifier.padding(top = 16.dp),
                onButtonClick = { mainViewModel.appendInfoText() },
                infoText = appState.InfoText
            )
        }
    }
}

@Composable
fun TestCard(
    modifier: Modifier,
    onButtonClick: () -> Unit,
    infoText: String
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column() {
            Button(
                content = {
                    Text("test")
                },
                onClick = { onButtonClick() }
            )
            Text(text = infoText, modifier = Modifier.animateContentSize())
        }
    }
}