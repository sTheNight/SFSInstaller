package com.example.sfsinstaller.models
import kotlinx.serialization.Serializable

@Serializable
data class ApiFormat(
    val compatibleVersion: String,
    val modPatch: FileInfo,
    val translation: FileInfo
)