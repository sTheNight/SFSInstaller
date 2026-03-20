package io.github.sthenight.sfsinstaller.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sthenight.sfsinstaller.R
import io.github.sthenight.sfsinstaller.models.ApiFormat
import io.github.sthenight.sfsinstaller.ui.states.ActionUiState
import io.github.sthenight.sfsinstaller.stores.ActionOptionStore
import io.github.sthenight.sfsinstaller.Constant
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.sthenight.sfsinstaller.BuildConfig
import io.github.sthenight.sfsinstaller.models.TaskType
import io.github.sthenight.sfsinstaller.models.TranslationSelection
import io.github.sthenight.sfsinstaller.utils.AndroidStringProvider
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.source
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ActionViewModel @Inject constructor(
    private val actionOptionStore: ActionOptionStore,
    private val stringProvider: AndroidStringProvider
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

    private fun appendInfo(
        resId: Int,
        isWarning: Boolean = false,
        vararg args: Any?
    ) {
        // 粗糙的实现
        val text = stringProvider.getString(resId, *args)
        infoText += if (isWarning)
            "<font color='red'>$text</font><br/>"
        else
            "$text<br/>"
    }

    private fun appendInfo(
        text: String,
        isWarning: Boolean = false
    ) {
        infoText += if (isWarning)
            "<font color='red'>$text</font><br/>"
        else
            "$text<br/>"
    }

    fun startAction(context: Context) {
        viewModelScope.launch {
            delay(1000L)

            val state = actionOptionStore.actionOptionState.value

            appendInfo(R.string.action_mod_patch_selected, false, state.isModPatchSelected)
            appendInfo(R.string.action_translation_selected, false, state.isTranslationSelected)
            appendInfo(R.string.action_divider)

            val results = withContext(Dispatchers.IO) {
                coroutineScope {
                    val tasks = mutableMapOf<TaskType, Deferred<Boolean>>()

                    if (state.isModPatchSelected) {
                        tasks[TaskType.ModpatchTask] = async {
                            releaseModPatchFile(context)
                        }
                    }

                    if (state.isTranslationSelected) {
                        tasks[TaskType.TranslationTask] = async {
                            releaseTranslationFile(context)
                        }
                    }

                    tasks[TaskType.ApkfileTask] = async {
                        releaseApkFile(context)
                    }

                    tasks.mapValues { (_, deferred) -> deferred.await() }
                }
            }

            delay(1000L)

            if (results[TaskType.ApkfileTask] == true) {
                installApk(context)
            }
        }
    }

    private suspend fun releaseTranslationFile(context: Context): Boolean {
        return try {
            appendInfo(R.string.releasing_translation)
            // 变量初始化
            val mediaPath = context.externalMediaDirs.firstOrNull()?.absolutePath?.toPath()
                ?: run {
                    appendInfo("externalMediaDirs is null", true)
                    return false
                }
            val network = io.github.sthenight.sfsinstaller.utils.NetworkProvider()
            val response = network.fetchDataAsString(Constant.REMOTE_LINK_URL)
            val remote = Json.decodeFromString<ApiFormat>(response)
            val translation = remote.translation
            // 可用性判断
            if (!translation.useable)
                throw IllegalStateException(stringProvider.getString(R.string.translation_unavailable))
            // 版本判断
            if (remote.compatibleVersion != BuildConfig.GAME_VERSION) {
                appendInfo(
                    R.string.translation_version_mismatch, true,
                    remote.compatibleVersion,
                    BuildConfig.GAME_VERSION
                )
                delay(1000L)
                throw Exception(stringProvider.getString(R.string.incompatible_version))
            }
            // 释放汉化包本体
            val finalPath = mediaPath
                .div("Custom Translations/${translation.name}")
            finalPath.parent?.let {
                FileSystem.SYSTEM.createDirectories(it)
            }
            network.fetchDataAsSource(translation.link).use { source ->
                FileSystem.SYSTEM.sink(finalPath).buffer().use { it.writeAll(source) }
            }
            // 释放汉化包选择文件
            releaseTranslationSelectionFile(
                context,
                translation.name.substringBeforeLast('.'),
                true
            )
            appendInfo(R.string.translation_release_success)
            true
        } catch (e: Exception) {
            appendInfo(
                R.string.translation_release_failed,
                true,
                e.message ?: "Unknown"
            )
            releaseTranslationSelectionFile(context, "Chinese", false)
            false
        }
    }

    private fun releaseTranslationSelectionFile(
        context: Context,
        codeName: String,
        custom: Boolean
    ) {
        try {
            val mediaPath = context.externalMediaDirs.firstOrNull()?.absolutePath?.toPath()
                ?: throw IllegalStateException("externalMediaDirs is null")

            val selectionFilePath = mediaPath.div("Saving/Settings/LanguageSettings_2.txt")
            selectionFilePath.parent?.let { FileSystem.SYSTEM.createDirectories(it) }

            val json = Json { prettyPrint = true }
            val selectionObj = TranslationSelection(codeName, custom)
            val content = json.encodeToString(selectionObj)

            FileSystem.SYSTEM.sink(selectionFilePath).buffer().use { sink ->
                sink.writeUtf8(content)
            }
        } catch (e: Exception) {
            appendInfo(
                "Translation selection release failed: ${e.message ?: "Unknown"}",
                true,
            )
        }
    }

    private fun releaseModPatchFile(context: Context): Boolean {
        return try {
            appendInfo(R.string.releasing_mod_patch)

            val dataDir = context.dataDir?.absolutePath?.toPath()
                ?: throw IllegalStateException("dataDir is null")
            val path = dataDir
                .div("shared_prefs")
                .div("com.StefMorojna.SpaceflightSimulator.v2.playerprefs.xml")

            path.parent?.let { FileSystem.SYSTEM.createDirectories(it) }

            context.assets.open("mod.xml").source().use { source ->
                FileSystem.SYSTEM.sink(path).buffer().use { it.writeAll(source) }
            }

            appendInfo(R.string.mod_patch_release_success)
            true
        } catch (e: java.io.IOException) {
            appendInfo(
                R.string.mod_patch_open_failed,
                true,
                e.message ?: "Unknown"
            )
            false
        } catch (e: Exception) {
            appendInfo(R.string.mod_patch_release_failed, true, e.message ?: "")
            false
        }
    }

    private fun releaseApkFile(context: Context): Boolean {
        return try {
            appendInfo(R.string.releasing_apk)

            val cachePath = context.externalCacheDir?.absolutePath?.toPath()
                ?: throw IllegalStateException("externalCacheDir is null")
            val apkPath = cachePath.div("sfs.apk")

            context.assets.open("sfs.apk").source().use { source ->
                FileSystem.SYSTEM.sink(apkPath).buffer().use {
                    it.writeAll(source)
                }
            }

            appendInfo(R.string.apk_release_success)
            true
        } catch (e: java.io.IOException) {
            appendInfo(

                R.string.apk_open_failed,
                true,
                e.message ?: "Unknown"
            )
            false
        } catch (e: Exception) {
            appendInfo(

                R.string.apk_release_failed,
                true,
                e.message ?: "Unknown"
            )
            false
        }
    }

    fun installApk(context: Context) {
        try {
            if (!context.packageManager.canRequestPackageInstalls()) {
                openPermissionGrantDialog()
                appendInfo(R.string.apk_install_permission_required, true)
                return
            }

            val apkFile = File(context.externalCacheDir, "sfs.apk")
            if (!apkFile.exists()) {
                appendInfo(R.string.apk_file_not_found, true)
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
            appendInfo(R.string.launch_installer)
        } catch (e: Exception) {
            appendInfo(R.string.launch_installer_failed, true, e.message ?: "Unknown")
        }
    }

    fun grantPermission(context: Context) {
        try {
            val uri = "package:${context.packageName}".toUri()
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            appendInfo(
                R.string.open_permission_settings_failed,
                true,
                e.message ?: "Unknown"
            )
        }
    }
}
