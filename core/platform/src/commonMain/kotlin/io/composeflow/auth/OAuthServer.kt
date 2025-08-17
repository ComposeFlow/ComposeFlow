package io.composeflow.auth

import io.composeflow.auth.google.TokenResponse

/**
 * Platform-specific OAuth callback server for handling OAuth redirects
 */
expect class OAuthServer {
    fun start(port: Int, onTokenReceived: (TokenResponse) -> String): Unit
    fun stop()
    
    companion object {
        fun findAvailablePort(startPort: Int = 8090, endPort: Int = 8110): Int
        fun isPortAvailable(port: Int): Boolean
        fun create(): OAuthServer
    }
}