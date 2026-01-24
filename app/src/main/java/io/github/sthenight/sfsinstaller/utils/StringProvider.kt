package io.github.sthenight.sfsinstaller.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidStringProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getString(redId: Int, vararg args: Any?): String {
        return context.getString(redId, *args)
    }
}