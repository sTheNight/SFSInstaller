package io.github.sthenight.sfsinstaller.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sthenight.sfsinstaller.R
import io.github.sthenight.sfsinstaller.ui.components.HtmlText
import io.github.sthenight.sfsinstaller.ui.components.RetryDialog
import io.github.sthenight.sfsinstaller.ui.viewmodels.ActionViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActionScreen(
    actionViewModel: ActionViewModel,
    back: () -> Unit,
) {
    val state by actionViewModel.actionUiState.collectAsState()
    val currentContext = LocalContext.current
    LaunchedEffect(null) {
        actionViewModel.resetInfo()
        actionViewModel.startAction(currentContext)
    }
    if (state.isPermissionGrantDialogShow)
        RetryDialog(
            closeDialog = { actionViewModel.closePermissionGrantDialog() },
            retryInstall = { actionViewModel.installApk(currentContext) },
            grantPermission = { actionViewModel.grantPermission(currentContext) }
        )
    Scaffold(
        topBar = {
            Column() {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.start_action))
                    },
                    navigationIcon = {
                        IconButton(
                            shapes = IconButtonDefaults.shapes(),
                            onClick = {
                                back()
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.close_24px),
                                contentDescription = null
                            )
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                SelectionContainer() {
                    HtmlText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        htmlText = actionViewModel.infoText,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}