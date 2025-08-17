package io.composeflow.auth.google

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import io.composeflow.BuildConfig
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.auth.SignInWithIdpResponse
import io.composeflow.firebase.FirebaseApiCaller
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.util.decodeBase64Bytes
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class GoogleOAuth2Client(
    val callbackPort: Int = 8090,
    val httpClient: HttpClient,
    val jsonSerializer: Json = Json { ignoreUnknownKeys = true },
) {
    private val firebaseApiKey: String = BuildConfig.FIREBASE_API_KEY
    private val apiAuthEndpoint: String = "${BuildConfig.AUTH_ENDPOINT}/google"

    fun buildAuthUrl(): String = "$apiAuthEndpoint/login?redirectUrl=http://127.0.0.1:$callbackPort/callback"

    fun buildFirebaseManagementGrantUrl(): String =
        "$apiAuthEndpoint/login?redirectUrl=http://127.0.0.1:$callbackPort/callback&scopeSet=firebase"

    suspend fun refreshToken(googleTokenResponse: TokenResponse): Result<FirebaseIdToken, Throwable> =
        runCatching {
            val refreshToken =
                googleTokenResponse.refresh_token ?: throw IOException("Refresh token is null")
            val tokenResponse =
                FirebaseApiCaller(httpClient).obtainAccessTokenWithRefreshToken(
                    refreshToken,
                ) ?: throw IOException("Failed to obtain access token")

            val requestBody =
                buildJsonObject {
                    put("postBody", "id_token=${tokenResponse.id_token}&providerId=google.com")
                    put("requestUri", "http://127.0.0.1")
                    put("returnIdpCredential", true)
                    put("returnSecureToken", true)
                }.toString()

            val response =
                httpClient.post("https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=$firebaseApiKey") {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                    setBody(requestBody)
                }

            val responseBody = response.bodyAsText()
            responseBody.let {
                val jsonElement = Json.parseToJsonElement(it)
                val error = jsonElement.jsonObject["error"]
                if (error != null) {
                    val code = error.jsonObject["code"]
                    val message = error.jsonObject["message"]
                    throw Exception("code: $code, message: $message")
                }

                val idToken = jsonElement.jsonObject["idToken"]?.jsonPrimitive?.content ?: ""
                val decodeIdToken = decodeIdToken(idToken)
                val firebaseIdToken =
                    Json.decodeFromString<FirebaseIdToken.SignedInToken>(decodeIdToken)
                firebaseIdToken.copy(
                    googleTokenResponse = googleTokenResponse,
                    rawToken = idToken,
                )
            }
        }

    suspend fun signInWithGoogleIdToken(googleTokenResponse: TokenResponse): Result<FirebaseIdToken, Throwable> =
        runCatching {
            val requestBody =
                buildJsonObject {
                    put(
                        "postBody",
                        "id_token=${googleTokenResponse.id_token}&providerId=google.com",
                    )
                    put("requestUri", "http://127.0.0.1")
                    put("returnIdpCredential", true)
                    put("returnSecureToken", true)
                }.toString()

            val response =
                httpClient.post("https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=$firebaseApiKey") {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                    setBody(requestBody)
                }

            val responseBody = response.bodyAsText()
            responseBody.let {
                val withIdpResponse = Json.decodeFromString<SignInWithIdpResponse>(it)
                val decodeIdToken = decodeIdToken(withIdpResponse.idToken)
                val firebaseIdToken =
                    Json.decodeFromString<FirebaseIdToken.SignedInToken>(decodeIdToken)
                firebaseIdToken.copy(
                    googleTokenResponse = googleTokenResponse,
                    rawToken = withIdpResponse.idToken,
                )
            }
        }

    private fun decodeIdToken(idToken: String): String {
        val parts = idToken.split(".")
        // The UID is in the second part of the token, which is a base64 encoded JSON
        val payload = parts[1]
        // Add padding if needed for URL-safe base64 decoding
        val paddedPayload = payload + "=".repeat((4 - payload.length % 4) % 4)
        val decodedBytes = paddedPayload.decodeBase64Bytes()
        return decodedBytes.decodeToString()
    }
}
