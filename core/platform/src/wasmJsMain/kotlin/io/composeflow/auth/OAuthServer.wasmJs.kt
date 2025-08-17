package io.composeflow.auth

import io.composeflow.auth.google.TokenResponse

actual class OAuthServer {
    actual fun start(port: Int, onTokenReceived: (TokenResponse) -> String) {
        // WASM doesn't support local servers - OAuth would need to be handled differently
        // For example, using postMessage to communicate with parent window or a different flow
        throw UnsupportedOperationException("Local OAuth server not supported in WASM. Use a different OAuth flow.")
    }

    actual fun stop() {
        // No-op in WASM
    }

    actual companion object {
        actual fun isPortAvailable(port: Int): Boolean {
            // In WASM, port checking is not applicable
            return false
        }

        actual fun findAvailablePort(startPort: Int, endPort: Int): Int {
            // In WASM, we can't create local servers, so this is not applicable
            throw UnsupportedOperationException("Port finding not supported in WASM")
        }

        actual fun create(): OAuthServer = OAuthServer()
    }
}