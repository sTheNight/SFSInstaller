package com.example.sfsinstaller.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.res.stringResource
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
            crackPatchChecked = state["crackPatch"] ?: true,
            translationChecked = state["translation"] ?: true,
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
                crackPatchChecked = !it.crackPatchChecked
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
                retryDialogShow = false
            )
        }
    }
    fun openRetryDialog() {
        _appState.update {
            it.copy(
                retryDialogShow = true
            )
        }
    }

    fun toggleTranslation() {
        _appState.update {
            it.copy(translationChecked = !it.translationChecked)
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
                    appendInfoText("获取数据成功", InfoLevel.LEVEL_INFO)

                    val remoteFile = Json.decodeFromString<RemoteFile>(data)
                    _appState.update { it.copy(remoteInfoData = remoteFile) }

                    val externalFileDirPath =
                        context.getExternalFilesDir(null)?.absolutePath?.toPath()
                    val dataDirPath = context.dataDir.absolutePath.toPath()

                    if (appState.value.translationChecked && remoteFile.translation != null) {
                        val translationDir = externalFileDirPath?.div("Custom Translations") ?: run {
                            throw IllegalStateException("获取 Custom Translations 目录失败")
                        }
                        val translationPath: Path =
                            translationDir / "${remoteFile.translation.name}"
                        val translationTask = async {
                            releaseFile(
                                fileInfo = remoteFile.translation,
                                displayName = context.getString(R.string.translation),
                                destPath = translationPath
                            )
                        }
                        tasks.add(translationTask)
                    }

                    if (appState.value.crackPatchChecked && remoteFile.modPatch != null) {
                        val modPatchDir = dataDirPath.div("shared_prefs")
                        val modPatchPath: Path = modPatchDir / "${remoteFile.modPatch.name}"

                        val modPatchTask = async {
                            releaseFile(
                                fileInfo = remoteFile.modPatch,
                                displayName = context.getString(R.string.mod_patch),
                                destPath = modPatchPath
                            )
                        }
                        tasks.add(modPatchTask)
                    }
                    val releaseApkTask = async {
                        releaseApkFile(context)
                    }
                    tasks.add(releaseApkTask)
                    val results = tasks.awaitAll()
                    if (results.all { it }) {
                        appendInfoText("所有任务均已完成")
                    } else {
                        appendInfoText("任务执行失败", InfoLevel.LEVEL_ERROR)
                    }
                    InstallApk(context)
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
        destPath: Path
    ): Boolean {
        return try {
            if (!fileInfo.useable) {
                throw IllegalStateException("${displayName} 不可用")
            }

            val fileURL = fileInfo.link.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException("${displayName} 链接为空")

            appendInfoText("正在下载 ${displayName}")

            val network = Network()
            val bytes = network.fetchDataAsBytes(fileURL)
            appendInfoText("${displayName} 大小: ${bytes.size} Byte")

            val parentDir = destPath?.parent
            if (parentDir != null && !FileSystem.SYSTEM.exists(parentDir)) {
                FileSystem.SYSTEM.createDirectories(parentDir)
            }

            FileSystem.SYSTEM.sink(destPath).buffer().use { sink ->
                sink.write(bytes)
            }
            appendInfoText("${displayName} 写入完成")
            true
        } catch (e: Exception) {
            appendInfoText("释放 ${displayName} 失败: ${e.message}", InfoLevel.LEVEL_ERROR)
            false
        }
    }

    suspend fun releaseApkFile(context: Context): Boolean {
        return try {
            appendInfoText("正在释放 APK 文件…")

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

            appendInfoText("APK 文件写入完成")
            true
        } catch (e: Exception) {
            appendInfoText("释放 APK 文件失败: ${e.message}", InfoLevel.LEVEL_ERROR)
            false
        }
    }

    suspend fun InstallApk(context: Context) {
        closeRetryDialog()
        if (!context.packageManager.canRequestPackageInstalls()) {
            openRetryDialog()
            appendInfoText("无法安装未知应用，请授予权限后重试", InfoLevel.LEVEL_ERROR)
            delay(1000L)
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
            appendInfoText("安装器已启动")
        }
    }
}

data class AppState(
    val infoList: List<InfoMsg> = emptyList(),
    val crackPatchChecked: Boolean = true,
    val isTaskRunning: Boolean = false,
    val translationChecked: Boolean = true,
    val retryDialogShow: Boolean = false,
    val remoteInfoData: RemoteFile? = null
)
