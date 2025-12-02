package com.example.sfsinstaller.models
import kotlinx.serialization.Serializable

@Serializable
data class RemoteFile(
    val compatibleVersion: String,
    val modPatch: FileInfo,
    val translation: FileInfo
)