package io.composeflow.model.project.asset

import io.composeflow.asVariableName
import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.platform.getAssetCacheFileFor
import io.composeflow.serializer.FallbackMutableStateListSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("AssetHolder")
data class AssetHolder(
    @Serializable(with = FallbackMutableStateListSerializer::class)
    val images: MutableList<BlobInfoWrapper> = mutableStateListEqualsOverrideOf(),
    @Serializable(with = FallbackMutableStateListSerializer::class)
    val icons: MutableList<BlobInfoWrapper> = mutableStateListEqualsOverrideOf(),
) {
    /**
     * Creates instructions to copy local files to the specified directory for the generated app
     * - First String: The full path of the source file
     * - Second String: The relative path of the destination file where the source file will be copied to
     */
    fun generateCopyLocalFileInstructions(
        userId: String,
        projectId: String,
    ): Map<String, String> =
        images.associate {
            val cacheFile =
                getAssetCacheFileFor(
                    userId = userId,
                    projectId = projectId,
                    blobInfoWrapper = it,
                )
            cacheFile.toFile().path to "composeApp/src/commonMain/composeResources/drawable/${it.fileName.asVariableName()}"
        } +
            icons.associate {
                val cacheFile =
                    getAssetCacheFileFor(
                        userId = userId,
                        projectId = projectId,
                        blobInfoWrapper = it,
                    )
                cacheFile.toFile().path to "composeApp/src/commonMain/composeResources/drawable/${it.fileName.asVariableName()}"
            }
}
