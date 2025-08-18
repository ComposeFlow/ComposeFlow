package io.composeflow.serializer

import com.charleskorn.kaml.YamlPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class YamlLocationAwareExceptionTest {
    @Test
    fun `basic exception creation with message only`() {
        val exception = YamlLocationAwareException("Test error message")

        assertEquals("Test error message", exception.message)
    }

    @Test
    fun `exception with line and column information`() {
        val exception =
            YamlLocationAwareException(
                message = "Property 'type' is required",
                line = 5,
                column = 10,
            )

        assertTrue(exception.message.contains("at line 5, column 10"))
        assertTrue(exception.message.contains("Property 'type' is required"))
    }

    @Test
    fun `exception with line only`() {
        val exception =
            YamlLocationAwareException(
                message = "Invalid value",
                line = 3,
                column = null,
            )

        assertTrue(exception.message.contains("Invalid value"))
        // Should not show column information when it's null
        assertTrue(!exception.message.contains("column"))
    }

    @Test
    fun `exception with YAML content shows context`() {
        val yamlContent =
            """
            id: "test"
            name: "Profile"
            rootNode:
              id: "node1"
              label: "Test Node"
            """.trimIndent()

        val exception =
            YamlLocationAwareException(
                message = "Missing property",
                line = 4,
                column = 3,
                yamlContent = yamlContent,
            )

        val message = exception.message
        assertTrue(message.contains("Missing property"))
        assertTrue(message.contains("at line 4, column 3"))
        assertTrue(message.contains("Context:"))
        assertTrue(message.contains("4:   id: \"node1\"")) // Fixed: includes the indentation
        assertTrue(message.contains("^")) // Pointer to error location
    }

    @Test
    fun `exception context shows surrounding lines`() {
        val yamlContent =
            """
            line1: value1
            line2: value2
            line3: value3
            line4: value4
            line5: value5
            """.trimIndent()

        val exception =
            YamlLocationAwareException(
                message = "Error on line 3",
                line = 3,
                column = 1,
                yamlContent = yamlContent,
            )

        val message = exception.message
        assertTrue(message.contains("2: line2: value2")) // Previous line
        assertTrue(message.contains("3: line3: value3")) // Current line
        assertTrue(message.contains("4: line4: value4")) // Next line
    }

    @Test
    fun `exception at first line shows limited context`() {
        val yamlContent =
            """
            first: line
            second: line
            third: line
            """.trimIndent()

        val exception =
            YamlLocationAwareException(
                message = "Error on first line",
                line = 1,
                column = 1,
                yamlContent = yamlContent,
            )

        val message = exception.message
        assertTrue(message.contains("1: first: line"))
        assertTrue(message.contains("2: second: line"))
        // Should not show line 0 (doesn't exist)
        assertTrue(!message.contains("0:"))
    }

    @Test
    fun `exception at last line shows limited context`() {
        val yamlContent =
            """
            first: line
            second: line
            third: line
            """.trimIndent()

        val exception =
            YamlLocationAwareException(
                message = "Error on last line",
                line = 3,
                column = 1,
                yamlContent = yamlContent,
            )

        val message = exception.message
        assertTrue(message.contains("2: second: line"))
        assertTrue(message.contains("3: third: line"))
        // Should not show line 4 (doesn't exist)
        assertTrue(!message.contains("4:"))
    }

    @Test
    fun `exception with YamlPath integration`() {
        // Create a mock YamlPath (we can't easily construct real ones in tests)
        val mockPath = createMockYamlPath()

        val exception =
            YamlLocationAwareException(
                message = "Path error",
                line = 2,
                column = 5,
                yamlPath = mockPath,
            )

        val message = exception.message
        assertTrue(message.contains("Path error"))
        assertTrue(message.contains("at line 2, column 5"))
        // YamlPath toString should be included
        assertTrue(message.contains("path:"))
    }

    @Test
    fun `exception with cause preserves original exception`() {
        val originalException = RuntimeException("Original error")
        val exception =
            YamlLocationAwareException(
                message = "Wrapped error",
                cause = originalException,
            )

        assertEquals(originalException, exception.cause)
        assertEquals("Wrapped error", exception.message)
    }

    @Test
    fun `exception with null values handles gracefully`() {
        val exception =
            YamlLocationAwareException(
                message = "Test error",
                line = null,
                column = null,
                yamlPath = null,
                yamlContent = null,
            )

        assertEquals("Test error", exception.message)
    }

    @Test
    fun `exception with line out of bounds in content`() {
        val yamlContent =
            """
            line1: value1
            line2: value2
            """.trimIndent()

        val exception =
            YamlLocationAwareException(
                message = "Error beyond content",
                line = 10, // Beyond available lines
                column = 1,
                yamlContent = yamlContent,
            )

        val message = exception.message
        assertTrue(message.contains("Error beyond content"))
        assertTrue(message.contains("at line 10, column 1"))
        // Should not crash, but also shouldn't show invalid context
        assertTrue(!message.contains("Context:"))
    }

    @Test
    fun `exception with zero or negative line numbers`() {
        val yamlContent = "test: content"

        val exception =
            YamlLocationAwareException(
                message = "Invalid line number",
                line = 0,
                column = 1,
                yamlContent = yamlContent,
            )

        val message = exception.message
        assertTrue(message.contains("Invalid line number"))
        // Should not show context for invalid line numbers
        assertTrue(!message.contains("Context:"))
    }

    @Test
    fun `pointer positioning with different column values`() {
        val yamlContent = "  key: value"

        val exception =
            YamlLocationAwareException(
                message = "Column test",
                line = 1,
                column = 5, // Should point to 'k' in 'key'
                yamlContent = yamlContent,
            )

        val message = exception.message
        assertTrue(message.contains("1: $yamlContent"))
        // Check that pointer is positioned correctly
        val lines = message.lines()
        val pointerLine = lines.find { it.contains("^") }
        assertNotNull(pointerLine)
        // Pointer should be at position accounting for "1: " prefix + column
        val expectedPointerPosition = "1: ".length + 5 - 1
        assertEquals('^', pointerLine[expectedPointerPosition])
    }

    // Helper function to create a mock YamlPath for testing
    private fun createMockYamlPath(): YamlPath {
        // Since YamlPath is from kaml library and might be hard to mock,
        // we'll create one through parsing if possible, or use a simple approach
        return try {
            // Try to create a simple path - this might vary based on kaml version
            YamlPath.root
        } catch (e: Exception) {
            // If we can't create a proper YamlPath, the test will still be valuable
            // for the other functionality
            YamlPath.root
        }
    }
}
