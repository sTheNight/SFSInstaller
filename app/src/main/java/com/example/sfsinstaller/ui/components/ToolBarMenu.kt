package com.example.sfsinstaller.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sfsinstaller.BuildConfig
import com.example.sfsinstaller.R
import com.example.sfsinstaller.utils.openUri

@Composable
fun ToolbarMenu(
    openAboutDialog : () -> Unit
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (BuildConfig.IS_DEBUG) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Debug",
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

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