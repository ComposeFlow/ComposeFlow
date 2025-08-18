package io.composeflow.model.project.string

import kotlin.test.Test
import kotlin.test.assertEquals

class StringResourceOperationsInferKeyTest {
    @Test
    fun `inferKeyIfEmpty returns existing key when key is not empty`() {
        val resource =
            stringResourceOf(
                key = "existing_key",
                "en-US" to "Hello World",
            )
        assertEquals("existing_key", resource.inferKeyIfEmpty())
    }

    @Test
    fun `inferKeyIfEmpty generates key from English value when key is empty`() {
        val resource =
            stringResourceOf(
                key = "",
                "en-US" to "Hello World",
            )
        assertEquals("hello_world", resource.inferKeyIfEmpty())
    }

    @Test
    fun `inferKeyIfEmpty handles special characters and spaces`() {
        val resource =
            stringResourceOf(
                key = "",
                "en-US" to "Save & Continue!",
            )
        assertEquals("save_continue", resource.inferKeyIfEmpty())
    }

    @Test
    fun `inferKeyIfEmpty handles multiple consecutive spaces and underscores`() {
        val resource =
            stringResourceOf(
                key = "",
                "en-US" to "Hello    World___Test",
            )
        assertEquals("hello_world_test", resource.inferKeyIfEmpty())
    }

    @Test
    fun `inferKeyIfEmpty truncates long strings at word boundary`() {
        val resource =
            stringResourceOf(
                key = "",
                "en-US" to "This is a very long string that exceeds fifty characters and should be truncated",
            )
        assertEquals("this_is_a_very_long_string_that_exceeds_fifty", resource.inferKeyIfEmpty())
    }

    @Test
    fun `inferKeyIfEmpty handles exactly 50 characters without truncation`() {
        val resource =
            stringResourceOf(
                key = "",
                "en-US" to "This is exactly fifty chars when counted properly", // Exactly 50 chars
            )
        assertEquals("this_is_exactly_fifty_chars_when_counted_properly", resource.inferKeyIfEmpty())
    }

    @Test
    fun `inferKeyIfEmpty returns empty key when no English value exists`() {
        val resource =
            stringResourceOf(
                key = "",
                "es-ES" to "Hola Mundo", // Spanish only, no English
            )
        assertEquals("", resource.inferKeyIfEmpty())
    }

    @Test
    fun `inferKeyIfEmpty returns empty key when English value is blank`() {
        val resource =
            stringResourceOf(
                key = "",
                "en-US" to "   ", // Blank spaces only
            )
        assertEquals("", resource.inferKeyIfEmpty())
    }

    @Test
    fun `inferKeyIfEmpty handles numbers in text`() {
        val resource =
            stringResourceOf(
                key = "",
                "en-US" to "Error 404: Page Not Found",
            )
        assertEquals("error_404_page_not_found", resource.inferKeyIfEmpty())
    }

    @Test
    fun `inferKeyIfEmpty handles single word truncation`() {
        val resource =
            stringResourceOf(
                key = "",
                "en-US" to "Supercalifragilisticexpialidocious_is_a_very_long_word_that_needs_truncation",
            )
        assertEquals("supercalifragilisticexpialidocious_is_a_very", resource.inferKeyIfEmpty())
    }

    @Test
    fun `inferKeyIfEmpty handles hyphenated words`() {
        val resource =
            stringResourceOf(
                key = "",
                "en-US" to "Twenty-one dash-separated-words",
            )
        assertEquals("twenty_one_dash_separated_words", resource.inferKeyIfEmpty())
    }
}
