package io.composeflow.ai

import kotlinx.serialization.Serializable

@Serializable
data class AiResponse(
    val id: String,
    val message: String, // Shown to the user, explaining the response briefly. But without providing any details.
    val responseDetail: ResponseDetail?,
)

@Serializable
sealed interface ResponseDetail

@Serializable
sealed interface MutationResponseDetail : ResponseDetail

@Serializable
data class CreateNewScreen(
    val id: String,
    val yamlContent: String,
) : MutationResponseDetail

@Serializable
data class CreateProject(
    val id: String,
    val projectName: String,
    val packageName: String,
    val screenPrompts: MutableList<ScreenPrompt>,
)

@Serializable
data class ScreenPrompt(
    val screenName: String,
    val prompt: String,
)

@Serializable
data class CreateProjectAiResponse(
    val id: String,
    val message: String, // Shown to the user, explaining the response briefly. But without providing any details.
    val createProject: CreateProject?,
)

@Serializable
data class TranslateStringsResponse(
    val translations: Map<String, Map<String, String>>, // key -> locale -> translation
)
