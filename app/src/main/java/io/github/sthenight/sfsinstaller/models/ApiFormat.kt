package io.github.sthenight.sfsinstaller.models
import kotlinx.serialization.Serializable

@Serializable
data class ApiFormat(
    val compatibleVersion: String,
    val modPatch: io.github.sthenight.sfsinstaller.models.FileInfo,
    val translation: io.github.sthenight.sfsinstaller.models.FileInfo
)