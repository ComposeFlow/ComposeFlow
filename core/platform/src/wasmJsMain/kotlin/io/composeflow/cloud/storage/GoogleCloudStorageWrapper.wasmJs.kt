package io.composeflow.cloud.storage

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.Err
import io.github.vinceglb.filekit.core.PlatformFile

actual class GoogleCloudStorageWrapper {
    actual suspend fun uploadAsset(
        userId: String,
        projectId: String,
        file: PlatformFile,
        publicRead: Boolean,
    ): Result<BlobInfoWrapper?, Throwable> =
        Err(UnsupportedOperationException("Google Cloud Storage not available on WASM"))

    actual suspend fun uploadFile(
        userId: String,
        projectId: String,
        fileName: String,
        content: String,
        publicRead: Boolean,
    ): Result<BlobInfoWrapper?, Throwable> =
        Err(UnsupportedOperationException("Google Cloud Storage not available on WASM"))

    actual suspend fun listFile(userId: String): Result<List<BlobInfoWrapper>, Throwable> =
        Err(UnsupportedOperationException("Google Cloud Storage not available on WASM"))

    actual suspend fun downloadAsset(blobInfoWrapper: BlobInfoWrapper): Result<BlobInfoWrapper?, Throwable> =
        Err(UnsupportedOperationException("Google Cloud Storage not available on WASM"))

    actual suspend fun getFile(fullPath: String): Result<BlobInfoWrapper, Throwable> =
        Err(UnsupportedOperationException("Google Cloud Storage not available on WASM"))

    actual suspend fun deleteFile(fullPath: String): Result<Unit, Throwable> =
        Err(UnsupportedOperationException("Google Cloud Storage not available on WASM"))

    actual suspend fun deleteFolder(folderName: String): Result<Unit, Throwable> =
        Err(UnsupportedOperationException("Google Cloud Storage not available on WASM"))
}