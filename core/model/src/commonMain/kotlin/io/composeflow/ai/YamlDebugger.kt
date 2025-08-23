package io.composeflow.ai

internal expect object YamlDebugger {
    suspend fun saveSuccessYaml(
        yamlContent: String,
        promptString: String,
        retryCount: Int,
        requestId: String?,
        timestamp: String,
    )

    suspend fun saveFailedYaml(
        yamlContent: String,
        promptString: String,
        errorMessage: String,
        retryCount: Int,
        requestId: String?,
        timestamp: String,
    )
}
