package io.composeflow.cloud.storage

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import io.composeflow.BuildConfig
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Paths

class GoogleCloudStorageWrapper {
    private val bucketName = BuildConfig.GOOGLE_CLOUD_STORAGE_BUCKET_NAME

    suspend fun uploadAsset(
        userId: String,
        projectId: String,
        file: PlatformFile,
        publicRead: Boolean = false,
    ): Result<BlobInfoWrapper?, Throwable> =
        runCatching {
            file.path?.let { filePath ->
                val blobInfo =
                    uploadFileWithPath(
                        objectName = "$userId/$projectId/asset/${file.name}",
                        filePath = filePath,
                        publicRead = publicRead,
                    )
                blobInfo
            }
        }

    suspend fun uploadFile(
        userId: String,
        projectId: String,
        fileName: String,
        content: String,
        publicRead: Boolean = false,
    ): Result<BlobInfoWrapper?, Throwable> =
        runCatching {
            val blobInfo =
                uploadFileWithContent(
                    objectName = "$userId/$projectId/$fileName",
                    content = content,
                    publicRead = publicRead,
                )
            blobInfo
        }

    suspend fun listFile(userId: String): Result<List<BlobInfoWrapper>, Throwable> =
        runCatching {
            val response = Reference.of(bucketName, userId.asFolderName()).listAll()
            val res = mutableListOf<BlobInfoWrapper>()

            for (items in response.prefixes) {
                res.add(items.toKotlinWrapper())
            }

            for (item in response.items) {
                res.add(item.toKotlinWrapper())
            }

            return@runCatching res.toList()
        }

    suspend fun downloadAsset(blobInfoWrapper: BlobInfoWrapper): Result<BlobInfoWrapper?, Throwable> = getFile(blobInfoWrapper.blobId.name)

    suspend fun getFile(fullPath: String): Result<BlobInfoWrapper, Throwable> =
        runCatching {
            Reference.of(bucketName, fullPath).get()
        }

    suspend fun deleteFile(fullPath: String): Result<Unit, Throwable> =
        runCatching {
            Reference.of(bucketName, fullPath).delete()
        }

    suspend fun deleteFolder(folderName: String): Result<Unit, Throwable> =
        runCatching {
            val blobList = Reference.of(bucketName, folderName.asFolderName()).listAll()
            blobList.prefixes.map {
                deleteFolder(it.location.path)
            }
            blobList.items.map {
                it.delete()
            }
        }

    private suspend fun uploadFileWithPath(
        objectName: String,
        filePath: String,
        publicRead: Boolean,
    ): BlobInfoWrapper {
        val fileBytes =
            withContext(Dispatchers.IO) {
                Files.readAllBytes(Paths.get(filePath))
            }
        val ref = Reference.of(bucketName, objectName)
        return ref.upload(ref.toKotlinWrapper(fileBytes))
    }

    private suspend fun uploadFileWithContent(
        objectName: String,
        content: String,
        publicRead: Boolean,
    ): BlobInfoWrapper {
        val ref = Reference.of(bucketName, objectName)
        return ref.upload(
            ref.toKotlinWrapper(content.toByteArray()),
        )
    }
}

/**
 * Append "/" at the end of the name so that the String is able to represents a folder name in
 * Google cloud storage.
 * Google Cloud Storage doesn't have a concept of folder hierarchy, instead it has flat namespace.
 * Thus, adding "/" at the end implies that it represents a folder name.
 */
private fun String.asFolderName() = if (!this.endsWith("/")) "$this/" else this
