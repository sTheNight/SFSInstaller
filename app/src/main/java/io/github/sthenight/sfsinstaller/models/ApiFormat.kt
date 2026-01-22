package io.github.sthenight.sfsinstaller.models
import kotlinx.serialization.Serializable

@Serializable
data class ApiFormat(
    val compatibleVersion: String,
    val translation: FileInfo
)