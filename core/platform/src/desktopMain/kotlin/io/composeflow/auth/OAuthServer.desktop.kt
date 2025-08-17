package io.composeflow.auth

import co.touchlab.kermit.Logger
import io.composeflow.auth.google.TokenResponse
import io.composeflow.logger.logger
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import java.net.ServerSocket

actual class OAuthServer {
    private var server: EmbeddedServer<*, *>? = null
    private val jsonSerializer = Json { ignoreUnknownKeys = true }

    actual fun start(port: Int, onTokenReceived: (TokenResponse) -> String) {
        server = embeddedServer(CIO, port = port) {
            routing {
                get("/callback") {
                    val res = call.request.queryParameters["res"]

                    if (res != null) {
                        try {
                            val token = jsonSerializer.decodeFromString<TokenResponse>(res)
                            val response = onTokenReceived(token)
                            call.respondText(response, ContentType.Text.Html)
                        } catch (e: Exception) {
                            Logger.e("Error processing OAuth callback: ${e.message}", e)
                            call.respondText(
                                "Internal server error: ${e.message}",
                                ContentType.Text.Html,
                                HttpStatusCode.InternalServerError
                            )
                        }
                    } else {
                        call.respondText(
                            "Missing 'res' parameter.",
                            ContentType.Text.Html,
                            HttpStatusCode.BadRequest
                        )
                    }
                }
            }
        }.start(wait = false)
    }

    actual fun stop() {
        server?.stop(1000, 5000)
        server = null
    }

    actual companion object {
        actual fun isPortAvailable(port: Int): Boolean {
            return try {
                ServerSocket(port).use { true }
            } catch (_: IOException) {
                false
            }
        }

        actual fun findAvailablePort(startPort: Int, endPort: Int): Int {
            var port = startPort
            val end = if (startPort > endPort) startPort else endPort

            while (port <= end) {
                if (isPortAvailable(port)) {
                    logger.info("port: $port is available for OAuth callback")
                    return port
                }
                logger.info("port: $port isn't available for OAuth callback. Trying different port")
                port += 1
            }
            error("Couldn't find available ports. Shutdown other applications that use :8080")
        }

        actual fun create(): OAuthServer = OAuthServer()
    }
}
