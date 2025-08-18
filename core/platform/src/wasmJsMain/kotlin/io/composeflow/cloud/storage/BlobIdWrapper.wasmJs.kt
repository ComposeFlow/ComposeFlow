package io.composeflow.cloud.storage

actual fun Any.toKotlinWrapper(): BlobIdWrapper {
    throw UnsupportedOperationException("BlobId operations not available on WASM")
}