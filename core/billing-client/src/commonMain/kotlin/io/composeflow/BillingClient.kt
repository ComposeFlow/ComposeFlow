package io.composeflow

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import io.composeflow.auth.AuthRepository
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.di.ServiceLocator
import io.composeflow.http.KtorClientFactory
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class BillingClient(
    private val authRepository: AuthRepository = AuthRepository(),
    private val httpClient: io.ktor.client.HttpClient = KtorClientFactory.create(),
    private val ioDispatcher: CoroutineDispatcher =
        ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER) {
            Dispatchers.Default
        },
    private val endpoint: String = BuildConfig.BILLING_ENDPOINT,
) {
    suspend fun createPricingTableLink(): Result<String, Throwable> =
        runCatching {
            withContext(ioDispatcher) {
                // TODO: Consider a case when the token expires
                val firebaseIdToken =
                    authRepository.firebaseIdToken
                        .take(1)
                        .last()

                val token =
                    when (firebaseIdToken) {
                        is FirebaseIdToken.SignedInToken -> {
                            firebaseIdToken.rawToken
                                ?: throw Exception("Signed-in user has no raw token")
                        }

                        is FirebaseIdToken.Anonymouse -> {
                            Logger.i("Anonymous user attempted to create pricing table link. Feature not available for anonymous users.")
                            return@withContext null
                        }

                        null -> {
                            Logger.i("No authenticated user found for pricing table link creation.")
                            return@withContext null
                        }
                    }

                val response =
                    httpClient.post("$endpoint/createPricingTableLink") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody("{}")
                    }

                val body = response.bodyAsText()
                val jsonElement = Json.parseToJsonElement(body)

                val error = jsonElement.jsonObject["error"]
                if (error != null) {
                    val code = error.jsonObject["code"]
                    val message = error.jsonObject["message"]
                    throw Exception("code: $code, message: $message")
                }

                val url =
                    jsonElement.jsonObject["pricingTableUrl"]?.jsonPrimitive?.content
                return@withContext url
            } ?: throw Exception("No res")
        }
}
