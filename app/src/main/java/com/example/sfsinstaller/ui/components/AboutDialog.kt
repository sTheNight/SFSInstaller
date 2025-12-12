package com.example.sfsinstaller.ui.components
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sfsinstaller.BuildConfig
import com.example.sfsinstaller.R
import androidx.core.graphics.createBitmap

@Composable
fun AboutDialog(closeDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = closeDialog,
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    ElevatedCard(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Image(
                            painter = getAppIconPainter(R.mipmap.ic_launcher),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                        )
                    }
                    Spacer(Modifier.width(24.dp))

                    Column {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp
                        )
                        SelectionContainer {
                            Text(
                                text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HtmlText(htmlText = stringResource(R.string.info_text_html))
                    }
                }
            }
        }
    )
}
@Composable
fun getAppIconPainter(resourceId: Int): Painter {
    val context = LocalContext.current
    val drawable = ContextCompat.getDrawable(context, resourceId)
    return if (drawable is BitmapDrawable) {
        BitmapPainter(drawable.bitmap.asImageBitmap())
    } else {
        if (drawable != null) {
            val bitmap = createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1)
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            BitmapPainter(bitmap.asImageBitmap())
        } else {
            painterResource(R.drawable.ic_launcher_background)
        }
    }
}