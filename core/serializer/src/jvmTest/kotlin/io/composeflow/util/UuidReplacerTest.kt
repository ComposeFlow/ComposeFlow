package io.composeflow.util

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class UuidReplacerTest {
    @Test
    fun testExtractIds() {
        val ids = extractIds(testYaml)
        assertEquals(
            listOf(
                "6fad4b91-c1eb-4004-ac24-782b1bfa570e",
                "bc6df753-ab2b-4a6b-8f32-cf1ce1afaa35",
                "d542f13d-3d36-4aa4-a4ff-7caa71bce23b",
                "f842f13d-3d36-4aa4-e4ff-8caa71bce23b",
                "c542f13d-3d36-6aa4-b4ff-7caa71bce23b",
            ),
            ids,
        )
    }

    @Test
    fun testFormatUuid() {
        val testCases =
            listOf(
                "f47ac10b-58cc-4372-a567-0e02b2c3d479", // Valid UUID
                "12345678-1234-1234-1234-123456789abc", // Correct format, but might not be a valid UUID v4
                "123-12-1-1-123", // Too short parts
                "123456789-12345-12345-12345-123456789abcdef", // Too long parts
                "12345678-1234-1234-1234", // Incorrect number of parts
                "invalid input", // Invalid input (can't split)
                "reply-button",
                "retweet-button",
                "12345678-abcd-ef01-2345-67890abcdef0", // Mixed case and numbers
                "F47AC10B-58CC-4372-A567-0E02B2C3D479", // Valid UUID in upper case
                "F47gC10B-58h0-4372-A567-0E02B2C3D479", // Invalid UUID including invalid chars (like 'g', 'h')
            )

        testCases.forEach { input ->
            val formattedUuid = formatToUuid(input)
            assertTrue(isValidUuid(formattedUuid))
        }
    }

    @Test
    fun testReplaceInvalidUuid() {
        val replacedYaml = replaceInvalidUuid(testInputScreenYaml)

        val extractedIds = extractIds(replacedYaml)
        extractedIds.forEach {
            assertTrue(isValidUuid(it))
        }
    }

    private fun isValidUuid(uuidString: String): Boolean =
        try {
            Uuid.parse(uuidString)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
}

private val testYaml = """
id: "6fad4b91-c1eb-4004-ac24-782b1bfa570e"
name: "Blank"
rootNode:
  id: "bc6df753-ab2b-4a6b-8f32-cf1ce1afaa35"
  children:
    - id: "d542f13d-3d36-4aa4-a4ff-7caa71bce23b"
    - someOtherId: "f842f13d-3d36-4aa4-e4ff-8caa71bce23b"
    - anotherVeryLongId: "c542f13d-3d36-6aa4-b4ff-7caa71bce23b"  
"""
