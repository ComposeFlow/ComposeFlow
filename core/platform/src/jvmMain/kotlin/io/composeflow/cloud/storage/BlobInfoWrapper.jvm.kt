package io.composeflow.cloud.storage

import com.google.cloud.storage.BlobInfo
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
actual fun Any.toBlobInfoWrapper(contentBytes: ByteArray?): BlobInfoWrapper {
    require(this is BlobInfo) { "Expected BlobInfo but got ${this::class.simpleName}" }
    val split = blobId.name.split("/")
    return BlobInfoWrapper(
        blobId = blobId.toKotlinWrapper(),
        fileName = split.last(),
        folderName = if (split.size >= 2) split[split.size - 2] else "",
        mediaLink = mediaLink,
        contentBytes = contentBytes,
        size = size,
        createTime = createTimeOffsetDateTime?.toKotlinxInstant(),
        updateTime = updateTimeOffsetDateTime?.toKotlinxInstant(),
        deleteTime = deleteTimeOffsetDateTime?.toKotlinxInstant(),
    )
}
