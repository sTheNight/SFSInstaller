package com.example.sfsinstaller.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.sfsinstaller.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetryDialog(
    closeDialog: () -> Unit,
    retryInstall: () -> Unit,
    grandPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {  },
        title = {
            Text(stringResource(R.string.no_permission_dialog_title))
        },
        confirmButton = {
            TextButton(onClick = { grandPermission() }) {
                Text(stringResource(R.string.grant))
            }
        },
        dismissButton = {
            TextButton(onClick = {retryInstall()}) {
                Text(stringResource(R.string.retry))
            }
            TextButton(onClick = { closeDialog() }) {
                Text(stringResource(R.string.cancle))
            }
        },
        text = {
            Text(stringResource(R.string.retry_msg))
        },
    )
}