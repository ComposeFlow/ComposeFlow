package io.composeflow.cloud.storage

import kotlinx.serialization.Serializable

@Serializable
data class BlobIdWrapper(
    val bucket: String,
    val name: String,
    val generation: Long?,
)

expect fun Any.toKotlinWrapper(): BlobIdWrapper
