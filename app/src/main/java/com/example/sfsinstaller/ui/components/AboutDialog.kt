package com.example.sfsinstaller.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.sfsinstaller.R

@Composable
fun AboutDialog(
    closeDialog: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            closeDialog()
        },
        title = {
            Text(stringResource(R.string.about_title))
        },
        confirmButton = {
            TextButton(onClick = { closeDialog() }) {
                Text(stringResource(R.string.ok))
            }
        },
        text = {
            Text(stringResource(R.string.about_text))
        }
    )
}