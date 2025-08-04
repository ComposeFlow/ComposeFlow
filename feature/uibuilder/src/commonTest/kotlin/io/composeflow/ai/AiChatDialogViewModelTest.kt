package io.composeflow.ai

import io.composeflow.ai.AiAssistantUiState
import io.composeflow.ai.AiChatDialogViewModel
import io.composeflow.ai.LlmRepository
import io.composeflow.ai.ToolDispatcher
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.model.project.Project
import io.composeflow.repository.ProjectRepository
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for AiChatDialogViewModel message history functionality.
 * Verifies that message history storage and navigation work correctly.
 */
class AiChatDialogViewModelTest {
    private lateinit var project: Project
    private lateinit var aiAssistantUiState: AiAssistantUiState
    private lateinit var firebaseIdToken: FirebaseIdToken
    private lateinit var viewModel: AiChatDialogViewModel
    private var onAiAssistantUiStateUpdatedCallCount = 0

    @Before
    fun setUp() {
        project = Project()
        aiAssistantUiState = AiAssistantUiState.Idle
        firebaseIdToken = createMockFirebaseIdToken()
        onAiAssistantUiStateUpdatedCallCount = 0

        viewModel =
            AiChatDialogViewModel(
                project = project,
                firebaseIdTokenArg = firebaseIdToken,
                aiAssistantUiState = aiAssistantUiState,
                llmRepository = LlmRepository(),
                projectRepository = ProjectRepository(firebaseIdToken),
                toolDispatcher = ToolDispatcher(),
                onAiAssistantUiStateUpdated = { onAiAssistantUiStateUpdatedCallCount++ },
            )
    }

    private fun createMockFirebaseIdToken(): FirebaseIdToken =
        FirebaseIdToken.SignedInToken(
            name = "Test User",
            picture = "https://example.com/picture.jpg",
            iss = "https://securetoken.google.com/test-project",
            aud = "test-project",
            auth_time = 1234567890L,
            user_id = "test-user-id",
            sub = "test-user-id",
            iat = 1234567890L,
            exp = 1234567890L + 3600L,
            email = "test@example.com",
            email_verified = true,
            firebase = JsonPrimitive("test-firebase-data"),
            rawToken = "test-raw-token",
        )

    // Message History Storage Tests

    @Test
    fun testMessageHistory_InitiallyEmpty() {
        assertEquals(emptyList(), viewModel.messageHistory.value)
        assertEquals(-1, viewModel.currentHistoryIndex.value)
    }

    // Note: Tests that call onSendGeneralRequest are commented out because they require
    // LlmRepository network calls. The message history functionality can be tested
    // indirectly through the navigation functions.

    // Navigation Function Tests

    @Test
    fun testNavigateToPreviousMessage_EmptyHistory() {
        val result = viewModel.navigateToPreviousMessage()
        assertNull(result)
        assertEquals(-1, viewModel.currentHistoryIndex.value)
    }

    // Navigation tests with non-empty history would require calling onSendGeneralRequest,
    // which involves LlmRepository network calls. For now, testing with empty history only.

    @Test
    fun testNavigateToNextMessage_EmptyHistory() {
        val result = viewModel.navigateToNextMessage()
        assertNull(result)
        assertEquals(-1, viewModel.currentHistoryIndex.value)
    }

    // All other navigation tests require calling onSendGeneralRequest which involves
    // LlmRepository network calls. These tests would need to be integration tests
    // or require a different testing approach with dependency injection.
}
