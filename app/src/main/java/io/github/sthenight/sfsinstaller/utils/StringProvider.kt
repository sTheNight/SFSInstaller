package io.github.sthenight.sfsinstaller.utils

import android.content.Context

class AndroidStringProvider(
    private val context: Context
) {
    fun getString(redId: Int, vararg args: Any?): String {
        return context.getString(redId, *args)
    }
}
