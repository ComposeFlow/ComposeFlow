package io.composeflow.wrapper

import io.composeflow.platform.PlatformFile

actual class TarExtractor {
    actual fun extractTarGz(
        tarGzFile: PlatformFile,
        destinationDirectory: PlatformFile,
    ) {
        throw UnsupportedOperationException("Tar extraction not available on WASM")
    }
}