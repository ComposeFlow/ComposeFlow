package io.composeflow.cloud.storage

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@OptIn(kotlin.time.ExperimentalTime::class)
fun Instant.asDateString(timeZone: TimeZone? = null): String {
    val date = toLocalDateTime(timeZone ?: TimeZone.currentSystemDefault()).date
    return "${date.year}-${
        date.month.toString().padStart(2, '0')
    }-${date.day.toString().padStart(2, '0')}"
}
