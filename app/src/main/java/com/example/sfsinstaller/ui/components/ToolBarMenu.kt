package com.example.sfsinstaller.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.sfsinstaller.utils.Constant
import com.example.sfsinstaller.R
import com.example.sfsinstaller.utils.openUri

@Composable
fun ToolbarMenu(
    openAboutDialog : () -> Unit
) {
    var mainMenuExpanded by remember { mutableStateOf(false) }
    var communicationMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Box() {
        IconButton(onClick = { mainMenuExpanded = !mainMenuExpanded }) {
            Icon(
                painter = painterResource(R.drawable.more_vert_24px),
                contentDescription = null
            )
        }
        DropdownMenu(
            expanded = mainMenuExpanded,
            onDismissRequest = { mainMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_communication)) },
                onClick = {
                    mainMenuExpanded = false
                    communicationMenuExpanded = true
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.arrow_right_24px),
                        contentDescription = null
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_about)) },
                onClick = {
                    mainMenuExpanded = false
                    openAboutDialog()
                }
            )
        }
        DropdownMenu(
            expanded = communicationMenuExpanded,
            onDismissRequest = {
                communicationMenuExpanded = false
            }
        ) {
            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.communication_join_qq_group))
                },
                onClick = {
                    communicationMenuExpanded = false
                    openUri(context = context, uri = Constant.QQ_GROUP_URL)
                }
            )
            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.communication_join_qq_channel))
                },
                onClick = {
                    communicationMenuExpanded = false
                    openUri(context = context, uri = Constant.QQ_CHANNEL_URL)
                }
            )
        }
    }
}