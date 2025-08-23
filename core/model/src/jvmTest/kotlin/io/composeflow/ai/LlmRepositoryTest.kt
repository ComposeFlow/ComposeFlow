package io.composeflow.ai

import io.composeflow.ai.openrouter.tools.ToolArgs
import io.composeflow.ai.openrouter.tools.ToolExecutionStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlin.time.ExperimentalTime::class)
class LlmRepositoryTest {
    private val repository = LlmRepository()

    @Test
    fun testSlidingWindowWithEmptyList() {
        val result = repository.applySlidingWindow(emptyList())
        assertEquals(0, result.size)
    }

    @Test
    fun testSlidingWindowWithSmallList() {
        val toolArgs =
            listOf(
                ToolArgs.RemoveComposeNodeArgs("node1").apply {
                    status = ToolExecutionStatus.Success
                    result = "Success"
                },
                ToolArgs.RemoveComposeNodeArgs("node2").apply {
                    status = ToolExecutionStatus.Success
                    result = "Success"
                },
            )

        val result = repository.applySlidingWindow(toolArgs)
        assertEquals(2, result.size)
        assertEquals("node1", (result[0] as ToolArgs.RemoveComposeNodeArgs).composeNodeId)
        assertEquals("node2", (result[1] as ToolArgs.RemoveComposeNodeArgs).composeNodeId)
    }

    @Test
    fun testSlidingWindowKeepsMostRecentTool() {
        // Create tools with large YAML content to exceed limit
        val largeYaml = "x".repeat(30000)
        val toolArgs =
            listOf(
                ToolArgs.AddComposeNodeArgs("container1", largeYaml, 0).apply {
                    status = ToolExecutionStatus.Success
                    result = "Success"
                },
                ToolArgs.AddComposeNodeArgs("container2", largeYaml, 0).apply {
                    status = ToolExecutionStatus.Success
                    result = "Success"
                },
                ToolArgs.RemoveComposeNodeArgs("node3").apply {
                    status = ToolExecutionStatus.Success
                    result = "Success"
                },
            )

        val result = repository.applySlidingWindow(toolArgs)
        // Should keep at least the most recent tool
        assertTrue(result.isNotEmpty())
        assertEquals("node3", (result.last() as ToolArgs.RemoveComposeNodeArgs).composeNodeId)
    }

    @Test
    fun testEstimateToolSizeForDifferentTypes() {
        val removeNodeTool = ToolArgs.RemoveComposeNodeArgs("nodeId123")
        val addNodeTool = ToolArgs.AddComposeNodeArgs("containerId", "yaml content here", 5)
        val listTool = ToolArgs.ListScreensArgs()

        val removeSize = repository.estimateToolSize(removeNodeTool)
        val addSize = repository.estimateToolSize(addNodeTool)
        val listSize = repository.estimateToolSize(listTool)

        // AddComposeNodeArgs should be larger due to YAML content
        assertTrue(addSize > removeSize, "Add size ($addSize) should be > Remove size ($removeSize)")

        // All should have reasonable base size
        assertTrue(removeSize > 50, "Remove size ($removeSize) should be > 50")
        assertTrue(listSize > 50, "List size ($listSize) should be > 50")

        // Check that sizes are calculated properly (addNodeTool has YAML content so should be bigger)
        assertTrue(addNodeTool.composeNodeYaml.length > 0, "YAML content should exist")
    }

    @Test
    fun testSlidingWindowRespectsToolCount() {
        // Create more than MAX_TOOL_COUNT tools
        val toolArgs =
            (1..25).map { i ->
                ToolArgs.RemoveComposeNodeArgs("node$i").apply {
                    status = ToolExecutionStatus.Success
                    result = "Success"
                }
            }

        val result = repository.applySlidingWindow(toolArgs)
        // Should not exceed MAX_TOOL_COUNT (20)
        assertTrue(result.size <= 20)
        // Should keep the most recent tools
        assertEquals("node25", (result.last() as ToolArgs.RemoveComposeNodeArgs).composeNodeId)
    }

    @Test
    fun testSlidingWindowWithMixedStatuses() {
        val toolArgs =
            listOf(
                ToolArgs.RemoveComposeNodeArgs("node1").apply {
                    status = ToolExecutionStatus.Error
                    result = "Error occurred"
                },
                ToolArgs.RemoveComposeNodeArgs("node2").apply {
                    status = ToolExecutionStatus.Success
                    result = "Success"
                },
                ToolArgs.RemoveComposeNodeArgs("node3").apply {
                    status = ToolExecutionStatus.NotExecuted
                    result = "Not executed"
                },
            )

        val result = repository.applySlidingWindow(toolArgs)
        assertEquals(3, result.size)
        // Order should be preserved
        assertEquals(ToolExecutionStatus.Error, result[0].status)
        assertEquals(ToolExecutionStatus.Success, result[1].status)
        assertEquals(ToolExecutionStatus.NotExecuted, result[2].status)
    }
}
