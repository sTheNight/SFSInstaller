package io.github.sthenight.sfsinstaller.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.sthenight.sfsinstaller.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TranslationWarningDialog(
    closeDialog: () -> Unit
) {
    AlertDialog(
        title = {
            Text(stringResource(R.string.translation_install_warning_title))
        },
        text = {
            Text(stringResource(R.string.translation_install_warning_content))
        },
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
        onDismissRequest = {}
    )
}