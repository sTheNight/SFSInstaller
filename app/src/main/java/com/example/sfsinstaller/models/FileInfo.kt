package com.example.sfsinstaller.models
import kotlinx.serialization.Serializable

@Serializable
data class FileInfo(
    val useable: Boolean,
    val link: String,
    val hash: String,
    val name: String
)