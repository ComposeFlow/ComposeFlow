package io.composeflow.cloud.storage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
@SerialName("BlobInfoWrapper")
@OptIn(ExperimentalTime::class)
data class BlobInfoWrapper(
    val blobId: BlobIdWrapper,
    val fileName: String,
    val folderName: String,
    val mediaLink: String?,
    val size: Long,
    @Transient
    val contentBytes: ByteArray? = null,
    val createTime: Instant? = null,
    val updateTime: Instant? = null,
    val deleteTime: Instant? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BlobInfoWrapper

        if (blobId != other.blobId) return false
        if (fileName != other.fileName) return false
        if (mediaLink != other.mediaLink) return false
        if (size != other.size) return false
        if (contentBytes != null) {
            if (other.contentBytes == null) return false
            if (!contentBytes.contentEquals(other.contentBytes)) return false
        } else if (other.contentBytes != null) {
            return false
        }
        if (createTime != other.createTime) return false
        if (updateTime != other.updateTime) return false
        if (deleteTime != other.deleteTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blobId.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + (mediaLink?.hashCode() ?: 0)
        result = 31 * result + size.hashCode()
        result = 31 * result + (contentBytes?.contentHashCode() ?: 0)
        result = 31 * result + (createTime?.hashCode() ?: 0)
        result = 31 * result + (updateTime?.hashCode() ?: 0)
        result = 31 * result + (deleteTime?.hashCode() ?: 0)
        return result
    }
}

expect fun Any.toBlobInfoWrapper(contentBytes: ByteArray? = null): BlobInfoWrapper

@OptIn(ExperimentalTime::class)
fun Reference.toKotlinWrapper(contentBytes: ByteArray? = null): BlobInfoWrapper {
    val split = location.path.split("/")
    return BlobInfoWrapper(
        blobId = BlobIdWrapper(location.bucket, location.path, generation = null),
        fileName = split.last(),
        folderName = if (split.size >= 2) split[split.size - 2] else "",
        mediaLink = "",
        size = contentBytes?.size?.toLong() ?: 0,
        contentBytes = contentBytes,
    )
}
