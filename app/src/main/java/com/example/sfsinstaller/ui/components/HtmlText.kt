package com.example.sfsinstaller.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun HtmlText(
    modifier: Modifier = Modifier,
    htmlText: String,
    fontSize: TextUnit = 14.sp
) {
    val aboutInfoText = AnnotatedString.fromHtml(
        htmlString = htmlText,
        linkStyles = TextLinkStyles(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            ),
            pressedStyle = SpanStyle(
                background = MaterialTheme.colorScheme.primaryContainer
            )
        )
    )
    Text(
        modifier = modifier,
        text = aboutInfoText,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodySmall,
        fontSize = fontSize,
    )
}