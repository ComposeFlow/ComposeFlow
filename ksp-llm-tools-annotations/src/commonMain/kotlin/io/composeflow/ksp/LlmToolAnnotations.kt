package io.composeflow.ksp

/**
 * Annotation to mark methods that should be exposed as tools to LLMs.
 *
 * @param name The name of the tool as it will be exposed to the LLM.
 * @param description A detailed description of what the tool does.
 * @param category Optional category for grouping related tools.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class LlmTool(
    val name: String = "",
    val description: String,
    val category: String = ""
)

/**
 * Annotation to provide additional information about a parameter of an LLM tool.
 *
 * @param description A detailed description of the parameter.
 * @param required Whether the parameter is required or optional.
 * @param defaultValue The default value for the parameter if it's optional.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class LlmParam(
    val description: String,
    val required: Boolean = true,
    val defaultValue: String = ""
)