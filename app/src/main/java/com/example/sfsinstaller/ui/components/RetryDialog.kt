package com.example.sfsinstaller.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.sfsinstaller.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetryDialog(
    closeDialog: () -> Unit,
    retryInstall: () -> Unit,
    grantPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(stringResource(R.string.no_permission_dialog_title))
        },
        confirmButton = {
            TextButton(onClick = { retryInstall() }) {
                Text(stringResource(R.string.retry))
            }
        },
        dismissButton = {
            TextButton(onClick = { closeDialog() }) {
                Text(stringResource(R.string.cancel))
            }
        },
        text = {
            Column() {
                HtmlText(htmlText = stringResource(R.string.retry_msg))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = { grantPermission() }),
                    text = stringResource(R.string.grant),
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold
                )
            }
        },
    )
}