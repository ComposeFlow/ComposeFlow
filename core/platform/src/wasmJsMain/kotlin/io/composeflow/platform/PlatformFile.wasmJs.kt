package io.composeflow.platform

actual class PlatformFile(
    actual val path: String = "",
) {
    actual fun resolve(path: String): PlatformFile = PlatformFile("${this.path}/$path")

    actual fun mkdirs(): Boolean = true // No-op for WASM

    actual fun exists(): Boolean = false // No-op for WASM

    actual fun deleteRecursively(): Boolean = true // No-op for WASM

    actual fun listFiles(): List<PlatformFile>? = null // No-op for WASM

    actual val name: String get() = path.substringAfterLast("/")
}

actual fun getCacheDir(): PlatformFile {
    // WASM doesn't have a traditional file system
    return PlatformFile("/tmp/composeflow")
}
