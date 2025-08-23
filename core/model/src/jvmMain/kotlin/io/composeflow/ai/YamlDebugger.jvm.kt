package io.composeflow.ai

import io.composeflow.platform.getCacheDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual object YamlDebugger {
    actual suspend fun saveSuccessYaml(
        yamlContent: String,
        promptString: String,
        retryCount: Int,
        requestId: String?,
        timestamp: String,
    ) {
        if (!DEBUG_YAML) return
        
        withContext(Dispatchers.IO) {
            try {
                val debugDir =
                    getCacheDir()
                        .resolve("debug")
                        .resolve("success-yaml")
                debugDir.mkdirs()

                val filename =
                    buildString {
                        append("success-yaml-")
                        append(timestamp)
                        requestId?.let { append("-$it") }
                        append("-retry$retryCount")
                        append(".yaml")
                    }

                val yamlFile = debugDir.resolve(filename).toFile()
                val metadataFile = debugDir.resolve("$filename.metadata.txt").toFile()

                // Save the successful YAML content
                yamlFile.writeText(yamlContent)

                // Save metadata about the success
                val metadata =
                    buildString {
                        appendLine("Successful YAML Debug Information")
                        appendLine("=".repeat(50))
                        appendLine("Timestamp: $timestamp")
                        appendLine("Request ID: ${requestId ?: "N/A"}")
                        appendLine("Retry Count: $retryCount")
                        appendLine("=".repeat(50))
                        appendLine()
                        appendLine("Prompt Used:")
                        appendLine(promptString)
                    }
                metadataFile.writeText(metadata)
            } catch (e: Exception) {
                // Ignore errors in debug logging
            }
        }
    }

    actual suspend fun saveFailedYaml(
        yamlContent: String,
        promptString: String,
        errorMessage: String,
        retryCount: Int,
        requestId: String?,
        timestamp: String,
    ) {
        if (!DEBUG_YAML) return
        
        withContext(Dispatchers.IO) {
            try {
                val debugDir =
                    getCacheDir()
                        .resolve("debug")
                        .resolve("failed-yaml")
                debugDir.mkdirs()

                val filename =
                    buildString {
                        append("failed-yaml-")
                        append(timestamp)
                        requestId?.let { append("-$it") }
                        append("-retry$retryCount")
                        append(".yaml")
                    }

                val yamlFile = debugDir.resolve(filename).toFile()
                val metadataFile = debugDir.resolve("$filename.metadata.txt").toFile()

                // Save the failed YAML content
                yamlFile.writeText(yamlContent)

                // Save metadata about the failure
                val metadata =
                    buildString {
                        appendLine("Failed YAML Debug Information")
                        appendLine("=".repeat(50))
                        appendLine("Timestamp: $timestamp")
                        appendLine("Request ID: ${requestId ?: "N/A"}")
                        appendLine("Retry Count: $retryCount")
                        appendLine("Error Message: $errorMessage")
                        appendLine("=".repeat(50))
                        appendLine()
                        appendLine("Prompt Used:")
                        appendLine(promptString)
                    }
                metadataFile.writeText(metadata)
            } catch (e: Exception) {
                // Ignore errors in debug logging
            }
        }
    }
}