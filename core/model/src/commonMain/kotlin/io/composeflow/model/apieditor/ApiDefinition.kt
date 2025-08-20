package io.composeflow.model.apieditor

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.FunSpecWrapper
import io.composeflow.kotlinpoet.wrapper.KModifierWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.kotlinpoet.wrapper.parameterizedBy
import io.composeflow.kotlinpoet.wrapper.PropertySpecWrapper
import io.composeflow.kotlinpoet.wrapper.asTypeNameWrapper
import io.composeflow.asClassName
import io.composeflow.asVariableName
import io.composeflow.kotlinpoet.ClassHolder
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE
import io.composeflow.model.project.issue.DestinationContext
import io.composeflow.model.project.issue.Issue
import io.composeflow.model.project.issue.NavigatableDestination
import io.composeflow.model.project.issue.TrackableIssue
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.FallbackMutableStateListSerializer
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

// Temporary bridge function until generateArgumentParameterSpec is converted to return wrappers
private fun com.squareup.kotlinpoet.ParameterSpec.toParameterSpecWrapper(): io.composeflow.kotlinpoet.wrapper.ParameterSpecWrapper {
    // For now, create a simple wrapper - this should be replaced when ApiProperty is fully converted
    return io.composeflow.kotlinpoet.wrapper.ParameterSpecWrapper.builder(this.name, String::class.asTypeNameWrapper()).build()
}

typealias ApiId = String

@Serializable
@SerialName("ApiDefinition")
data class ApiDefinition(
    val id: ApiId = Uuid.random().toString(),
    val name: String = "",
    val method: Method = Method.Get,
    val url: String = "",
    val headers: List<Pair<String, ApiProperty>> = emptyList(),
    val queryParameters: List<Pair<String, ApiProperty>> = emptyList(),
    val exampleJsonResponse: JsonWithJsonPath? = null,
    val authorization: Authorization = Authorization.BasicAuth(),
    @Serializable(FallbackMutableStateListSerializer::class)
    val parameters: MutableList<ApiProperty.StringParameter> = mutableStateListEqualsOverrideOf(),
) {
    fun isValid(): Boolean = exampleJsonResponse != null && generateTrackableIssue().isEmpty()

    fun generateCodeBlock(): CodeBlockWrapper {
        val authCodeBlock = authorization.generateCodeBlock()
        val builder = CodeBlockWrapper.builder()
        builder.add(
            CodeBlockWrapper.of(
                "%M(",
                MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.model.apieditor", "ApiDefinition"),
            ),
        )
        builder.add("""name = "${name.asVariableName().trim()}",""")
        builder.add(
            CodeBlockWrapper.of(
                "method = %M.${method.name},",
                MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.model.apieditor", "Method"),
            ),
        )
        val headersBlockBuilder = CodeBlockWrapper.builder().add(headers.generateCodeBlock())
        authCodeBlock?.let {
            headersBlockBuilder.add(""""${HttpHeaders.Authorization}" to "$it",""")
        }

        builder.add(
            """
          url = "${url.trim()}",
          headers = listOf(${headersBlockBuilder.build()}),
          queryParameters = listOf(${queryParameters.generateCodeBlock()}),
          )
        """,
        )
        return builder.build()
    }

    private fun createApiResultFunName(): String = "create" + name.asClassName().capitalize(Locale.current) + "Result"

    fun callApiFunName() = "onCall${name.asClassName().capitalize(Locale.current)}Api"

    fun apiResultName(): String = name.asVariableName() + "Result"

    private fun updateApiResultFunName(): String = "update" + name.asClassName().capitalize(Locale.current) + "Result"

    fun generateInitApiResultInViewModelFunSpec(): FunSpecWrapper {
        val funSpecBuilder =
            FunSpecWrapper
                .builder("init${name.asClassName().capitalize(Locale.current)}")
                .addCode(
                    CodeBlockWrapper.of(
                        """${updateApiResultFunName()}()
                    """,
                    ),
                )
        return funSpecBuilder.build()
    }

    fun generateUpdateApiResultFunSpec(): FunSpecWrapper {
        val argumentString =
            buildString {
                parameters.forEachIndexed { index, parameter ->
                    append("${parameter.name} = ${parameter.name}")
                    if (index != parameters.lastIndex) {
                        append(",")
                    }
                }
            }
        val funSpecBuilder =
            FunSpecWrapper
                .builder(updateApiResultFunName())
                .addModifiers(KModifierWrapper.PRIVATE)
                .addCode(
                    """
            %M.%M {
                _${apiResultName()}.value = %T.Loading
                _${apiResultName()}.value = try {
                    %T.Success(${createApiResultFunName()}($argumentString))
                } catch (e: Exception) {
                    %T.Error(e.message ?: "Unknown error", e)
                }
            }
            """,
                    MemberHolder.PreCompose.viewModelScope,
                    MemberHolder.Coroutines.launch,
                    ClassHolder.ComposeFlow.DataResult,
                    ClassHolder.ComposeFlow.DataResult,
                    ClassHolder.ComposeFlow.DataResult,
                )
        parameters.forEach {
            funSpecBuilder.addParameter(it.generateArgumentParameterSpec().toParameterSpecWrapper())
        }
        return funSpecBuilder.build()
    }

    fun generateCallApiFunSpec(): FunSpecWrapper {
        val argumentString =
            buildString {
                parameters.forEachIndexed { index, parameter ->
                    append("${parameter.name} = ${parameter.name}")
                    if (index != parameters.lastIndex) {
                        append(",")
                    }
                }
            }
        val funSpecBuilder =
            FunSpecWrapper
                .builder(callApiFunName())
                .addCode("${updateApiResultFunName()}($argumentString)")
        parameters.forEach {
            funSpecBuilder.addParameter(it.generateArgumentParameterSpec().toParameterSpecWrapper())
        }
        return funSpecBuilder.build()
    }

    fun generateApiResultFunSpec(): FunSpecWrapper {
        val funSpecBuilder =
            FunSpecWrapper
                .builder(createApiResultFunName())
                .addModifiers(KModifierWrapper.PRIVATE)
                .addModifiers(KModifierWrapper.SUSPEND)
                .returns(
                    ClassHolder.Kotlinx.Serialization.JsonElement,
                )

        with(funSpecBuilder) {
            addCode("return %M(", MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.api", "callApi"))
            addCode(generateCodeBlock())
            addCode(",")
            exampleJsonResponse?.let {
                addCode("jsonPath = \"${it.jsonPath}\"")
            }
            addCode(")")
        }

        parameters.forEach {
            funSpecBuilder.addParameter(it.generateArgumentParameterSpec().toParameterSpecWrapper())
        }

        return funSpecBuilder.build()
    }

    fun generateApiResultFlowProperties(): List<PropertySpecWrapper> {
        val dataResultType =
            ClassHolder.ComposeFlow.DataResult
                .parameterizedBy(ClassHolder.Kotlinx.Serialization.JsonElement)
        val backingProperty =
            PropertySpecWrapper
                .builder(
                    "_${apiResultName()}",
                    ClassHolder.Coroutines.Flow.MutableStateFlow
                        .parameterizedBy(dataResultType),
                ).addModifiers(KModifierWrapper.PRIVATE)
                .initializer(
                    "%T(%T.Idle)",
                    ClassHolder.Coroutines.Flow.MutableStateFlow,
                    ClassHolder.ComposeFlow.DataResult,
                ).build()
        val property =
            PropertySpecWrapper
                .builder(
                    apiResultName(),
                    ClassHolder.Coroutines.Flow.StateFlow
                        .parameterizedBy(dataResultType),
                ).initializer("_${apiResultName()}")
                .build()
        return listOf(
            backingProperty,
            property,
        )
    }

    private fun apiParameters(): List<ApiProperty> =
        buildList {
            addAll(
                headers.filter { it.second is ApiProperty.StringParameter }.map { it.second } +
                    queryParameters
                        .filter { it.second is ApiProperty.StringParameter }
                        .map { it.second },
            )
        }

    fun generateTrackableIssue(): List<TrackableIssue> =
        buildList {
            apiParameters().forEach {
                if (it !in parameters) {
                    add(
                        TrackableIssue(
                            destinationContext =
                                DestinationContext.ApiEditorScreen(
                                    apiId = id,
                                ),
                            issue =
                                Issue.InvalidApiParameterReference(
                                    destination = NavigatableDestination.ApiEditorScreen,
                                ),
                        ),
                    )
                }
            }
        }

    private fun List<Pair<String, ApiProperty>>.generateCodeBlock(): String {
        val builder = StringBuilder()
        forEach { pair ->
            builder.append("\"${pair.first.trim()}\" to ${pair.second.asCodeBlock()},")
        }
        return builder.toString()
    }

    companion object {
        const val NAME_MUST_NOT_BE_EMPTY = "Name must not be empty"
        const val URL_MUST_NOT_BE_EMPTY = "Url must not be empty"
    }
}
