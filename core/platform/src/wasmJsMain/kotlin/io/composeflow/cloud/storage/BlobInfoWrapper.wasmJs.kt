package io.composeflow.cloud.storage

actual fun Any.toBlobInfoWrapper(contentBytes: ByteArray?): BlobInfoWrapper =
    throw UnsupportedOperationException("BlobInfo operations not available on WASM")
