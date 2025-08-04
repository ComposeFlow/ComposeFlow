@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.composeflow.ai

import co.touchlab.kermit.Logger
import io.composeflow.Res
import io.composeflow.ai.openrouter.tools.ToolArgs
import io.composeflow.ai.openrouter.tools.ToolExecutionStatus
import io.composeflow.ai_failed_to_generate_response
import io.composeflow.ai_failed_to_generate_response_timeout
import io.composeflow.ai_login_needed
import io.composeflow.ai_response_stopped_by_user
import io.composeflow.auth.AuthRepository
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.json.jsonSerializer
import io.composeflow.model.project.Project
import io.composeflow.model.project.asSummarizedContext
import io.composeflow.model.useroperation.OperationHistory
import io.composeflow.model.useroperation.UserOperation
import io.composeflow.removeLineBreak
import io.composeflow.repository.ProjectRepository
import io.composeflow.ui.EventResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AiChatDialogViewModel(
    private val project: Project,
    private val firebaseIdTokenArg: FirebaseIdToken,
    private val aiAssistantUiState: AiAssistantUiState,
    private val llmRepository: LlmRepository = LlmRepository(),
    private val projectRepository: ProjectRepository = ProjectRepository(firebaseIdTokenArg),
    private val toolDispatcher: ToolDispatcher = ToolDispatcher(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val onAiAssistantUiStateUpdated: (AiAssistantUiState) -> Unit,
) : ViewModel() {
    // Observe the firebaseIdToken flow from AuthRepository for automatic refresh
    private val firebaseIdToken =
        authRepository.firebaseIdToken.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )
    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages = _messages.asStateFlow()

    // Message history for navigation
    private val _messageHistory = MutableStateFlow<List<String>>(emptyList())
    val messageHistory = _messageHistory.asStateFlow()

    private val _currentHistoryIndex = MutableStateFlow(-1)
    val currentHistoryIndex = _currentHistoryIndex.asStateFlow()

    private var generationJob: Job? = null

    @OptIn(ExperimentalTime::class)
    fun onSendGeneralRequest(userInput: String) {
        // Add message to history if it's not empty and not a duplicate of the last entry
        if (userInput.isNotBlank() &&
            (messageHistory.value.isEmpty() || messageHistory.value.last() != userInput)
        ) {
            _messageHistory.value += userInput
        }
        // Reset history index
        _currentHistoryIndex.value = -1

        generationJob =
            viewModelScope.launch {
                val firebaseIdTokenRawValue = (firebaseIdToken.value as? FirebaseIdToken.SignedInToken)?.rawToken
                if (firebaseIdTokenRawValue == null) {
                    _messages.value +=
                        MessageModel(
                            messageOwner = MessageOwner.Ai,
                            message = getString(Res.string.ai_login_needed),
                            messageType = MessageType.ToolCallError,
                            isFailed = true,
                            createdAt = Clock.System.now(),
                        )
                    return@launch
                }

                aiAssistantUiState.isGenerating.value = true
                onAiAssistantUiStateUpdated(aiAssistantUiState)
                _messages.value +=
                    MessageModel(
                        messageOwner = MessageOwner.User,
                        message = userInput,
                        createdAt = Clock.System.now(),
                    )
                try {
                    var result: ToolResponse? = null
                    val prompt = userInput.removeLineBreak()
                    val previousToolArgs = mutableListOf<ToolArgs>()
                    while (isActive && (result as? ToolResponse.Success)?.response?.isConsideredComplete() != true) {
                        result =
                            llmRepository.handleToolRequest(
                                firebaseIdToken = firebaseIdTokenRawValue,
                                promptString = prompt,
                                projectContext = project.asSummarizedContext(),
                                previousToolArgs = previousToolArgs,
                            )

                        when (result) {
                            is ToolResponse.Error -> {
                                _messages.value +=
                                    MessageModel(
                                        messageOwner = MessageOwner.Ai,
                                        message = result.message,
                                        isFailed = true,
                                        createdAt = Clock.System.now(),
                                        messageType = MessageType.ToolCallError,
                                    )
                                previousToolArgs.add(
                                    ToolArgs.FakeArgs().apply {
                                        status = ToolExecutionStatus.Error
                                    },
                                )
                            }

                            is ToolResponse.Success -> {
                                Logger.i("Success tool result received:")
                                Logger.i(jsonSerializer.encodeToString(result.response))

                                val toolCallSummary =
                                    result.response.tool_calls?.let { toolCalls ->
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
                                        message = result.message,
                                        createdAt = Clock.System.now(),
                                        messageType =
                                            if (result.response.tool_calls?.isNotEmpty() ==
                                                true
                                            ) {
                                                MessageType.ToolCall
                                            } else {
                                                MessageType.Regular
                                            },
                                        toolCallSummary = toolCallSummary,
                                    )
                                result.response.tool_calls?.forEach {
                                    val toolEventResult = dispatchToolResponse(it.tool_args)
                                    if (toolEventResult.isSuccessful()) {
                                        it.tool_args.status = ToolExecutionStatus.Success
                                        previousToolArgs.add(it.tool_args)
                                        saveProject(project)
                                    } else {
                                        it.tool_args.status = ToolExecutionStatus.Error
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

                    onAiAssistantUiStateUpdated(aiAssistantUiState)
                } catch (timeoutException: TimeoutCancellationException) {
                    val message =
                        getString(Res.string.ai_failed_to_generate_response_timeout)
                    onAiAssistantUiStateUpdated(AiAssistantUiState.Error(message = message))

                    _messages.value +=
                        MessageModel(
                            messageOwner = MessageOwner.Ai,
                            message = message,
                            isFailed = true,
                            createdAt = Clock.System.now(),
                        )
                } catch (e: Exception) {
                    val message =
                        getString(Res.string.ai_failed_to_generate_response)

                    onAiAssistantUiStateUpdated(AiAssistantUiState.Error(message = message))
                    _messages.value +=
                        MessageModel(
                            messageOwner = MessageOwner.Ai,
                            message = message,
                            isFailed = true,
                            createdAt = Clock.System.now(),
                        )
                } finally {
                    aiAssistantUiState.isGenerating.value = false
                }
            }
    }

    private suspend fun dispatchToolResponse(toolArgs: ToolArgs): EventResult {
        if (toolArgs !is ToolArgs.FakeArgs) {
            recordOperation(
                project,
                UserOperation.ExecuteAiTool(
                    toolArgs = toolArgs,
                ),
            )
        }
        return toolDispatcher.dispatchToolResponse(project, toolArgs)
    }

    fun onStopGeneration() {
        generationJob?.cancel()
        aiAssistantUiState.isGenerating.value = false
        onAiAssistantUiStateUpdated(aiAssistantUiState)

        viewModelScope.launch {
            _messages.value +=
                MessageModel(
                    messageOwner = MessageOwner.Ai,
                    message = getString(Res.string.ai_response_stopped_by_user),
                    isFailed = true,
                    createdAt = Clock.System.now(),
                )
        }
    }

    /**
     * Navigate to previous message in history (up arrow)
     * Returns the message to display in the text field, or null if no navigation occurred
     */
    fun navigateToPreviousMessage(): String? {
        val history = messageHistory.value
        if (history.isEmpty()) return null

        val currentIndex = currentHistoryIndex.value
        val newIndex =
            when (currentIndex) {
                -1 -> history.lastIndex // Start from the most recent message
                0 -> 0 // Stay at the oldest message
                else -> currentIndex - 1 // Go to previous message
            }

        _currentHistoryIndex.value = newIndex
        return history[newIndex]
    }

    /**
     * Navigate to next message in history (down arrow)
     * Returns the message to display in the text field, or null if no navigation occurred
     */
    fun navigateToNextMessage(): String? {
        val history = messageHistory.value
        if (history.isEmpty()) return null

        val currentIndex = currentHistoryIndex.value
        if (currentIndex == -1) return null // Not currently navigating

        val newIndex =
            if (currentIndex >= history.lastIndex) {
                -1 // Reset to empty text field
            } else {
                currentIndex + 1 // Go to next message
            }

        _currentHistoryIndex.value = newIndex
        return if (newIndex == -1) "" else history[newIndex]
    }

    private fun saveProject(project: Project) {
        viewModelScope.launch {
            projectRepository.updateProject(project)
        }
    }

    private fun recordOperation(
        project: Project,
        userOperation: UserOperation,
    ) {
        viewModelScope.launch {
            OperationHistory.record(
                project = project,
                userOperation = userOperation,
            )
        }
    }
}
