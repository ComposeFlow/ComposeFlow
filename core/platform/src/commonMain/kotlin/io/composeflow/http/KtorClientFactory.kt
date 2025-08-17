package io.composeflow.http

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

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
}