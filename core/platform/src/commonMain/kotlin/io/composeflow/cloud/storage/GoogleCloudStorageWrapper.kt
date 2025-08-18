package io.composeflow.cloud.storage

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import io.composeflow.BuildConfig
import io.github.vinceglb.filekit.core.PlatformFile
expect class GoogleCloudStorageWrapper() {
    suspend fun uploadAsset(
        userId: String,
        projectId: String,
        file: PlatformFile,
        publicRead: Boolean = false,
    ): Result<BlobInfoWrapper?, Throwable>

    suspend fun uploadFile(
        userId: String,
        projectId: String,
        fileName: String,
        content: String,
        publicRead: Boolean = false,
    ): Result<BlobInfoWrapper?, Throwable>

    suspend fun listFile(userId: String): Result<List<BlobInfoWrapper>, Throwable>

    suspend fun downloadAsset(blobInfoWrapper: BlobInfoWrapper): Result<BlobInfoWrapper?, Throwable>

    suspend fun getFile(fullPath: String): Result<BlobInfoWrapper, Throwable>

    suspend fun deleteFile(fullPath: String): Result<Unit, Throwable>

    suspend fun deleteFolder(folderName: String): Result<Unit, Throwable>
}

/**
 * Append "/" at the end of the name so that the String is able to represents a folder name in
 * Google cloud storage.
 * Google Cloud Storage doesn't have a concept of folder hierarchy, instead it has flat namespace.
 * Thus, adding "/" at the end implies that it represents a folder name.
 */
internal fun String.asFolderName() = if (!this.endsWith("/")) "$this/" else this
