package io.composeflow.wrapper

expect class AndroidEmulatorWrapper() {
    suspend fun listAvdsAndWait(): List<String>
    suspend fun runAvd(avdName: String, portNumber: Int)
}