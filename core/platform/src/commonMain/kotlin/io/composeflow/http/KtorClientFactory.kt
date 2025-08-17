package io.composeflow.http

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes

object KtorClientFactory {

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
        coerceInputValues = true
    }

    fun create(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(jsonConfig)
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }

    fun createWithoutLogging(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(jsonConfig)
            }
        }
    }

    fun createWithTimeout(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(jsonConfig)
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5.minutes.inWholeMilliseconds
                connectTimeoutMillis = 5.minutes.inWholeMilliseconds
                socketTimeoutMillis = 5.minutes.inWholeMilliseconds
            }
        }
    }
}