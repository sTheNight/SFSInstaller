package com.example.sfsinstaller.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfsinstaller.R
import com.example.sfsinstaller.models.FileInfo
import com.example.sfsinstaller.models.InfoLevel
import com.example.sfsinstaller.models.InfoMsg
import com.example.sfsinstaller.models.RemoteFile
import com.example.sfsinstaller.utils.Constant
import com.example.sfsinstaller.utils.Network
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.HashingSink
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.source
import java.io.File

class MainViewModel(
    state: SavedStateHandle
) : ViewModel() {
    private val _appState = MutableStateFlow(
        AppState(
            isCrackPatchChecked = state["isCrackPatchChecked"] ?: true,
            isTranslationChecked = state["isTranslationChecked"] ?: true,
            isRetryDialogShow = state["isRetryDialogShow"] ?: false,
            infoList = state["infoList"] ?: emptyList(),
            remoteInfoData = null
        )
    )
    var appState: StateFlow<AppState> = _appState.asStateFlow()
    fun appendInfoText(info: String, level: InfoLevel = InfoLevel.LEVEL_INFO) {
        _appState.update {
            it.copy(
                infoList = it.infoList + InfoMsg(info = info, level = level)
            )
        }
    }

    fun clearInfoText() {
        _appState.update {
            it.copy(
                infoList = emptyList()
            )
        }
    }

    fun toggleCrackPatch() {
        _appState.update {
            it.copy(
                isCrackPatchChecked = !it.isCrackPatchChecked
            )
        }
    }

    private fun setTaskRunningTrue() {
        _appState.update { it.copy(isTaskRunning = true) }
    }

    private fun setTaskRunningFalse() {
        _appState.update { it.copy(isTaskRunning = false) }
    }

    fun closeRetryDialog() {
        _appState.update {
            it.copy(
                isRetryDialogShow = false
            )
        }
    }

    fun openRetryDialog() {
        _appState.update {
            it.copy(
                isRetryDialogShow = true
            )
        }
    }

    fun toggleTranslation() {
        _appState.update {
            it.copy(isTranslationChecked = !it.isTranslationChecked)
        }
    }

    fun fetchInfomation(context: Context) {
        viewModelScope.launch {
            try {
                setTaskRunningTrue()
                val networkService = Network()
                val data = networkService.fetchDataAsString(Constant.REMOTE_LINK_URL)
                val tasks = mutableListOf<Deferred<Boolean>>()

                if (data.isNotEmpty()) {
                    appendInfoText(context.getString(R.string.success_get_data))

                    val remoteFile = Json.decodeFromString<RemoteFile>(data)
                    _appState.update { it.copy(remoteInfoData = remoteFile) }

                    val externalFileDirPath =
                        context.getExternalFilesDir(null)?.absolutePath?.toPath()
                    val dataDirPath = context.dataDir.absolutePath.toPath()

                    if (remoteFile.compatibleVersion != Constant.COMPATIBLE_VERSION)
                        appendInfoText(context.getString(R.string.version_not_compatible))

                    if (appState.value.isTranslationChecked) {
                        val translationDir =
                            externalFileDirPath?.div("Custom Translations") ?: run {
                                throw IllegalStateException(context.getString(R.string.get_cutsom_translation_fold_failed))
                            }
                        val translationPath: Path =
                            translationDir / remoteFile.translation.name
                        val translationTask = async {
                            releaseFile(
                                fileInfo = remoteFile.translation,
                                displayName = context.getString(R.string.translation),
                                destPath = translationPath,
                                context = context
                            )
                        }
                        val releaseTranslationSelectionFile = async {
                            true
                        }
                        tasks.add(translationTask)
                        tasks.add(releaseTranslationSelectionFile)
                    }

                    if (appState.value.isCrackPatchChecked) {
                        val modPatchDir = dataDirPath.div("shared_prefs")
                        val modPatchPath: Path = modPatchDir / remoteFile.modPatch.name

                        val modPatchTask = async {
                            releaseFile(
                                fileInfo = remoteFile.modPatch,
                                displayName = context.getString(R.string.mod_patch),
                                destPath = modPatchPath,
                                context = context
                            )
                        }
                        tasks.add(modPatchTask)
                    }

                    if (!appState.value.isCrackPatchChecked && !appState.value.isTranslationChecked) {
                        appendInfoText(
                            context.getString(R.string.no_installation_options_checked),
                            level = InfoLevel.LEVEL_WARNING
                        )
                        delay(1000L)
                    }

                    val releaseApkTask = async {
                        releaseApkFile(context)
                    }

                    tasks.add(releaseApkTask)
                    val results = tasks.awaitAll()
                    if (results.all { it }) {
                        appendInfoText(context.getString(R.string.all_task_completed))
                    } else {
                        appendInfoText(
                            context.getString(R.string.task_failed),
                            InfoLevel.LEVEL_ERROR
                        )
                    }
                    installApk(context)
                    setTaskRunningFalse()
                }
            } catch (e: Exception) {
                appendInfoText("${e.message}", InfoLevel.LEVEL_ERROR)
                setTaskRunningFalse()
            }
        }
    }

    suspend fun releaseFile(
        fileInfo: FileInfo,
        displayName: String,
        destPath: Path,
        context: Context
    ): Boolean {
        try {
            if (!fileInfo.useable) {
                throw IllegalStateException(context.getString(R.string.file_unusable, displayName))
            }

            val fileURL = fileInfo.link.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException(
                    context.getString(
                        R.string.file_link_null,
                        displayName
                    )
                )

            appendInfoText(context.getString(R.string.file_releasing, displayName))

            val network = Network()
            val bytes = network.fetchDataAsBytes(fileURL)

            val parentDir = destPath.parent
            if (parentDir != null && !FileSystem.SYSTEM.exists(parentDir)) {
                FileSystem.SYSTEM.createDirectories(parentDir)
            }

            val fileSink = FileSystem.SYSTEM.sink(destPath).buffer()

            val hashingSink = HashingSink.sha256(fileSink)

            hashingSink.buffer().use { sink ->
                sink.write(bytes)
            }

            if (fileInfo.hash.isNotEmpty()) {
                val calculatedHash = hashingSink.hash.hex()
                val expectedHash = fileInfo.hash

                if (!calculatedHash.equals(expectedHash, ignoreCase = true)) {
                    appendInfoText(
                        context.getString(
                            R.string.file_hash_mismatch,
                            displayName,
                            calculatedHash
                        ), InfoLevel.LEVEL_WARNING
                    )
                }
            } else {
                appendInfoText(context.getString(R.string.file_hash_missing, displayName))
            }

            appendInfoText(context.getString(R.string.file_released, displayName))
            return true

        } catch (e: Exception) {
            appendInfoText(
                context.getString(
                    R.string.file_release_failed,
                    displayName,
                    e.message.toString()
                ), InfoLevel.LEVEL_ERROR
            )
            return false
        }
    }

    fun releaseApkFile(context: Context): Boolean {
        return try {
            appendInfoText(
                context.getString(
                    R.string.file_releasing,
                    context.getString(R.string.file_apk)
                )
            )

            val assetManager = context.assets
            val inputStream = assetManager.open("sfs.apk")

            val cacheDirPath = context.externalCacheDir?.absolutePath ?: run {
                val fallbackDir = context.filesDir.resolve("apk_cache")
                val fallbackPath = fallbackDir.absolutePath.toPath()
                if (!FileSystem.SYSTEM.exists(fallbackPath)) {
                    FileSystem.SYSTEM.createDirectories(fallbackPath)
                }
                fallbackDir.absolutePath
            }
            val destPath: Path = (cacheDirPath.toPath() / "sfs.apk")
            val parentDir = destPath.parent
            if (parentDir != null && !FileSystem.SYSTEM.exists(parentDir)) {
                FileSystem.SYSTEM.createDirectories(parentDir)
            }

            inputStream.source().use { source ->
                FileSystem.SYSTEM.sink(destPath).buffer().use { sink ->
                    sink.writeAll(source)
                }
            }

            appendInfoText(
                context.getString(
                    R.string.file_released,
                    context.getString(R.string.file_apk)
                )
            )
            true
        } catch (e: Exception) {
            appendInfoText(
                context.getString(
                    R.string.file_release_failed,
                    context.getString(R.string.file_apk),
                    e.message.toString()
                ), InfoLevel.LEVEL_ERROR
            )
            false
        }
    }

    suspend fun installApk(context: Context) {
        closeRetryDialog()
        if (!context.packageManager.canRequestPackageInstalls()) {
            openRetryDialog()
            appendInfoText(
                context.getString(R.string.failed_to_install_no_permission),
                InfoLevel.LEVEL_WARNING
            )
            delay(500L)
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            val cacheDirPath = context.externalCacheDir?.absolutePath ?: run {
                val fallbackDir = context.filesDir.resolve("apk_cache")
                val fallbackPath = fallbackDir.absolutePath.toPath()
                if (!FileSystem.SYSTEM.exists(fallbackPath)) {
                    FileSystem.SYSTEM.createDirectories(fallbackPath)
                }
                fallbackDir.absolutePath
            }
            val apkFile = File((cacheDirPath.toPath() / "sfs.apk").toString())
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
            appendInfoText(context.getString(R.string.try_to_install))
        }
    }
}

data class AppState(
    val infoList: List<InfoMsg> = emptyList(),
    val isCrackPatchChecked: Boolean = true,
    val isTaskRunning: Boolean = false,
    val isTranslationChecked: Boolean = true,
    val isRetryDialogShow: Boolean = false,
    val remoteInfoData: RemoteFile? = null
)
