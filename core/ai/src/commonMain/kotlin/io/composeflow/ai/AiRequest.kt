package io.composeflow.ai

import kotlinx.serialization.Serializable

@Serializable
data class TranslateStringResource(
    val key: String,
    val defaultValue: String,
    val description: String? = null,
)

@Serializable
data class TranslateStringsRequest(
    val stringResources: List<TranslateStringResource>,
    val defaultLocale: String,
    val targetLocales: List<String>,
)
