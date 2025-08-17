package io.composeflow.wrapper

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

sealed interface DownloadableJdk {
    fun getDownloadUrl(): String

    fun getJdkDirName(): String

    data object OpenJdk17 : DownloadableJdk {
        override fun getJdkDirName(): String = "openjdk-17"

        override fun getDownloadUrl(): String = "" // Will be implemented in platform-specific versions
    }
}

expect object JdkDownloader {
    suspend fun downloadAndExtract(
        downloadableJdk: DownloadableJdk = DownloadableJdk.OpenJdk17,
        tarExtractor: TarExtractor = TarExtractor(),
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ): String?
}
