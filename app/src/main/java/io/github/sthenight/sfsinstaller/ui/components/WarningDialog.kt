package io.github.sthenight.sfsinstaller.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import io.github.sthenight.sfsinstaller.BuildConfig
import io.github.sthenight.sfsinstaller.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WarningDialog(closeDialog: () -> Unit) {
    AlertDialog(
        title = {
            Text(stringResource(R.string.warning_dialog_title))
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                shapes = ButtonDefaults.shapes(),
                onClick = {
                    closeDialog()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        text = {
            HtmlText(htmlText = stringResource(R.string.warning_dialog_msg))
        }
    )
}