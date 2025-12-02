package com.example.sfsinstaller.models

data class InfoMsg(
    val level: InfoLevel,
    val info: String
)
enum class InfoLevel() {
    LEVEL_ERROR,
    LEVEL_WARNING,
    LEVEL_INFO
}