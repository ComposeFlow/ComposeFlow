package io.composeflow.cloud.storage

import kotlin.time.Instant

@OptIn(kotlin.time.ExperimentalTime::class)
actual fun Any.toKotlinxInstant(): Instant = throw UnsupportedOperationException("OffsetDateTime operations not available on WASM")
