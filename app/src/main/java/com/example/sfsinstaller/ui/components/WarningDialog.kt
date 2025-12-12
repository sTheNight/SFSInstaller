package com.example.sfsinstaller.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.sfsinstaller.BuildConfig
import com.example.sfsinstaller.R
import kotlinx.coroutines.delay

@Composable
fun WarningDialog(closeDialog: () -> Unit) {
    var waitSecond by remember { mutableStateOf(5) }
    var buttonText =
        if (waitSecond <= 0) stringResource(R.string.ok)
        else "${stringResource(R.string.ok)}(${waitSecond})"

    LaunchedEffect(Unit) {
        while (waitSecond > 0) {
            delay(1000)
            waitSecond--
        }
    }

    AlertDialog(
        title = {
            Text(stringResource(R.string.warning_dialog_title))
        },
        onDismissRequest = {
            if (BuildConfig.IS_DEBUG)
                closeDialog()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    closeDialog()
                },
                enabled = waitSecond <= 0
            ) {
                Text(buttonText)
            }
        },
        text = {
            HtmlText(htmlText = stringResource(R.string.warning_dialog_msg))
        }
    )
}