@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.composeflow.serializer

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Instant

class FallbackInstantSerializerInternalTest {
    @Serializable
    data class TestData(
        @Serializable(with = FallbackInstantSerializer::class)
        val timestamp: Instant,
    )

    private val json = Json { encodeDefaults = true }

    @Test
    fun deserialize_validInstant_shouldReturnCorrectInstant() {
        val instant = Instant.fromEpochSeconds(1693571696) // 2023-09-01T12:34:56Z
        val jsonString = """{"timestamp":"$instant"}"""

        val result = json.decodeFromString<TestData>(jsonString)

        assertEquals(instant, result.timestamp)
    }

    @Test
    fun deserialize_invalidInstant_shouldFallbackToDistantPast() {
        val jsonString = """{"timestamp":"invalid-date"}"""

        val result = json.decodeFromString<TestData>(jsonString)

        assertNotNull(result.timestamp)
        assertEquals(Instant.DISTANT_PAST, result.timestamp)
    }

    @Test
    fun serialize_shouldProduceValidJson() {
        val instant = Instant.fromEpochSeconds(1711710000) // 2024-03-29T10:00:00Z
        val data = TestData(timestamp = instant)

        val jsonString = json.encodeToString(data)

        assertEquals("""{"timestamp":"$instant"}""", jsonString)
    }

    @Test
    fun roundTripSerialization_validInstant_shouldMatchOriginal() {
        val original =
            TestData(timestamp = Instant.fromEpochSeconds(1640995200)) // 2022-01-01T00:00:00Z
        val jsonString = json.encodeToString(original)
        val decoded = json.decodeFromString<TestData>(jsonString)

        assertEquals(original, decoded)
    }
}
