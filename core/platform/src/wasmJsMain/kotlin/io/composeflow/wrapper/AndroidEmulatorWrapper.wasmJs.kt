package io.composeflow.wrapper

actual class AndroidEmulatorWrapper actual constructor() {
    actual suspend fun listAvdsAndWait(): List<String> = emptyList()

    actual suspend fun runAvd(
        avdName: String,
        portNumber: Int,
    ) {
        // No-op for WASM
    }
}
