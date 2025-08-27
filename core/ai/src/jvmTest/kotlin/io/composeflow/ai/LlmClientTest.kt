package io.composeflow.ai

import kotlin.test.Test
import kotlin.test.assertEquals

class LlmClientTest {
    @Test
    fun testExtractContent_jsonCodeBlock() {
        val input =
            """
            Some text before
            ```json
            {
                "key": "value",
                "number": 42
            }
            ```
            Some text after
            """.trimIndent()

        val expected =
            """
            {
                "key": "value",
                "number": 42
            }
            """.trimIndent()

        assertEquals(expected, LlmClient.extractContent(input))
    }

    @Test
    fun testExtractContent_jsonCodeBlockWithoutSurroundingTexts() {
        val input =
            """
            ```json
            {
                "key": "value",
                "number": 42
            }
            ```
            """.trimIndent()

        val expected =
            """
            {
                "key": "value",
                "number": 42
            }
            """.trimIndent()

        assertEquals(expected, LlmClient.extractContent(input))
    }

    @Test
    fun testExtractContent_jsonCodeBlockWithoutClosing() {
        val input =
            """
            Some text before
            ```json
            {
                "incomplete": "json",
                "still": "works"
            }
            """.trimIndent()

        val expected =
            """
            {
                "incomplete": "json",
                "still": "works"
            }
            """.trimIndent()

        assertEquals(expected, LlmClient.extractContent(input))
    }

    @Test
    fun testExtractContent_genericCodeBlock() {
        val input =
            """
            Some text before
            ```
            {
                "name": "test",
                "enabled": true
            }
            ```
            Some text after
            """.trimIndent()

        val expected =
            """
            {
                "name": "test",
                "enabled": true
            }
            """.trimIndent()

        assertEquals(expected, LlmClient.extractContent(input))
    }

    @Test
    fun testExtractContent_noCodeBlock() {
        val input =
            """
            {
                "name": "test",
                "enabled": true
            }
            """.trimIndent()

        val expected =
            """
            {
                "name": "test",
                "enabled": true
            }
            """.trimIndent()

        assertEquals(expected, LlmClient.extractContent(input))
    }

    @Test
    fun testExtractContent_jsonCodeBlockWithWhitespace() {
        val input =
            """
            ```json
            {"data": "value"}
              ```
            """.trimIndent()

        val expected = """{"data": "value"}"""

        assertEquals(expected, LlmClient.extractContent(input))
    }

    @Test
    fun testExtractContent_emptyJsonCodeBlock() {
        val input =
            """
            ```json
            ```
            """.trimIndent()

        val expected = ""

        assertEquals(expected, LlmClient.extractContent(input))
    }

    @Test
    fun testExtractContent_multipleCodeBlocks_selectsFirst() {
        val input =
            """
            ```json
            {"first": "block"}
            ```

            ```json
            {"second": "block"}
            ```
            """.trimIndent()

        val expected = """{"first": "block"}"""

        assertEquals(expected, LlmClient.extractContent(input))
    }

    @Test
    fun testExtractContent_tripleBackticksWithNestedBackticks() {
        // Triple backticks code block should properly handle nested backticks
        val input =
            """
            ```json
            {
                "content": "some `backticks` inside"
            }
            ```
            """.trimIndent()

        val expected =
            """
            {
                "content": "some `backticks` inside"
            }
            """.trimIndent()

        assertEquals(expected, LlmClient.extractContent(input))
    }

    @Test
    fun testExtractContent_singleLineJsonCodeBlock() {
        val input = """```json {"inline": "json"}```"""

        val expected = """{"inline": "json"}"""

        assertEquals(expected, LlmClient.extractContent(input))
    }

    @Test
    fun testExtractContent_singleLineSingleBlacktick() {
        val input = """`json {"inline": "json"}`"""

        val expected = """{"inline": "json"}"""

        assertEquals(expected, LlmClient.extractContent(input))
    }
}
