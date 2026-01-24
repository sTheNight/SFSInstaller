package io.github.sthenight.sfsinstaller.models

import kotlinx.serialization.Serializable

@Serializable
data class TranslationSelection(
    val codeName: String,
    val custom: Boolean
)