package com.example.sfsinstaller.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
fun openUri(
    context: Context,
    uri: String,
    recall: (() -> Unit)? = null
) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    } catch (e: Exception) {
        // something problem here
        // i will fixed it later
        recall?.invoke()
    }
}