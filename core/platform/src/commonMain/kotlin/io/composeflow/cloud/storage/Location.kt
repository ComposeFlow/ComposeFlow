package io.composeflow.cloud.storage

import io.ktor.http.encodeURLPath

class Location(
    val bucket: String,
    val path: String,
) {
    fun isRoot(): Boolean = path.isEmpty()

    fun bucketOnlyServerUrl(): String = "b/${bucket.encodeURLPath()}/o"

    fun filename(): String {
        val split = path.split("/")
        return split.last()
    }

    fun folderName(): String {
        val split = path.split("/")
        return if (split.size >= 2) split[split.size - 2] else ""
    }
}
