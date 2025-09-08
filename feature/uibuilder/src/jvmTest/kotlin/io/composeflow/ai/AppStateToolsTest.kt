package io.composeflow.ai

import io.composeflow.ai.openrouter.tools.ToolArgs
import io.composeflow.ai.openrouter.tools.ToolExecutionStatus
import io.composeflow.model.project.Project
import io.composeflow.model.state.AppState
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for app state tool operations using ToolDispatcher.
 * Verifies that ListAppStatesArgs and GetAppStateArgs properly return data to the LLM.
 */
class AppStateToolsTest {
    private lateinit var project: Project
    private lateinit var toolDispatcher: ToolDispatcher

    @Before
    fun setUp() {
        project = Project()
        toolDispatcher = ToolDispatcher()
    }

    private suspend fun executeToolAndSetStatus(toolArgs: ToolArgs) {
        val eventResult = toolDispatcher.dispatchToolResponse(project, toolArgs)
        toolArgs.status = if (eventResult.isSuccessful()) ToolExecutionStatus.Success else ToolExecutionStatus.Error
    }

    @Test
    fun testListAppStatesArgs_EmptyProject() =
        runTest {
            // Test listing app states in an empty project
            val toolArgs = ToolArgs.ListAppStatesArgs()

            executeToolAndSetStatus(toolArgs)

            assertEquals(ToolExecutionStatus.Success, toolArgs.status)
            assertTrue(toolArgs.result.contains("[]") || toolArgs.result.isBlank()) // Empty list in YAML format
        }

    @Test
    fun testListAppStatesArgs_WithAppStates() =
        runTest {
            // Add some app states to the project
            val stringState1 = AppState.StringAppState(name = "userInput", defaultValue = "Hello")
            val stringState2 = AppState.StringAppState(name = "counter", defaultValue = "0")

            project.globalStateHolder.addState(stringState1)
            project.globalStateHolder.addState(stringState2)

            val toolArgs = ToolArgs.ListAppStatesArgs()

            executeToolAndSetStatus(toolArgs)

            assertEquals(ToolExecutionStatus.Success, toolArgs.status)

            // Result should contain YAML representation of the states
            assertTrue(toolArgs.result.contains("userInput") || toolArgs.result.contains("\"userInput\""))
            assertTrue(toolArgs.result.contains("counter") || toolArgs.result.contains("\"counter\""))
            assertTrue(toolArgs.result.contains("Hello") || toolArgs.result.contains("\"Hello\""))
        }

    @Test
    fun testGetAppStateArgs_ExistingState() =
        runTest {
            // Add an app state to the project
            val stringState = AppState.StringAppState(name = "testState", defaultValue = "test value")
            project.globalStateHolder.addState(stringState)

            val toolArgs = ToolArgs.GetAppStateArgs(appStateId = stringState.id)

            executeToolAndSetStatus(toolArgs)

            assertEquals(ToolExecutionStatus.Success, toolArgs.status)

            // Result should contain YAML representation of the specific state
            assertTrue(toolArgs.result.contains("testState") || toolArgs.result.contains("\"testState\""))
            assertTrue(toolArgs.result.contains("test value") || toolArgs.result.contains("\"test value\""))
        }

    @Test
    fun testGetAppStateArgs_NonExistentState() =
        runTest {
            // Try to get a state that doesn't exist
            val toolArgs = ToolArgs.GetAppStateArgs(appStateId = "non-existent-id")

            executeToolAndSetStatus(toolArgs)

            assertEquals(ToolExecutionStatus.Success, toolArgs.status)

            // Result should indicate the state was not found
            assertTrue(toolArgs.result.contains("not found") || toolArgs.result.contains("null"))
        }

    @Test
    fun testToolArgsResultProperty_IsModifiable() {
        // Test that the result property can be modified (was previously val)
        val toolArgs = ToolArgs.ListAppStatesArgs()

        // Initial value
        assertEquals("Successfully executed.", toolArgs.result)

        // Should be able to modify
        toolArgs.result = "Custom result"
        assertEquals("Custom result", toolArgs.result)

        // Should be able to modify again
        toolArgs.result = "Another result"
        assertEquals("Another result", toolArgs.result)
    }

    @Test
    fun testIntegrationFlow_ListThenGet() =
        runTest {
            // Test a realistic scenario: list states, then get a specific one

            // Add app states
            val state1 = AppState.StringAppState(name = "userName", defaultValue = "Anonymous")
            val state2 = AppState.StringAppState(name = "theme", defaultValue = "light")
            project.globalStateHolder.addState(state1)
            project.globalStateHolder.addState(state2)

            // First, list all states
            val listToolArgs = ToolArgs.ListAppStatesArgs()
            executeToolAndSetStatus(listToolArgs)

            assertEquals(ToolExecutionStatus.Success, listToolArgs.status)
            val listResult = listToolArgs.result

            // Result should contain both states
            assertTrue(listResult.contains("userName") || listResult.contains("\"userName\""))
            assertTrue(listResult.contains("theme") || listResult.contains("\"theme\""))

            // Then, get a specific state
            val getToolArgs = ToolArgs.GetAppStateArgs(appStateId = state1.id)
            executeToolAndSetStatus(getToolArgs)

            assertEquals(ToolExecutionStatus.Success, getToolArgs.status)
            val getResult = getToolArgs.result

            // Result should contain the specific state
            assertTrue(getResult.contains("userName") || getResult.contains("\"userName\""))
            assertTrue(getResult.contains("Anonymous") || getResult.contains("\"Anonymous\""))
            // Should not contain the other state
            assertTrue(!getResult.contains("theme") || !getResult.contains("\"theme\"") || getResult.contains("not found"))
        }

    @Test
    fun testAddAppStateArgs_ValidStringStateYaml() =
        runTest {
            // Test adding a StringAppState via YAML
            val appStateYaml =
                """
!<StringAppState>
id: "test-id-123"
name: "testStringState"
defaultValue: "Hello World"
userWritable: true
                """.trimIndent()

            val toolArgs = ToolArgs.AddAppStateArgs(appStateYaml = appStateYaml)

            executeToolAndSetStatus(toolArgs)

            assertEquals(ToolExecutionStatus.Success, toolArgs.status)
            assertEquals("Successfully executed.", toolArgs.result)

            // Verify state was added to project
            val states = project.globalStateHolder.getStates(project)
            assertEquals(1, states.size)
            val addedState = states.first()
            assertTrue(addedState is AppState.StringAppState)
            assertEquals("testStringState", addedState.name)
            assertEquals("Hello World", (addedState as AppState.StringAppState).defaultValue)
        }

    @Test
    fun testAddAppStateArgs_ValidBooleanStateYaml() =
        runTest {
            // Test adding a BooleanAppState via YAML
            val appStateYaml =
                """
!<BooleanAppState>
id: "bool-id-456"
name: "isDarkMode"
defaultValue: false
userWritable: true
                """.trimIndent()

            val toolArgs = ToolArgs.AddAppStateArgs(appStateYaml = appStateYaml)

            executeToolAndSetStatus(toolArgs)

            assertEquals(ToolExecutionStatus.Success, toolArgs.status)
            assertEquals("Successfully executed.", toolArgs.result)

            // Verify state was added to project
            val states = project.globalStateHolder.getStates(project)
            assertEquals(1, states.size)
            val addedState = states.first()
            assertTrue(addedState is AppState.BooleanAppState)
            assertEquals("isDarkMode", addedState.name)
            assertEquals(false, (addedState as AppState.BooleanAppState).defaultValue)
        }

    @Test
    fun testAddAppStateArgs_InvalidYaml() =
        runTest {
            // Test handling of malformed YAML
            val invalidYaml =
                """
!<StringAppState>
invalid: yaml: structure
name missing quotes
                """.trimIndent()

            val toolArgs = ToolArgs.AddAppStateArgs(appStateYaml = invalidYaml)

            executeToolAndSetStatus(toolArgs)

            assertEquals(ToolExecutionStatus.Error, toolArgs.status)
            assertTrue(toolArgs.result.contains("Failed to parse app state YAML"))

            // Verify no state was added to project
            val states = project.globalStateHolder.getStates(project)
            assertEquals(0, states.size)
        }

    @Test
    fun testAddAppStateArgs_DuplicateName() =
        runTest {
            // Add a state first
            val existingState = AppState.StringAppState(name = "duplicateName", defaultValue = "existing")
            project.globalStateHolder.addState(existingState)

            // Try to add another state with the same name
            val appStateYaml =
                """
!<StringAppState>
id: "new-id-789"
name: "duplicateName"
defaultValue: "new value"
userWritable: true
                """.trimIndent()

            val toolArgs = ToolArgs.AddAppStateArgs(appStateYaml = appStateYaml)

            executeToolAndSetStatus(toolArgs)

            assertEquals(ToolExecutionStatus.Success, toolArgs.status)
            assertEquals("Successfully executed.", toolArgs.result)

            // Verify both states exist with unique names
            val states = project.globalStateHolder.getStates(project)
            assertEquals(2, states.size)
            val stateNames = states.map { it.name }.toSet()
            assertTrue(stateNames.contains("duplicateName"))
            // The new state should have a unique name generated
            assertTrue(stateNames.size == 2) // Both names should be unique
        }

    @Test
    fun testAddAppStateArgs_EmptyProject() =
        runTest {
            // Test adding state to completely empty project
            assertTrue(project.globalStateHolder.getStates(project).isEmpty())

            val appStateYaml =
                """
!<StringAppState>
id: "first-state-id"
name: "firstState" 
defaultValue: "initial"
userWritable: true
                """.trimIndent()

            val toolArgs = ToolArgs.AddAppStateArgs(appStateYaml = appStateYaml)

            executeToolAndSetStatus(toolArgs)

            assertEquals(ToolExecutionStatus.Success, toolArgs.status)
            assertEquals("Successfully executed.", toolArgs.result)

            // Verify state was added
            val states = project.globalStateHolder.getStates(project)
            assertEquals(1, states.size)
            assertEquals("firstState", states.first().name)
        }

    @Test
    fun testAddAppStateArgs_IntegrationWithListAndGet() =
        runTest {
            // Test full workflow: add state via YAML, then list and get it

            // Add state via YAML
            val appStateYaml =
                """
!<StringAppState>
id: "integration-test-id"
name: "integrationState"
defaultValue: "integration test value"
userWritable: true
                """.trimIndent()

            val addArgs = ToolArgs.AddAppStateArgs(appStateYaml = appStateYaml)
            executeToolAndSetStatus(addArgs)
            assertEquals(ToolExecutionStatus.Success, addArgs.status)

            // List states to verify it's there
            val listArgs = ToolArgs.ListAppStatesArgs()
            executeToolAndSetStatus(listArgs)
            assertEquals(ToolExecutionStatus.Success, listArgs.status)
            assertTrue(listArgs.result.contains("integrationState"))

            // Get the specific state
            val states = project.globalStateHolder.getStates(project)
            val addedState = states.find { it.name == "integrationState" }
            assertNotNull(addedState)

            val getArgs = ToolArgs.GetAppStateArgs(appStateId = addedState!!.id)
            executeToolAndSetStatus(getArgs)
            assertEquals(ToolExecutionStatus.Success, getArgs.status)
            assertTrue(getArgs.result.contains("integrationState"))
            assertTrue(getArgs.result.contains("integration test value"))
        }
}
