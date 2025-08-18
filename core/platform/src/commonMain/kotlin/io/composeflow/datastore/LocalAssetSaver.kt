package io.composeflow.datastore

import io.composeflow.cloud.storage.BlobIdWrapper
import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.di.ServiceLocator
import io.composeflow.platform.getCacheDir
import io.composeflow.platform.PlatformFile as LocalPlatformFile
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

class LocalAssetSaver(
    private val ioDispatcher: CoroutineDispatcher =
        ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER) {
            Dispatchers.Default
        },
) {
    fun isAssetExistLocally(
        userId: String,
        projectId: String,
        blobInfoWrapper: BlobInfoWrapper,
    ): Boolean {
        // For now, assume assets don't exist locally in the multiplatform version
        // This would need platform-specific implementations to check file existence
        return false
    }

    suspend fun saveAsset(
        userId: String,
        projectId: String,
        blobInfoWrapper: BlobInfoWrapper,
    ) {
        withContext(ioDispatcher) {
            val assetDir = prepareAssetDir(userId, projectId)
            // Platform-specific file saving would be implemented here
            // For now, this is a no-op
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
            
            // For multiplatform compatibility, create a basic BlobInfoWrapper
            // Platform-specific file copying would be implemented elsewhere
            BlobInfoWrapper(
                blobId =
                    BlobIdWrapper(
                        bucket = "local",
                        name = "$userId/$projectId/asset/${file.name}",
                        generation = null,
                    ),
                fileName = file.name,
                folderName = "$userId/$projectId/asset",
                mediaLink = null,
                size = 0,
                contentBytes = null,
                createTime =
                    kotlin.time.Clock.System
                        .now(),
                updateTime =
                    kotlin.time.Clock.System
                        .now(),
            )
        }

    suspend fun deleteAsset(
        userId: String,
        projectId: String,
        blobInfoWrapper: BlobInfoWrapper,
    ) {
        withContext(ioDispatcher) {
            val assetDir = prepareAssetDir(userId, projectId)
            // Platform-specific file deletion would be implemented here
            // For now, this is a no-op
        }
    }
}

private fun prepareAssetDir(
    userId: String,
    projectId: String,
): LocalPlatformFile {
    val assetsDir =
        getCacheDir()
            .resolve("projects")
            .resolve(userId)
            .resolve(projectId)
            .resolve("assets")
    assetsDir.mkdirs()
    return assetsDir
}
