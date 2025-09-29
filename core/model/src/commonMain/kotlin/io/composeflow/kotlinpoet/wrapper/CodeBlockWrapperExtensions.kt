package io.composeflow.kotlinpoet.wrapper

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder

/**
 * Creates a control flow block with automatic begin/end handling.
 *
 * This is a convenience extension that wraps code in a control flow structure (e.g., if, while, for)
 * and automatically calls [beginControlFlow] and [endControlFlow].
 *
 * @param controlFlow The control flow string (e.g., "if (%L)", "for (%L in %L)")
 * @param args Format arguments to be interpolated into the control flow string
 * @param block Lambda that receives the builder and adds code within the control flow
 * @return This builder for chaining
 *
 * @sample
 * ```kotlin
 * builder.controlFlow("if (value > 0)") {
 *     addStatement("println(\"Positive\")")
 * }
 * // Generates:
 * // if (value > 0) {
 * //     println("Positive")
 * // }
 * ```
 */
fun CodeBlockBuilderWrapper.controlFlow(
    controlFlow: String,
    vararg args: Any?,
    block: (CodeBlockBuilderWrapper) -> CodeBlockBuilderWrapper,
): CodeBlockBuilderWrapper = block(beginControlFlow(controlFlow, *args)).endControlFlow()

/**
 * Wraps code in a coroutine launch block.
 *
 * This extension always wraps the provided code in a `viewModelScope.launch { }` block,
 * regardless of the execution context. For conditional launching based on suspend context,
 * use [launchCoroutineIfNeeded] instead.
 *
 * @param controlFlow The control flow format string. Default: `"%M.%M {"`
 * @param args Format arguments. Default: `[viewModelScope, launch]`
 * @param block Lambda that adds code to execute within the coroutine
 * @return This builder for chaining
 *
 * @see launchCoroutineIfNeeded for conditional launching
 *
 * @sample
 * ```kotlin
 * builder.launchCoroutine {
 *     addStatement("performAsyncOperation()")
 * }
 * // Generates:
 * // viewModelScope.launch {
 * //     performAsyncOperation()
 * // }
 * ```
 */
fun CodeBlockBuilderWrapper.launchCoroutine(
    controlFlow: String = "%M.%M",
    vararg args: Any? = arrayOf(MemberHolder.PreCompose.viewModelScope, MemberHolder.Coroutines.launch),
    block: (CodeBlockBuilderWrapper) -> CodeBlockBuilderWrapper,
): CodeBlockBuilderWrapper = controlFlow(controlFlow, *args, block = block)

/**
 * Conditionally wraps code in a coroutine launch block based on execution context.
 *
 * This extension intelligently determines whether to wrap code in a `viewModelScope.launch { }` block:
 * - If [GenerationContext.withinSuspendedFunction] is `true`: executes the block directly without launching
 * - If [GenerationContext.withinSuspendedFunction] is `false`: wraps the block in a coroutine launch
 *
 * This prevents unnecessary nested coroutine launches when the code is already executing within
 * a suspend function context, improving performance and avoiding redundant coroutine creation.
 *
 * @param context The generation context containing information about the current execution environment
 * @param controlFlow The control flow format string. Default: `"%M.%M {"`
 * @param args Format arguments. Default: `[viewModelScope, launch]`
 * @param block Lambda that adds code to execute (with or without coroutine wrapping)
 * @return This builder for chaining
 *
 * @sample
 * ```kotlin
 * // When NOT in a suspend function:
 * builder.launchCoroutineIfNeeded(context) {
 *     addStatement("settings.putString(\"key\", value)")
 * }
 * // Generates:
 * // viewModelScope.launch {
 * //     settings.putString("key", value)
 * // }
 *
 * // When ALREADY in a suspend function:
 * builder.launchCoroutineIfNeeded(context) {
 *     addStatement("settings.putString(\"key\", value)")
 * }
 * // Generates:
 * // settings.putString("key", value)
 * ```
 */
fun CodeBlockBuilderWrapper.launchCoroutineIfNeeded(
    context: GenerationContext,
    controlFlow: String = "%M.%M",
    vararg args: Any? = arrayOf(MemberHolder.PreCompose.viewModelScope, MemberHolder.Coroutines.launch),
    block: (CodeBlockBuilderWrapper) -> CodeBlockBuilderWrapper,
): CodeBlockBuilderWrapper =
    if (context.withinSuspendedFunction) {
        block(this)
    } else {
        controlFlow(controlFlow, *args, block = block)
    }
