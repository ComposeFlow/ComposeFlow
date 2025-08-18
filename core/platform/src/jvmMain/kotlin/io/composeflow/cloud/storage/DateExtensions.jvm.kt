package io.composeflow.cloud.storage

import java.time.OffsetDateTime
import kotlin.time.Instant

@OptIn(kotlin.time.ExperimentalTime::class)
actual fun Any.toKotlinxInstant(): Instant {
    require(this is OffsetDateTime) { "Expected OffsetDateTime but got ${this::class.simpleName}" }
    return Instant.fromEpochSeconds(this.toEpochSecond(), this.nano.toLong())
}
