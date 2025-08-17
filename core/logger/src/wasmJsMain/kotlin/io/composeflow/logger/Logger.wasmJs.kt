package io.composeflow.logger

private class NoOpLogger : Logger {
    override fun debug(message: String) {
        // No-op on WASM
    }

    override fun info(message: String) {
        // No-op on WASM
    }

    override fun warn(message: String) {
        // No-op on WASM
    }

    override fun error(message: String) {
        // No-op on WASM
    }

    override fun error(message: String, throwable: Throwable) {
        // No-op on WASM
    }
}

actual val logger: Logger = NoOpLogger()