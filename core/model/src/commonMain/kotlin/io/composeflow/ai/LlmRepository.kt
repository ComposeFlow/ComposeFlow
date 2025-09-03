package io.composeflow.ai

import androidx.compose.runtime.mutableStateListOf
import co.touchlab.kermit.Logger
import com.github.michaelbull.result.mapBoth
import io.composeflow.ai.openrouter.tools.ToolArgs
import io.composeflow.ai.openrouter.tools.ToolExecutionStatus
import io.composeflow.di.ServiceLocator
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.appscreen.screen.createCopyOfNewName
import io.composeflow.model.project.string.ResourceLocale
import io.composeflow.model.project.string.StringResource
import io.composeflow.serializer.decodeFromStringWithFallback
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * If set to true, record the success and failed deserialization yaml for debugging purpose
 */
const val DEBUG_YAML = false

class LlmRepository(
    private val client: LlmClient = LlmClient(),
    private val ioDispatcher: CoroutineDispatcher =
        ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER) {
            Dispatchers.Default
        },
) {
    companion object {
        private const val MAX_RETRY_COUNT = 4
        private const val MAX_CONTEXT_CHARS = 50000 // Maximum characters for tool context
        private const val MAX_TOOL_COUNT = 20 // Maximum number of tools to keep
    }

    suspend fun createProject(
        firebaseIdToken: String,
        promptString: String,
        retryCount: Int = 0,
    ): CreateProjectResult =
        withTimeout(5.minutes) {
            if (retryCount >= 4) {
                Logger.e("Failed to generate screen. Tried maximum number of attempts.")
                throw IllegalStateException("Failed to generate screen. Tried maximum number of attempts.")
            }

            Logger.i("$promptString,  Retry count: $retryCount")
            val response =
                client.invokeCreateProject(
                    firebaseIdToken = firebaseIdToken,
                    promptString,
                )
            response.mapBoth(
                success = {
                    CreateProjectResult(
                        message = it.message,
                        projectName = it.createProject?.projectName ?: "projectName",
                        packageName = it.createProject?.packageName ?: "com.example",
                        prompts = it.createProject?.screenPrompts ?: mutableStateListOf(),
                    )
                },
                failure = {
                    it.message?.let { message ->
                        Logger.e(message)
                    }
                    throw it
                },
            )
        }

    suspend fun createScreen(
        firebaseIdToken: String,
        promptString: String,
        retryCount: Int = 0,
        requestId: String? = null,
        screenName: String? = null,
        projectContext: ProjectContext? = null,
    ): CreateScreenResponse =
        withTimeout(5.minutes) {
            if (retryCount >= MAX_RETRY_COUNT) {
                Logger.e("Failed to generate screen. Tried maximum number of attempts.")
                throw IllegalStateException("Failed to generate screen. Tried maximum number of attempts.")
            }
            Logger.i("$promptString,  Retry count: $retryCount")
            val response =
                client.invokeGenerateScreen(
                    firebaseIdToken = firebaseIdToken,
                    promptString = promptString,
                    projectContextString = projectContext?.toContextString(),
                )
            response.mapBoth(
                success = {
                    when (val responseDetail = it.responseDetail) {
                        is CreateNewScreen -> {
                            var screen: Screen?
                            var result: CreateScreenResponse
                            try {
                                screen =
                                    decodeFromStringWithFallback<Screen>(
                                        responseDetail.yamlContent,
                                    )
                                if (screenName != null) {
                                    screen = screen.createCopyOfNewName(screenName)
                                }

                                // If any constraints are violated such as, nested scrollable
                                // containers, rethrow the same prompt to LLM.
                                // TODO: Think about a mechanism to reproduce the situation
                                screen.getRootNode().allChildren().forEach { child ->
                                    child.parentNode?.let { parent ->
                                        val errors = child.checkConstraints(parent)
                                        if (errors.isNotEmpty()) {
                                            Logger.w("Constraint errors: $errors")
                                            throw IllegalStateException(
                                                "${child.id}," +
                                                    errors.joinToString(
                                                        ", ",
                                                    ),
                                            )
                                        }
                                    }
                                }

                                if (DEBUG_YAML) {
                                    // Save successful YAML for comparison with failed cases
                                    saveSuccessYamlForDebugging(
                                        yamlContent = responseDetail.yamlContent,
                                        promptString = promptString,
                                        retryCount = retryCount,
                                        requestId = requestId,
                                    )
                                }

                                result =
                                    CreateScreenResponse.Success(
                                        screen = screen,
                                        message = it.message,
                                        requestId = requestId,
                                    )
                            } catch (e: Exception) {
                                Logger.w("Failed to parse the yaml. $e")

                                if (DEBUG_YAML) {
                                    // Save failed YAML for debugging
                                    saveFailedYamlForDebugging(
                                        yamlContent = responseDetail.yamlContent,
                                        errorMessage = e.message ?: "Unknown error",
                                        promptString = promptString,
                                        retryCount = retryCount,
                                        requestId = requestId,
                                    )
                                }

                                result =
                                    CreateScreenResponse.Error(
                                        originalPrompt = promptString,
                                        retryCount = retryCount,
                                        requestId = requestId,
                                        errorMessage = e.message ?: "Unknown error",
                                        errorContent = responseDetail.yamlContent,
                                        throwable = e,
                                    )
                            }
                            result
                        }

                        null -> {
                            throw IllegalStateException("Failed to generate the screen.")
                        }
                    }
                },
                failure = {
                    it.message?.let { message ->
                        Logger.e(message)
                    }
                    throw it
                },
            )
        }

    suspend fun handleToolRequest(
        firebaseIdToken: String,
        promptString: String,
        projectContext: String,
        previousToolArgs: List<ToolArgs> = emptyList(),
        retryCount: Int = 0,
    ): ToolResponse =
        withTimeout(5.minutes) {
            if (previousToolArgs.size >= 3 &&
                previousToolArgs
                    .take(3)
                    .all { it.status == ToolExecutionStatus.Error }
            ) {
                Logger.e("Failed to handle request. Tried maximum number of attempts.")
                throw IllegalStateException("Failed to handle request. Tried maximum number of attempts.")
            }

            // Apply sliding window to manage context size
            val windowedToolArgs = applySlidingWindow(previousToolArgs)

            Logger.i("$promptString,  Retry count: $retryCount")
            val response =
                client.invokeHandleGeneralRequest(
                    firebaseIdToken = firebaseIdToken,
                    promptString = promptString,
                    projectContextString = projectContext,
                    previousToolArgs = windowedToolArgs,
                )
            response.mapBoth(
                success = {
                    ToolResponse.Success(
                        originalPrompt = promptString,
                        message = it.message ?: "Executed successfully",
                        response = it,
                        previousToolArgs = previousToolArgs,
                    )
                },
                failure = {
                    ToolResponse.Error(
                        originalPrompt = promptString,
                        throwable = it,
                        message = it.message ?: "Unknown error",
                        previousToolArgs = previousToolArgs,
                    )
                },
            )
        }

    suspend fun prepareArchitecture(
        firebaseIdToken: String,
        promptString: String,
        projectContext: ProjectContext? = null,
        previousToolArgs: List<ToolArgs> = emptyList(),
        retryCount: Int = 0,
    ): ToolResponse =
        withTimeout(5.minutes) {
            if (retryCount >= MAX_RETRY_COUNT) {
                Logger.e("Failed to prepare architecture. Tried maximum number of attempts.")
                throw IllegalStateException("Failed to prepare architecture. Tried maximum number of attempts.")
            }

            // Apply sliding window to manage context size
            val windowedToolArgs = applySlidingWindow(previousToolArgs)

            Logger.i("Preparing architecture: $promptString, Retry count: $retryCount")

            val promptWithGuidance =
                """
                $promptString

                IMPORTANT: Focus ONLY on preparing the data architecture:
                - Define data types/models needed across the app
                - Define app states and state management structure

                DO NOT:
                - Generate or manipulate UI components
                - Access screen details or UI elements

                This is a pre-generation phase where only the architectural foundation is being established.
                """.trimIndent()

            val response =
                client.invokeHandleGeneralRequest(
                    firebaseIdToken = firebaseIdToken,
                    promptString = promptWithGuidance,
                    projectContextString = projectContext?.toContextString() ?: "",
                    previousToolArgs = windowedToolArgs,
                )
            response.mapBoth(
                success = {
                    ToolResponse.Success(
                        originalPrompt = promptString,
                        message = it.message ?: "Architecture prepared successfully",
                        response = it,
                        previousToolArgs = previousToolArgs,
                    )
                },
                failure = {
                    ToolResponse.Error(
                        originalPrompt = promptString,
                        throwable = it,
                        message = it.message ?: "Unknown error preparing architecture",
                        previousToolArgs = previousToolArgs,
                    )
                },
            )
        }

    suspend fun translateStrings(
        firebaseIdToken: String,
        stringResources: List<StringResource>,
        defaultLocale: ResourceLocale,
        targetLocales: List<ResourceLocale>,
    ): TranslateStringsResult =
        withTimeout(5.minutes) {
            Logger.i("Translating ${stringResources.size} strings from $defaultLocale to ${targetLocales.joinToString()}")

            val translateResources =
                stringResources.map { resource ->
                    TranslateStringResource(
                        key = resource.key,
                        defaultValue = resource.localizedValues[defaultLocale] ?: "",
                        description = resource.description,
                    )
                }

            val response =
                client.invokeTranslateStrings(
                    firebaseIdToken = firebaseIdToken,
                    stringResources = translateResources,
                    defaultLocale = defaultLocale.languageCode,
                    targetLocales = targetLocales.map { it.languageCode },
                )

            response.mapBoth(
                success = { translateResponse ->
                    TranslateStringsResult.Success(
                        translations = translateResponse.translations,
                        message = "Successfully translated ${stringResources.size} strings",
                    )
                },
                failure = {
                    Logger.e("Failed to translate strings: ${it.message}")
                    TranslateStringsResult.Error(
                        message = it.message ?: "Unknown error occurred during translation",
                        throwable = it,
                    )
                },
            )
        }

    /**
     * Applies a sliding window approach to previousToolArgs to prevent sending too much context
     * to the LLM endpoint. This helps avoid large request payloads and potential timeout issues.
     *
     * Strategy:
     * 1. Keep recent tools (last MAX_TOOL_COUNT)
     * 2. Prioritize successful executions over errors
     * 3. Limit total character count to MAX_CONTEXT_CHARS
     * 4. Always keep the most recent tool call regardless of size
     */
    internal fun applySlidingWindow(previousToolArgs: List<ToolArgs>): List<ToolArgs> {
        if (previousToolArgs.isEmpty()) return emptyList()

        // Step 1: Keep only the most recent MAX_TOOL_COUNT tools
        val recentTools = previousToolArgs.takeLast(MAX_TOOL_COUNT)

        // Step 2: Calculate approximate serialized size for each tool
        val toolsWithSize =
            recentTools.map { toolArg ->
                val approximateSize = estimateToolSize(toolArg)
                toolArg to approximateSize
            }

        // Step 3: Apply sliding window based on character count
        val resultTools = mutableListOf<ToolArgs>()
        var totalSize = 0

        // Always include the most recent tool
        if (toolsWithSize.isNotEmpty()) {
            val (mostRecent, mostRecentSize) = toolsWithSize.last()
            resultTools.add(mostRecent)
            totalSize += mostRecentSize
        }

        // Add previous tools from most recent to oldest, respecting size limits
        for (i in toolsWithSize.size - 2 downTo 0) {
            val (tool, size) = toolsWithSize[i]

            // Check if adding this tool would exceed our limit
            if (totalSize + size > MAX_CONTEXT_CHARS) {
                break
            }

            resultTools.add(0, tool) // Add to beginning to maintain order
            totalSize += size
        }

        Logger.i(
            "Applied sliding window: ${previousToolArgs.size} -> ${resultTools.size} tools, " +
                "estimated size: $totalSize chars",
        )

        return resultTools
    }

    /**
     * Estimates the serialized size of a ToolArgs object for sliding window calculations.
     * This provides a quick approximation without full JSON serialization.
     */
    internal fun estimateToolSize(toolArg: ToolArgs): Int {
        // Base serialization overhead (field names, brackets, etc.)
        var estimatedSize = 100

        // Add sizes based on tool content
        estimatedSize += toolArg.status.name.length
        estimatedSize += toolArg.result.length

        // Add tool-specific field sizes
        when (toolArg) {
            is ToolArgs.AddComposeNodeArgs -> {
                estimatedSize += toolArg.containerNodeId.length
                estimatedSize += toolArg.composeNodeYaml.length
                estimatedSize += 20 // indexToDrop
            }

            is ToolArgs.RemoveComposeNodeArgs -> {
                estimatedSize += toolArg.composeNodeId.length
            }

            is ToolArgs.RemoveComposeNodesArgs -> {
                estimatedSize += toolArg.composeNodeIds.length
            }

            is ToolArgs.AddModifierArgs -> {
                estimatedSize += toolArg.composeNodeId.length
                estimatedSize += toolArg.modifierYaml.length
            }

            is ToolArgs.UpdateModifierArgs -> {
                estimatedSize += toolArg.composeNodeId.length
                estimatedSize += toolArg.modifierYaml.length
                estimatedSize += 20 // index
            }

            is ToolArgs.RemoveModifierArgs -> {
                estimatedSize += toolArg.composeNodeId.length
                estimatedSize += 20 // index
            }

            is ToolArgs.SwapModifiersArgs -> {
                estimatedSize += toolArg.composeNodeId.length
                estimatedSize += 40 // fromIndex + toIndex
            }

            is ToolArgs.MoveComposeNodeToContainerArgs -> {
                estimatedSize += toolArg.composeNodeId.length
                estimatedSize += toolArg.containerNodeId.length
                estimatedSize += 20 // index
            }

            is ToolArgs.AddAppStateArgs -> {
                estimatedSize += toolArg.appStateYaml.length
            }

            is ToolArgs.DeleteAppStateArgs -> {
                estimatedSize += toolArg.appStateId.length
            }

            is ToolArgs.UpdateAppStateArgs -> {
                estimatedSize += toolArg.appStateYaml.length
            }

            is ToolArgs.UpdateCustomDataTypeListDefaultValuesArgs -> {
                estimatedSize += toolArg.appStateId.length
                estimatedSize += toolArg.defaultValuesYaml.length
            }

            is ToolArgs.GetAppStateArgs -> {
                estimatedSize += toolArg.appStateId.length
            }

            is ToolArgs.AddDataTypeArgs -> {
                estimatedSize += toolArg.dataTypeYaml.length
            }

            is ToolArgs.DeleteDataTypeArgs -> {
                estimatedSize += toolArg.dataTypeId.length
            }

            is ToolArgs.UpdateDataTypeArgs -> {
                estimatedSize += toolArg.dataTypeYaml.length
            }

            is ToolArgs.AddDataFieldArgs -> {
                estimatedSize += toolArg.dataTypeId.length
                estimatedSize += toolArg.dataFieldYaml.length
            }

            is ToolArgs.DeleteDataFieldArgs -> {
                estimatedSize += toolArg.dataTypeId.length
                estimatedSize += toolArg.dataFieldId.length
            }

            is ToolArgs.AddCustomEnumArgs -> {
                estimatedSize += toolArg.customEnumYaml.length
            }

            is ToolArgs.DeleteCustomEnumArgs -> {
                estimatedSize += toolArg.customEnumId.length
            }

            is ToolArgs.UpdateCustomEnumArgs -> {
                estimatedSize += toolArg.customEnumYaml.length
            }

            is ToolArgs.GetDataTypeArgs -> {
                estimatedSize += toolArg.dataTypeId.length
            }

            is ToolArgs.GetCustomEnumArgs -> {
                estimatedSize += toolArg.customEnumId.length
            }

            is ToolArgs.GetScreenDetailsArgs -> {
                estimatedSize += toolArg.screenId.length
            }

            is ToolArgs.ListAppStatesArgs,
            is ToolArgs.ListDataTypesArgs,
            is ToolArgs.ListCustomEnumsArgs,
            is ToolArgs.GetProjectIssuesArgs,
            is ToolArgs.ListScreensArgs,
            -> {
                // These have minimal content but potentially large results
                estimatedSize += 50
            }

            is ToolArgs.FakeArgs -> {
                estimatedSize += toolArg.fakeString.length
            }
        }

        return estimatedSize
    }

    /**
     * Saves successful YAML content to local filesystem for comparison with failed cases.
     * Files are saved in the cache directory under debug/success-yaml/
     */
    private suspend fun saveSuccessYamlForDebugging(
        yamlContent: String,
        promptString: String,
        retryCount: Int,
        requestId: String?,
    ) {
        if (!DEBUG_YAML) return

        val timestamp =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .toString()
                .replace(":", "-")
                .replace(".", "-")

        YamlDebugger.saveSuccessYaml(
            yamlContent = yamlContent,
            promptString = promptString,
            retryCount = retryCount,
            requestId = requestId,
            timestamp = timestamp,
        )
    }

    /**
     * Saves failed YAML content to local filesystem for debugging purposes.
     * Files are saved in the cache directory under debug/failed-yaml/
     */
    private suspend fun saveFailedYamlForDebugging(
        yamlContent: String,
        errorMessage: String,
        promptString: String,
        retryCount: Int,
        requestId: String?,
    ) {
        if (!DEBUG_YAML) return

        val timestamp =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .toString()
                .replace(":", "-")
                .replace(".", "-")

        YamlDebugger.saveFailedYaml(
            yamlContent = yamlContent,
            promptString = promptString,
            errorMessage = errorMessage,
            retryCount = retryCount,
            requestId = requestId,
            timestamp = timestamp,
        )
    }
}

data class CreateProjectResult(
    val message: String,
    val projectName: String,
    val packageName: String,
    val prompts: MutableList<ScreenPrompt>,
)

data class ProjectContext(
    val screenContexts: List<ScreenContext>,
) {
    fun toContextString(): String =
        "Project context: { screens: [${screenContexts.joinToString(separator = ", ") { it.toContextString() }}] }"
}

data class ScreenContext(
    val id: String,
    val screenName: String,
) {
    fun toContextString() = "Screen(ID: $id, screenName:$screenName)"
}

sealed class TranslateStringsResult {
    data class Success(
        val translations: Map<String, Map<String, String>>, // key -> locale -> translation
        val message: String,
    ) : TranslateStringsResult()

    data class Error(
        val message: String,
        val throwable: Throwable,
    ) : TranslateStringsResult()
}
