package io.composeflow.cloud.storage

import com.google.cloud.storage.BlobId

actual fun Any.toKotlinWrapper(): BlobIdWrapper {
    require(this is BlobId) { "Expected BlobId but got ${this::class.simpleName}" }
    return BlobIdWrapper(
        bucket = bucket,
        name = name,
        generation = generation,
    )
}