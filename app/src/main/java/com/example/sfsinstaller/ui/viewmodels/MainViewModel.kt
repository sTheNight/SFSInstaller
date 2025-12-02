package com.example.sfsinstaller.ui.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfsinstaller.models.FileInfo
import com.example.sfsinstaller.models.InfoLevel
import com.example.sfsinstaller.models.InfoMsg
import com.example.sfsinstaller.models.RemoteFile
import com.example.sfsinstaller.utils.Constant
import com.example.sfsinstaller.utils.Network
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.source
import java.security.MessageDigest

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
            it.copy(crackPatchChecked = !it.crackPatchChecked)
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
                val networkService = Network()
                val data = networkService.fetchDataAsString(Constant.REMOTE_LINK_URL)

                if (data.isNotEmpty()) {
                    appendInfoText("获取数据成功", InfoLevel.LEVEL_INFO)

                    val remoteFile = Json.decodeFromString<RemoteFile>(data)
                    _appState.update { it.copy(remoteInfoData = remoteFile) }

                    val externalFileDirPath =
                        context.getExternalFilesDir(null)?.absolutePath?.toPath()
                    val dataDirPath = context.dataDir.absolutePath.toPath()

                    if (appState.value.translationChecked && remoteFile.translation != null) {
                        val translationDir = externalFileDirPath?.div("Custom Translations")
                            ?: run {
                                appendInfoText(
                                    "获取 Custom Translations 目录失败",
                                    InfoLevel.LEVEL_ERROR
                                )
                                return@launch
                            }
                        val translationPath: Path =
                            translationDir / "${remoteFile.translation.name}"

                        releaseFile(
                            fileInfo = remoteFile.translation,
                            displayName = "汉化包",
                            destPath = translationPath
                        )
                    }

                    if (appState.value.crackPatchChecked && remoteFile.modPatch != null) {
                        val modPatchDir = dataDirPath.div("shared_prefs")
                        val modPatchPath: Path = modPatchDir / "${remoteFile.modPatch.name}"

                        releaseFile(
                            fileInfo = remoteFile.modPatch,
                            displayName = "破解补丁",
                            destPath = modPatchPath
                        )
                    }
                }
            } catch (e: Exception) {
                appendInfoText("获取远程信息失败: ${e.message}", InfoLevel.LEVEL_ERROR)
            }
        }
    }
    fun releaseFile(
        fileInfo: FileInfo,
        displayName: String,
        destPath: Path
    ) {
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                appendInfoText("释放 ${displayName} 失败: ${e.message}", InfoLevel.LEVEL_ERROR)
            }
        }
    }
    fun releaseApkFile(context: Context) {
        viewModelScope.launch {
            try {
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

                appendInfoText("APK 文件写入完成: $destPath")
            } catch (e: Exception) {
                appendInfoText("释放 APK 文件失败: ${e.message}", InfoLevel.LEVEL_ERROR)
            }
        }
    }

}

data class AppState(
    val infoList: List<InfoMsg> = emptyList(),
    val crackPatchChecked: Boolean = true,
    val translationChecked: Boolean = true,
    val remoteInfoData: RemoteFile? = null
)
