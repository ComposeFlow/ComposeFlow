package io.composeflow.cloud.storage

import kotlin.time.Instant

actual fun Any.toKotlinxInstant(): Instant {
    throw UnsupportedOperationException("OffsetDateTime operations not available on WASM")
}