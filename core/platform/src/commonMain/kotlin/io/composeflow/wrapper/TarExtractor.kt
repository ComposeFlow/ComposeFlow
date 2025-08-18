package io.composeflow.wrapper

import io.composeflow.platform.PlatformFile

expect class TarExtractor() {
    fun extractTarGz(
        tarGzFile: PlatformFile,
        destinationDirectory: PlatformFile,
    )
}
