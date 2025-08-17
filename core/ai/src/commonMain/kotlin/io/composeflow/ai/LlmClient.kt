package io.composeflow.ai

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrThrow
import com.github.michaelbull.result.runCatching
import io.composeflow.BuildConfig
import io.composeflow.ai.openrouter.OpenRouterResponseWrapper
import io.composeflow.ai.openrouter.tools.ToolArgs
import io.composeflow.http.KtorClientFactory
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

val jsonSerializer =
    Json {
        ignoreUnknownKeys = true
    }

class LlmClient(
    private val client: HttpClient = KtorClientFactory.create(),
) {
    suspend fun invokeCreateProject(
        firebaseIdToken: String,
        promptString: String,
        dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
        retryCount: Int = 0,
    ): Result<CreateProjectAiResponse, Throwable> =
        runCatching {
            if (retryCount >= 3) {
                Logger.e("Failed to generate response. Tried maximum number of attempts.")
                throw IllegalStateException("Failed to generate response. Tried maximum number of attempts.")
            }
            val createProjectUrl = "${BuildConfig.LLM_ENDPOINT}/create_project"
            val escapedPromptString = Json.encodeToString(promptString)
            val jsonBody = """{
            "userRequest": $escapedPromptString
        }"""
            Logger.i("Json body: $jsonBody")


            withContext(dispatcher) {
                val response = client.post(createProjectUrl) {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        append(HttpHeaders.Authorization, "Bearer $firebaseIdToken")
                    }
                    setBody(jsonBody)
                }

                run {
                    if (!response.status.isSuccess()) {
                        val errorBody = response.bodyAsText()
                        Logger.e("LLM API call failed. Code: ${response.status.value}, Body: $errorBody. Request: $jsonBody")
                        throw Exception("Unexpected code ${response.status.value}, $errorBody")
                    } else {
                        val responseBodyString = response.bodyAsText()
                        try {
                            val aiResponseRawResponse =
                                jsonSerializer.decodeFromString<OpenRouterResponseWrapper>(
                                    responseBodyString,
                                )
                            val aiResponse =
                                jsonSerializer.decodeFromString(
                                    CreateProjectAiResponse.serializer(),
                                    extractContent(
                                        aiResponseRawResponse.response.choices[0]
                                            .message.content ?: "",
                                    ),
                                )
                            aiResponse
                        } catch (e: SerializationException) {
                            Logger.e("Error during JSON deserialization: ${e.message}")
                            return@withContext invokeCreateProject(
                                firebaseIdToken = firebaseIdToken,
                                promptString = "$e. Fix the json parse error. Previously generated Json: $responseBodyString",
                                dispatcher = dispatcher,
                                retryCount = retryCount + 1,
                            ).getOrThrow() // Rethrow any exception from the recursive call
                        }
                    }
                }
            }
        }

    suspend fun invokeGenerateScreen(
        firebaseIdToken: String,
        promptString: String,
        dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
        retryCount: Int = 0,
        projectContextString: String? = null,
    ): Result<AiResponse, Throwable> =
        runCatching {
            if (retryCount >= 3) {
                Logger.e("Failed to generate response. Tried maximum number of attempts.")
                throw IllegalStateException("Failed to generate response. Tried maximum number of attempts.")
            }
            val generateUiUrl = "${BuildConfig.LLM_ENDPOINT}/generate_ui"
            val escapedPromptString = Json.encodeToString(promptString)
            val jsonBody =
                projectContextString?.let {
                    """{
            "userRequest": $escapedPromptString,
            "projectContext": ${Json.encodeToString(projectContextString)}
        }"""
                } ?: """{
            "userRequest": $escapedPromptString
        }"""

            Logger.i("Json body: $jsonBody")


            withContext(dispatcher) {
                val response = client.post(generateUiUrl) {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        append(HttpHeaders.Authorization, "Bearer $firebaseIdToken")
                    }
                    setBody(jsonBody)
                }

                run {
                    if (!response.status.isSuccess()) {
                        val errorBody = response.bodyAsText()
                        Logger.e("LLM API call failed. Code: ${response.status.value}, Body: $errorBody. Request: $jsonBody")
                        throw Exception("Unexpected code ${response.status.value}, $errorBody")
                    } else {
                        val responseBodyString = response.bodyAsText()

                        // Log the raw response for debugging
                        Logger.i("Raw response body: $responseBodyString")

                        try {
                            val aiResponseRawResponse =
                                jsonSerializer.decodeFromString<OpenRouterResponseWrapper>(
                                    responseBodyString,
                                )
                            val aiResponse =
                                jsonSerializer.decodeFromString(
                                    AiResponse.serializer(),
                                    extractContent(
                                        aiResponseRawResponse.response.choices[0]
                                            .message.content ?: "",
                                    ),
                                )
                            aiResponse
                        } catch (e: SerializationException) {
                            Logger.e("Error during JSON deserialization: ${e.message}")
                            return@withContext invokeGenerateScreen(
                                firebaseIdToken = firebaseIdToken,
                                promptString = "$e. Fix the json parse error. Previously generated Json: $responseBodyString",
                                dispatcher = dispatcher,
                                retryCount = retryCount + 1,
                            ).getOrThrow() // Rethrow any exception from the recursive call
                        }
                    }
                }
            }
        }

    suspend fun invokeHandleGeneralRequest(
        firebaseIdToken: String,
        promptString: String,
        projectContextString: String,
        previousToolArgs: List<ToolArgs> = emptyList(),
        dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
        retryCount: Int = 0,
    ): Result<OpenRouterResponseWrapper, Throwable> =
        runCatching {
            if (retryCount >= 3) {
                Logger.e("Failed to generate response. Tried maximum number of attempts.")
                throw IllegalStateException("Failed to generate response. Tried maximum number of attempts.")
            }
            val handleRequestUrl = "${BuildConfig.LLM_ENDPOINT}/handle_request"
            val jsonBody = """{
            "userRequest": ${Json.encodeToString(promptString)},
            "projectContext": ${Json.encodeToString(projectContextString)},
            "toolCallResults": ${Json.encodeToString(previousToolArgs)}
        }"""

            Logger.i("invokeHandleGeneralRequest Json body: $jsonBody")


            withContext(dispatcher) {
                val response = client.post(handleRequestUrl) {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        append(HttpHeaders.Authorization, "Bearer $firebaseIdToken")
                    }
                    setBody(jsonBody)
                }

                run {
                    if (!response.status.isSuccess()) {
                        val errorBody = response.bodyAsText()
                        Logger.e("LLM API call failed. Code: ${response.status.value}, Body: $errorBody. Request: $jsonBody")
                        throw Exception("Unexpected code ${response.status.value}, $errorBody")
                    } else {
                        val responseBodyString = response.bodyAsText()

                        // Log the raw response for debugging
                        Logger.i("Raw response body: $responseBodyString")

                        try {
                            val toolResponse =
                                jsonSerializer.decodeFromString<OpenRouterResponseWrapper>(
                                    responseBodyString,
                                )
                            toolResponse
                        } catch (e: SerializationException) {
                            Logger.e("Error during JSON deserialization: ${e.message}")
                            return@withContext invokeHandleGeneralRequest(
                                firebaseIdToken = firebaseIdToken,
                                promptString =
                                    "Original prompt: $promptString. Error: $e. Fix the json parse error. " +
                                        "Previously generated Json: $responseBodyString",
                                projectContextString = projectContextString,
                                dispatcher = dispatcher,
                                retryCount = retryCount + 1,
                            ).getOrThrow() // Rethrow any exception from the recursive call
                        }
                    }
                }
            }
        }

    suspend fun invokeTranslateStrings(
        firebaseIdToken: String,
        stringResources: List<TranslateStringResource>,
        defaultLocale: String,
        targetLocales: List<String>,
        dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
        retryCount: Int = 0,
    ): Result<TranslateStringsResponse, Throwable> =
        runCatching {
            if (retryCount >= 3) {
                Logger.e("Failed to generate response. Tried maximum number of attempts.")
                throw IllegalStateException("Failed to generate response. Tried maximum number of attempts.")
            }
            val translateStringsUrl = "${BuildConfig.LLM_ENDPOINT}/translate_strings"

            val requestData =
                TranslateStringsRequest(
                    stringResources = stringResources,
                    defaultLocale = defaultLocale,
                    targetLocales = targetLocales,
                )

            val jsonBody = Json.encodeToString(requestData)
            Logger.i("Translate strings request body: $jsonBody")


            withContext(dispatcher) {
                val response = client.post(translateStringsUrl) {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        append(HttpHeaders.Authorization, "Bearer $firebaseIdToken")
                    }
                    setBody(jsonBody)
                }

                run {
                    if (!response.status.isSuccess()) {
                        val errorBody = response.bodyAsText()
                        Logger.e("LLM API call failed. Code: ${response.status.value}, Body: $errorBody. Request: $jsonBody")
                        throw Exception("Unexpected code ${response.status.value}, $errorBody")
                    } else {
                        val responseBodyString = response.bodyAsText()

                        // Log the raw response for debugging
                        Logger.i("Raw response body: $responseBodyString")

                        try {
                            val translateRawResponse =
                                jsonSerializer.decodeFromString<OpenRouterResponseWrapper>(
                                    responseBodyString,
                                )
                            val translateResponse =
                                jsonSerializer.decodeFromString(
                                    TranslateStringsResponse.serializer(),
                                    extractContent(
                                        translateRawResponse.response.choices[0]
                                            .message.content ?: "",
                                    ),
                                )
                            translateResponse
                        } catch (e: SerializationException) {
                            Logger.e("Error during JSON deserialization: ${e.message}")
                            return@withContext invokeTranslateStrings(
                                firebaseIdToken = firebaseIdToken,
                                stringResources = stringResources,
                                defaultLocale = defaultLocale,
                                targetLocales = targetLocales,
                                dispatcher = dispatcher,
                                retryCount = retryCount + 1,
                            ).getOrThrow() // Rethrow any exception from the recursive call
                        }
                    }
                }
            }
        }

    /**
     * Extract the content wrapped with
     * ```json
     * ```
     *
     * or
     * `json
     * `
     *
     * or return the original string as it is
     */
    private fun extractContent(input: String): String {
        // First, try to find content wrapped in `json ... `
        val jsonRegex = """`json[\s\n]*(.*?)[\s\n]*`""".toRegex(RegexOption.MULTILINE)
        val jsonMatchResult = jsonRegex.find(input)

        if (jsonMatchResult != null) {
            return jsonMatchResult.groupValues[1].trim()
        }

        // 2. Try to match ```json... (without closing ```)
        val jsonOpenOnlyRegex = """```json[\s\n]*(.*)""".toRegex(RegexOption.MULTILINE)
        val jsonOpenOnlyMatch = jsonOpenOnlyRegex.find(input)
        if (jsonOpenOnlyMatch != null) {
            return jsonOpenOnlyMatch.groupValues[1].trim()
        }

        // 3. Try to match ```...``` (generic)
        val genericCodeRegex = """```[\s\n]*(.*?)[\s\n]*```""".toRegex(RegexOption.MULTILINE)
        val genericCodeMatch = genericCodeRegex.find(input)
        if (genericCodeMatch != null) {
            return genericCodeMatch.groupValues[1].trim()
        }

        // If not wrapped in triple backticks, return the original string
        return input.trim()
    }
}