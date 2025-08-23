package io.composeflow.testing

actual fun isTest(): Boolean = false // WASM doesn't support reflection-based test detection