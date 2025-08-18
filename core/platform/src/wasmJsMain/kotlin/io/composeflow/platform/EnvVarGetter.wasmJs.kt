package io.composeflow.platform

actual fun getEnvVar(name: String): String? {
    // Environment variables are not accessible in WASM browser environment
    return null
}