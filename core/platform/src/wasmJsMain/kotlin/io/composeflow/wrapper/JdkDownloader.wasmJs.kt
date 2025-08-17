package io.composeflow.wrapper

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual object JdkDownloader {
    actual suspend fun downloadAndExtract(
        downloadableJdk: DownloadableJdk,
        tarExtractor: TarExtractor,
        dispatcher: CoroutineDispatcher,
    ): String? {
        Logger.i("JdkDownloader.downloadAndExtract called on WASM - not supported, returning null")
        return null
    }
}