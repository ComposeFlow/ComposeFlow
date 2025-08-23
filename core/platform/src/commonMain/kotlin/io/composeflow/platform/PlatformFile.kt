package io.composeflow.platform

import io.composeflow.cloud.storage.BlobInfoWrapper

expect class PlatformFile {
    fun resolve(path: String): PlatformFile

    fun mkdirs(): Boolean

    fun exists(): Boolean

    fun deleteRecursively(): Boolean

    fun listFiles(): List<PlatformFile>?

    val name: String
    
    val path: String
}

expect fun getCacheDir(): PlatformFile

fun getAssetCacheFileFor(
    userId: String,
    projectId: String,
    blobInfoWrapper: BlobInfoWrapper,
): PlatformFile {
    val asset =
        getCacheDir()
            .resolve("projects")
            .resolve(userId)
            .resolve(projectId)
            .resolve("assets")
            .resolve(blobInfoWrapper.fileName)
    return asset
}
