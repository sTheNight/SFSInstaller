package com.example.sfsinstaller.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.sfsinstaller.R

@Composable
fun WarningDialog(closeDialog: () -> Unit) {
    AlertDialog(
        title = {
            Text(stringResource(R.string.warning_dialog_title))
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = {
                closeDialog()
            }) { Text(stringResource(R.string.ok)) }
        },
        text = {
            HtmlText(htmlText = stringResource(R.string.warning_dialog_msg))
        }
    )
}