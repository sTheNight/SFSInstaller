package com.example.sfsinstaller.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfsinstaller.models.ApiFormat
import com.example.sfsinstaller.ui.states.ActionUiState
import com.example.sfsinstaller.utils.Constant
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import com.example.sfsinstaller.R
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.source
import java.io.File

class ActionViewModel(
    private val actionOptionStore: ActionOptionStore
) : ViewModel() {

    var infoText by mutableStateOf("")
        private set

    private val _actionUiState = MutableStateFlow(ActionUiState())
    val actionUiState = _actionUiState.asStateFlow()

    fun openPermissionGrantDialog() {
        _actionUiState.update { it.copy(isPermissionGrantDialogShow = true) }
    }

    fun closePermissionGrantDialog() {
        _actionUiState.update { it.copy(isPermissionGrantDialogShow = false) }
    }

    fun resetInfo() {
        infoText = ""
    }

    private fun appendInfo(context: Context, resId: Int, isWarning: Boolean = false, vararg args: Any?) {
        val text = context.getString(resId, *args)
        infoText += if (isWarning)
            "<font color='red'>$text</font><br/>"
        else
            "$text<br/>"
    }

    fun startAction(context: Context) {
        viewModelScope.launch {
            delay(1000L)
            val state = actionOptionStore.actionOptionState.first()

            appendInfo(context, R.string.action_translation_selected, false, state.isTranslationSelected)
            appendInfo(context, R.string.action_mod_patch_selected, false, state.isModPatchSelected)
            appendInfo(context, R.string.action_divider)

            val apkResult = withContext(Dispatchers.IO) {
                val tasks = mutableListOf<Deferred<Boolean>>()

                if (state.isModPatchSelected) {
                    tasks.add(async { releaseModPatchFile(context) })
                }
                if (state.isTranslationSelected) {
                    tasks.add(async { releaseTranslationFile(context) })
                }

                val apkTask = async { releaseApkFile(context) }
                tasks.add(apkTask)

                tasks.awaitAll()
                apkTask.await()
            }

            delay(1000L)
            if (apkResult) installApk(context)
        }
    }

    private suspend fun releaseTranslationFile(context: Context): Boolean {
        return try {
            appendInfo(context, R.string.releasing_translation)

            val mediaPath = context.externalMediaDirs[0]?.absolutePath?.toPath()
                ?: throw IllegalStateException("externalMediaDirs is null")

            val network = com.example.sfsinstaller.utils.Network()
            val response = network.fetchDataAsString(Constant.REMOTE_LINK_URL)
            val remote = Json.decodeFromString<ApiFormat>(response)
            val translation = remote.translation

            if (!translation.useable) {
                appendInfo(context, R.string.translation_unavailable, true)
                return false
            }

            if (remote.compatibleVersion != Constant.COMPATIBLE_VERSION) {
                appendInfo(context, R.string.translation_version_mismatch, true)
                return false
            }

            val finalPath = mediaPath.div("Custom Translations").div(translation.name)
            finalPath.parent?.let { FileSystem.SYSTEM.createDirectories(it) }

            network.fetchDataAsSource(translation.link).use { source ->
                FileSystem.SYSTEM.sink(finalPath).buffer().use { it.writeAll(source) }
            }

            appendInfo(context, R.string.translation_release_success)
            true
        } catch (e: Exception) {
            appendInfo(context, R.string.translation_release_failed, true, e.message ?: "")
            false
        }
    }

    private fun releaseModPatchFile(context: Context): Boolean {
        return try {
            appendInfo(context, R.string.releasing_mod_patch)

            val dataDir = context.dataDir?.absolutePath?.toPath()
                ?: throw IllegalStateException("dataDir is null")
            val path = dataDir.div("shared_prefs")
                .div("com.StefMorojna.SpaceflightSimulator.v2.playerprefs.xml")

            path.parent?.let { FileSystem.SYSTEM.createDirectories(it) }

            context.assets.open("mod.xml").source().use { source ->
                FileSystem.SYSTEM.sink(path).buffer().use { it.writeAll(source) }
            }

            appendInfo(context, R.string.mod_patch_release_success)
            true
        } catch (e: java.io.IOException) {
            appendInfo(context, R.string.mod_patch_open_failed, true)
            false
        } catch (e: Exception) {
            appendInfo(context, R.string.mod_patch_release_failed, true, e.message ?: "")
            false
        }
    }

    private fun releaseApkFile(context: Context): Boolean {
        return try {
            appendInfo(context, R.string.releasing_apk)

            val cachePath = context.externalCacheDir?.absolutePath?.toPath()
                ?: throw IllegalStateException("externalCacheDir is null")
            val apkPath = cachePath.div("sfs.apk")

            context.assets.open("sfs.apk").source().use { source ->
                FileSystem.SYSTEM.sink(apkPath).buffer().use { it.writeAll(source) }
            }

            appendInfo(context, R.string.apk_release_success)
            true
        } catch (e: java.io.IOException) {
            appendInfo(context, R.string.apk_open_failed, true)
            false
        } catch (e: Exception) {
            appendInfo(context, R.string.apk_release_failed, true, e.message ?: "")
            false
        }
    }

    fun installApk(context: Context) {
        try {
            if (!context.packageManager.canRequestPackageInstalls()) {
                openPermissionGrantDialog()
                appendInfo(context, R.string.apk_install_permission_required, true)
                return
            }

            val apkFile = File(context.externalCacheDir, "sfs.apk")
            if (!apkFile.exists()) {
                appendInfo(context, R.string.apk_file_not_found, true)
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            appendInfo(context, R.string.launch_installer)
        } catch (e: Exception) {
            appendInfo(context, R.string.launch_installer_failed, true, e.message ?: "")
        }
    }

    fun grantPermission(context: Context) {
        try {
            val uri = Uri.parse("package:${context.packageName}")
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            appendInfo(context, R.string.open_permission_settings_failed, true)
        }
    }
}
