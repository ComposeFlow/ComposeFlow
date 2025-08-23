package io.composeflow.ai

internal actual object YamlDebugger {
    actual suspend fun saveSuccessYaml(
        yamlContent: String,
        promptString: String,
        retryCount: Int,
        requestId: String?,
        timestamp: String,
    ) {
        // No-op for WASM - file operations not supported
    }

    actual suspend fun saveFailedYaml(
        yamlContent: String,
        promptString: String,
        errorMessage: String,
        retryCount: Int,
        requestId: String?,
        timestamp: String,
    ) {
        // No-op for WASM - file operations not supported
    }
}
