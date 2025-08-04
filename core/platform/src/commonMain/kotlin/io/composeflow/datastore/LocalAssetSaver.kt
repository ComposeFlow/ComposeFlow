package io.composeflow.datastore

import io.composeflow.cloud.storage.BlobIdWrapper
import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.di.ServiceLocator
import io.composeflow.platform.getCacheDir
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.time.ExperimentalTime

class LocalAssetSaver(
    private val ioDispatcher: CoroutineDispatcher =
        ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER) {
            Dispatchers.IO
        },
) {
    fun isAssetExistLocally(
        userId: String,
        projectId: String,
        blobInfoWrapper: BlobInfoWrapper,
    ): Boolean {
        val assetDir = prepareAssetDir(userId, projectId)
        val file = assetDir.resolve(blobInfoWrapper.fileName)
        return file.exists()
    }

    suspend fun saveAsset(
        userId: String,
        projectId: String,
        blobInfoWrapper: BlobInfoWrapper,
    ) {
        withContext(ioDispatcher) {
            val assetDir = prepareAssetDir(userId, projectId)
            val file = assetDir.resolve(blobInfoWrapper.fileName)
            blobInfoWrapper.contentBytes?.let {
                file.writeBytes(it)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun saveAssetLocally(
        userId: String,
        projectId: String,
        file: PlatformFile,
    ): BlobInfoWrapper =
        withContext(ioDispatcher) {
            val assetDir = prepareAssetDir(userId, projectId)
            val localFile = assetDir.resolve(file.name)

            // Copy file content to local cache
            file.path?.let { filePath ->
                val fileBytes = Files.readAllBytes(Paths.get(filePath))
                localFile.writeBytes(fileBytes)

                // Create a BlobInfoWrapper for local asset
                BlobInfoWrapper(
                    blobId =
                        BlobIdWrapper(
                            bucket = "local",
                            name = "$userId/$projectId/asset/${file.name}",
                            generation = System.currentTimeMillis(),
                        ),
                    fileName = file.name,
                    folderName = "$userId/$projectId/asset",
                    mediaLink = localFile.absolutePath,
                    size = fileBytes.size.toLong(),
                    contentBytes = fileBytes,
                    createTime =
                        kotlin.time.Clock.System
                            .now(),
                    updateTime =
                        kotlin.time.Clock.System
                            .now(),
                )
            } ?: throw Exception("File path is null")
        }

    suspend fun deleteAsset(
        userId: String,
        projectId: String,
        blobInfoWrapper: BlobInfoWrapper,
    ) {
        withContext(ioDispatcher) {
            val assetDir = prepareAssetDir(userId, projectId)
            val file = assetDir.resolve(blobInfoWrapper.fileName)
            file.delete()
        }
    }
}

private fun prepareAssetDir(
    userId: String,
    projectId: String,
): File {
    val assetsDir =
        getCacheDir()
            .resolve("projects")
            .resolve(userId)
            .resolve(projectId)
            .resolve("assets")
    assetsDir.mkdirs()
    return assetsDir
}
