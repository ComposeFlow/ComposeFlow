package io.composeflow.model.parameter.wrapper

import io.composeflow.cloud.storage.asDateString
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class InstantTypeWrapperTest {
    @Test
    fun testParseString() {
        val instant = Instant.parse("2024-07-15T00:00:00Z")
        val dateString = instant.asDateString(TimeZone.UTC)
        assertEquals("2024-JULY-15", dateString)
    }
}
