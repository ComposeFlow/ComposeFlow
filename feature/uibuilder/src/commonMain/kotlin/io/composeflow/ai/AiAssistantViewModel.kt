@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.composeflow.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import co.touchlab.kermit.Logger
import io.composeflow.Res
import io.composeflow.ai.openrouter.tools.ToolArgs
import io.composeflow.ai.openrouter.tools.ToolExecutionStatus
import io.composeflow.ai.subaction.GeneratedScreenPrompt
import io.composeflow.ai_failed_to_generate_response
import io.composeflow.ai_failed_to_generate_response_timeout
import io.composeflow.ai_login_needed
import io.composeflow.ai_preparing_architecture
import io.composeflow.auth.AuthRepository
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.appscreen.screen.postProcessAfterAiGeneration
import io.composeflow.removeLineBreak
import io.composeflow.serializer.encodeToString
import io.composeflow.ui.EventResult
import io.composeflow.util.toKotlinFileName
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AiAssistantViewModel(
    projectCreationQuery: String = "",
    initialMessage: String? = null,
    authRepository: AuthRepository = AuthRepository(),
    private val llmRepository: LlmRepository = LlmRepository(),
    private val toolDispatcher: ToolDispatcher = ToolDispatcher(),
) : ViewModel() {
    // Observe the firebaseIdToken flow from AuthRepository for automatic refresh
    private val firebaseIdToken =
        authRepository.firebaseIdToken.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )
    private val _uiState = MutableStateFlow<AiAssistantUiState>(AiAssistantUiState.Idle)
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages = _messages.asStateFlow()

    var initialProject by mutableStateOf(Project())
        private set

    init {
        if (projectCreationQuery.isNotBlank()) {
            // Wait for firebaseIdToken to be available before calling onSendProjectCreationQuery
            viewModelScope.launch {
                firebaseIdToken.collect { token ->
                    if (token != null) {
                        onSendProjectCreationQuery(projectCreationQuery)
                        return@collect // Only call once
                    }
                }
            }
        }

        initialMessage?.let {
            _messages.value +=
                MessageModel(
                    messageOwner = MessageOwner.Ai,
                    message = initialMessage,
                    createdAt = Clock.System.now(),
                )
        }
    }

    private fun onSendProjectCreationQuery(projectCreationQuery: String) {
        viewModelScope.launch {
            val rawToken = firebaseIdToken.value?.rawToken

            if (rawToken == null) {
                _messages.value +=
                    MessageModel(
                        messageOwner = MessageOwner.Ai,
                        message = getString(Res.string.ai_login_needed),
                        isFailed = true,
                        createdAt = Clock.System.now(),
                    )
                return@launch
            }

            _messages.value +=
                MessageModel(
                    messageOwner = MessageOwner.User,
                    message = projectCreationQuery,
                    createdAt = Clock.System.now(),
                )
            _uiState.value.isGenerating.value = true

            val result =
                llmRepository.createProject(
                    firebaseIdToken = rawToken,
                    promptString = projectCreationQuery,
                )

            _messages.value +=
                MessageModel(
                    messageOwner = MessageOwner.Ai,
                    message = result.message,
                    createdAt = Clock.System.now(),
                )
            _uiState.value.isGenerating.value = false

            initialProject =
                initialProject.copy(name = result.projectName, packageName = result.packageName)
            _uiState.value =
                AiAssistantUiState.ScreensCreationSuccess.ScreenPromptsCreated(
                    packageName = result.packageName,
                    screenPrompts =
                        result.prompts
                            .map {
                                GeneratedScreenPrompt.BeforeGeneration(
                                    id = it.screenName.toKotlinFileName(),
                                    screenName = it.screenName,
                                    prompt = it.prompt,
                                )
                            }.toMutableStateList(),
                )
        }
    }

    fun onScreenTitleUpdated(
        id: String,
        newTitle: String,
    ) {
        when (val state = _uiState.value) {
            is AiAssistantUiState.ScreensCreationSuccess.ScreenPromptsCreated -> {
                val updatedPrompts =
                    state.screenPrompts.map { prompt ->
                        when (prompt) {
                            is GeneratedScreenPrompt.BeforeGeneration -> {
                                if (prompt.screenName == id) {
                                    GeneratedScreenPrompt.BeforeGeneration(
                                        id = prompt.id,
                                        screenName = newTitle,
                                        prompt = prompt.prompt,
                                    )
                                } else {
                                    prompt
                                }
                            }

                            else -> {
                                prompt
                            }
                        }
                    }
                _uiState.value = state.copy(screenPrompts = updatedPrompts.toMutableStateList())
            }

            else -> {}
        }
    }

    fun onScreenPromptDeleted(id: String) {
        when (val state = _uiState.value) {
            is AiAssistantUiState.ScreensCreationSuccess.ScreenPromptsCreated -> {
                val updatedPrompts =
                    state.screenPrompts.filter { prompt -> prompt.id != id }
                _uiState.value = state.copy(screenPrompts = updatedPrompts.toMutableStateList())
            }

            else -> {}
        }
    }

    fun onScreenPromptUpdated(
        id: String,
        newPrompt: String,
    ) {
        when (val state = _uiState.value) {
            is AiAssistantUiState.ScreensCreationSuccess.ScreenPromptsCreated -> {
                val updatedPrompts =
                    state.screenPrompts.map { prompt ->
                        when (prompt) {
                            is GeneratedScreenPrompt.BeforeGeneration -> {
                                if (prompt.screenName == id) {
                                    GeneratedScreenPrompt.BeforeGeneration(
                                        id = prompt.id,
                                        screenName = prompt.screenName,
                                        prompt = newPrompt,
                                    )
                                } else {
                                    prompt
                                }
                            }

                            else -> {
                                prompt
                            }
                        }
                    }
                _uiState.value = state.copy(screenPrompts = updatedPrompts.toMutableStateList())
            }

            else -> {}
        }
    }

    fun onProceedToGenerateScreens() {
        viewModelScope.launch {
            when (val state = _uiState.value) {
                is AiAssistantUiState.ScreensCreationSuccess -> {
                    val originalPrompts = state.screenPrompts
                    val screenNames = originalPrompts.joinToString { it.screenName }
                    val updatedPrompts = originalPrompts.toMutableList()

                    val firebaseIdTokenRawValue = firebaseIdToken.value?.rawToken
                    if (firebaseIdTokenRawValue == null) {
                        _messages.value +=
                            MessageModel(
                                messageOwner = MessageOwner.Ai,
                                message = getString(Res.string.ai_login_needed),
                                isFailed = true,
                                createdAt = Clock.System.now(),
                            )
                        return@launch
                    }
                    // Call prepare_architecture endpoint before generating screens
                    try {
                        val projectContext =
                            ProjectContext(
                                screenContexts =
                                    state.screenPrompts.map {
                                        ScreenContext(
                                            id = it.id,
                                            screenName = it.screenName,
                                        )
                                    },
                            )
                        _uiState.value.isGenerating.value = true
                        _messages.value +=
                            MessageModel(
                                messageOwner = MessageOwner.Ai,
                                message = getString(Res.string.ai_preparing_architecture),
                                createdAt = Clock.System.now(),
                                messageType = MessageType.Regular,
                            )

                        var architectureResponse: ToolResponse? = null
                        val previousToolArgs = mutableListOf<ToolArgs>()
                        // Dispatch tool results if successful
                        while ((architectureResponse as? ToolResponse.Success)?.response?.isConsideredComplete() != true) {
                            architectureResponse =
                                llmRepository.prepareArchitecture(
                                    firebaseIdToken = firebaseIdTokenRawValue,
                                    promptString = "Prepare the architecture for an app with these screens: $screenNames",
                                    projectContext = projectContext,
                                    previousToolArgs = previousToolArgs,
                                )

                            when (architectureResponse) {
                                is ToolResponse.Error -> {
                                    _messages.value +=
                                        MessageModel(
                                            messageOwner = MessageOwner.Ai,
                                            message = architectureResponse.message,
                                            isFailed = true,
                                            createdAt = Clock.System.now(),
                                            messageType = MessageType.ToolCallError,
                                        )
                                    previousToolArgs.add(
                                        ToolArgs.FakeArgs().apply {
                                            status =
                                                ToolExecutionStatus.Error
                                        },
                                    )
                                }

                                is ToolResponse.Success -> {
                                    Logger.i("Success tool result received:")
                                    Logger.i(jsonSerializer.encodeToString(architectureResponse.response))

                                    val toolCallSummary =
                                        architectureResponse.response.tool_calls?.let { toolCalls ->
                                            if (toolCalls.isNotEmpty()) {
                                                "Tool calls: ${
                                                    toolCalls.joinToString(", ") {
                                                        it.tool_args.javaClass.simpleName.removeSuffix(
                                                            "Args",
                                                        )
                                                    }
                                                }"
                                            } else {
                                                null
                                            }
                                        }

                                    _messages.value +=
                                        MessageModel(
                                            messageOwner = MessageOwner.Ai,
                                            message = architectureResponse.message,
                                            createdAt = Clock.System.now(),
                                            messageType =
                                                if (architectureResponse.response.tool_calls?.isNotEmpty() ==
                                                    true
                                                ) {
                                                    MessageType.ToolCall
                                                } else {
                                                    MessageType.Regular
                                                },
                                            toolCallSummary = toolCallSummary,
                                        )
                                    architectureResponse.response.tool_calls?.forEach {
                                        val toolEventResult =
                                            dispatchToolResponse(initialProject, it.tool_args)
                                        if (toolEventResult.isSuccessful()) {
                                            it.tool_args.status = ToolExecutionStatus.Success
                                            previousToolArgs.add(it.tool_args)
                                        } else {
                                            it.tool_args.status =
                                                ToolExecutionStatus.Error
                                            it.tool_args.result =
                                                toolEventResult.errorMessages.joinToString("\n")
                                            previousToolArgs.add(it.tool_args)

                                            _messages.value +=
                                                MessageModel(
                                                    messageOwner = MessageOwner.Ai,
                                                    message =
                                                        toolEventResult.errorMessages.joinToString(
                                                            "\n",
                                                        ),
                                                    isFailed = true,
                                                    createdAt = Clock.System.now(),
                                                    messageType = MessageType.ToolCallError,
                                                )
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Logger.w("Failed to prepare architecture: ${e.message}")
                    } finally {
                        _uiState.value =
                            AiAssistantUiState.ScreensCreationSuccess.InitialProjectCreated(
                                project = initialProject,
                                screenPrompts = state.screenPrompts,
                            )
                    }

                    val semaphore = Semaphore(permits = MAX_CONCURRENT_LLM_CALLS)

                    val jobs =
                        originalPrompts.mapIndexed { index, prompt ->
                            async {
                                if (prompt is GeneratedScreenPrompt.BeforeGeneration) {
                                    semaphore.withPermit {
                                        // Mark as Generating
                                        updatedPrompts[index] =
                                            GeneratedScreenPrompt.Generating(
                                                id = prompt.id,
                                                screenName = prompt.screenName,
                                                prompt = prompt.prompt,
                                            )
                                        (_uiState.value as? AiAssistantUiState.ScreensCreationSuccess.InitialProjectCreated)?.let {
                                            it.screenPrompts = updatedPrompts.toMutableStateList()
                                        }
                                        _uiState.value.isGenerating.value = true

                                        var result: CreateScreenResponse =
                                            CreateScreenResponse.BeforeRequest(prompt.id)
                                        var promptString = prompt.prompt
                                        var retryCount = 0
                                        do {
                                            try {
                                                result =
                                                    llmRepository.createScreen(
                                                        firebaseIdToken = firebaseIdTokenRawValue,
                                                        promptString = promptString,
                                                        retryCount = retryCount,
                                                        requestId = prompt.id,
                                                        projectContext =
                                                            ProjectContext(
                                                                screenContexts =
                                                                    state.screenPrompts.map {
                                                                        ScreenContext(
                                                                            id = it.id,
                                                                            screenName = it.screenName,
                                                                        )
                                                                    },
                                                            ),
                                                    )
                                                if (result is CreateScreenResponse.Error) {
                                                    val indexOfScreen =
                                                        updatedPrompts.indexOfFirst { it.id == result.requestId }
                                                    updatedPrompts[indexOfScreen] =
                                                        GeneratedScreenPrompt.Error(
                                                            id = prompt.id,
                                                            screenName = prompt.screenName,
                                                            prompt = prompt.prompt,
                                                            errorMessage = result.errorMessage,
                                                        )
                                                    retryCount++
                                                    promptString =
                                                        "${result.errorMessage}. Fix the yaml parse error. Previously generated Yaml: ${result.errorContent}"
                                                }
                                            } catch (e: Exception) {
                                                val indexOfScreen =
                                                    updatedPrompts.indexOfFirst { it.id == result.requestId }
                                                updatedPrompts[indexOfScreen] =
                                                    GeneratedScreenPrompt.Error(
                                                        id = prompt.id,
                                                        screenName = prompt.screenName,
                                                        prompt = prompt.prompt,
                                                        withRetry = false,
                                                        errorMessage = e.message ?: "Unknown error",
                                                    )
                                            }
                                        } while (result is CreateScreenResponse.Error && retryCount < MAX_RETRY_COUNT)

                                        when (result) {
                                            is CreateScreenResponse.Success -> {
                                                val indexOfScreen =
                                                    updatedPrompts.indexOfFirst { it.id == result.requestId }
                                                updatedPrompts[indexOfScreen] =
                                                    GeneratedScreenPrompt.ScreenGenerated(
                                                        id = prompt.id,
                                                        screen =
                                                            result.screen.postProcessAfterAiGeneration(
                                                                newId = prompt.id,
                                                            ),
                                                        prompt = prompt.prompt,
                                                        screenName = prompt.screenName,
                                                    )
                                            }

                                            else -> {
                                                // We care only success state here
                                            }
                                        }
                                        (_uiState.value as? AiAssistantUiState.ScreensCreationSuccess.InitialProjectCreated)?.let {
                                            _uiState.value =
                                                it.copy(screenPrompts = updatedPrompts.toMutableStateList())
                                        }
                                    }
                                }
                            }
                        }

                    jobs.forEach { it.await() }
                    _uiState.value.isGenerating.value = false
                }

                else -> Unit
            }
        }
    }

    fun onRenderedErrorDetected(errorPrompt: GeneratedScreenPrompt.Error) {
        when (val state = _uiState.value) {
            is AiAssistantUiState.ScreensCreationSuccess.ScreenPromptsCreated -> {
                val originalPrompts = state.screenPrompts
                val updatedPrompts = originalPrompts.toMutableList()
                val indexOfScreen =
                    updatedPrompts.indexOfFirst { it.id == errorPrompt.id }
                if (indexOfScreen != -1) {
                    updatedPrompts[indexOfScreen] = errorPrompt
                }
                _uiState.value = state.copy(screenPrompts = updatedPrompts.toMutableStateList())
            }

            else -> {
                // We only care the ScreenPromptsCreated case. Intentionally using else block
            }
        }
    }

    fun onSendCreateScreenRequest(userInput: String) {
        viewModelScope.launch {
            // If already generated content exists, include it in the prompt.
            val existingContext =
                when (val state = uiState.value) {
                    is AiAssistantUiState.Success.NewScreenCreated -> {
                        encodeToString(
                            Screen.serializer(),
                            state.screen,
                        ) + " "
                    }

                    else -> ""
                }

            val firebaseIdTokenRawValue = firebaseIdToken.value?.rawToken
            if (firebaseIdTokenRawValue == null) {
                _messages.value +=
                    MessageModel(
                        messageOwner = MessageOwner.Ai,
                        message = getString(Res.string.ai_login_needed),
                        isFailed = true,
                        createdAt = Clock.System.now(),
                    )
                return@launch
            }
            _uiState.value.isGenerating.value = true

            _messages.value +=
                MessageModel(
                    messageOwner = MessageOwner.User,
                    message = userInput,
                    createdAt = Clock.System.now(),
                )
            try {
                var result: CreateScreenResponse
                var prompt = existingContext + userInput.removeLineBreak()
                var retryCount = 0
                do {
                    result =
                        llmRepository.createScreen(
                            firebaseIdToken = firebaseIdTokenRawValue,
                            promptString = prompt,
                            retryCount = retryCount,
                        )
                    if (result is CreateScreenResponse.Error) {
                        _messages.value +=
                            MessageModel(
                                messageOwner = MessageOwner.Ai,
                                message = result.errorMessage,
                                isFailed = true,
                                createdAt = Clock.System.now(),
                            )
                        retryCount++
                        prompt =
                            "${result.errorMessage}. Fix the yaml parse error. Previously generated Yaml: ${result.errorContent}"
                    }
                } while (result is CreateScreenResponse.Error && retryCount < MAX_RETRY_COUNT)

                when (result) {
                    is CreateScreenResponse.Success -> {
                        _messages.value +=
                            MessageModel(
                                messageOwner = MessageOwner.Ai,
                                message = result.message,
                                createdAt = Clock.System.now(),
                            )
                        _uiState.value = AiAssistantUiState.Success.NewScreenCreated(result.screen)
                    }

                    else -> {
                        // We care only the success state here
                    }
                }
                _uiState.value.isGenerating.value = false
            } catch (timeoutException: TimeoutCancellationException) {
                val message = getString(Res.string.ai_failed_to_generate_response_timeout)
                _uiState.value =
                    AiAssistantUiState.Error(message = message)
                _messages.value +=
                    MessageModel(
                        messageOwner = MessageOwner.Ai,
                        message = message,
                        isFailed = true,
                        createdAt = Clock.System.now(),
                    )
            } catch (e: Exception) {
                val message = getString(Res.string.ai_failed_to_generate_response)
                _uiState.value =
                    AiAssistantUiState.Error(message = message)
                _messages.value +=
                    MessageModel(
                        messageOwner = MessageOwner.Ai,
                        message = message,
                        isFailed = true,
                        createdAt = Clock.System.now(),
                    )
            }
        }
    }

    fun onDiscardResult() {
        _uiState.value = AiAssistantUiState.Idle
        _uiState.value.isGenerating.value = false
    }

    private suspend fun dispatchToolResponse(
        project: Project,
        toolArgs: ToolArgs,
    ): EventResult = toolDispatcher.dispatchToolResponse(project, toolArgs)
}
