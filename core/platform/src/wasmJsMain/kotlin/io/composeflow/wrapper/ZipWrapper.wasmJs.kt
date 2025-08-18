package io.composeflow.wrapper

import io.composeflow.platform.PlatformFile

actual class ZipWrapper {
    actual fun unzip(sourceFile: PlatformFile, destDirectory: PlatformFile) {
        throw UnsupportedOperationException("Zip operations not available on WASM")
    }
    
    actual fun zipDirectory(input: PlatformFile, output: PlatformFile) {
        throw UnsupportedOperationException("Zip operations not available on WASM")
    }
}