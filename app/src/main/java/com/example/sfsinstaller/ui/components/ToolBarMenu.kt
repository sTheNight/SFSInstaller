package com.example.sfsinstaller.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.sfsinstaller.R
import com.example.sfsinstaller.utils.openUri

@Composable
fun ToolbarMenu(
    openAboutDialog : () -> Unit
) {
    val context = LocalContext.current
    Row() {
        IconButton(onClick = {
            openUri(context,"https://github.com/sTheNight/SFSInstaller")
        }) {
            Icon(
                painter = painterResource(R.drawable.github),
                contentDescription = null
            )
        }
        IconButton(onClick = {
            openAboutDialog()
        }) {
            Icon(
                painter = painterResource(R.drawable.info_24px),
                contentDescription = null
            )
        }
    }
}