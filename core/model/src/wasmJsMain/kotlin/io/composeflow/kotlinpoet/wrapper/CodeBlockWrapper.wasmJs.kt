package io.composeflow.kotlinpoet.wrapper

/**
 * WASM implementation of CodeBlockWrapper that provides no-op implementations.
 * This is used when code generation is not supported on WASM platform.
 */
actual class CodeBlockWrapper {
    actual companion object {
        actual fun builder(): CodeBlockBuilderWrapper = CodeBlockBuilderWrapper()
        actual fun of(format: String, vararg args: Any?): CodeBlockWrapper = CodeBlockWrapper()
        actual fun join(codeBlocks: Iterable<CodeBlockWrapper>, separator: String): CodeBlockWrapper = CodeBlockWrapper()
        actual fun join(codeBlocks: Iterable<CodeBlockWrapper>, separator: String, prefix: String, suffix: String): CodeBlockWrapper = CodeBlockWrapper()
    }
    
    actual fun isEmpty(): Boolean = true
    actual override fun toString(): String = ""
}

actual class CodeBlockBuilderWrapper {
    actual fun add(format: String, vararg args: Any?): CodeBlockBuilderWrapper = this
    actual fun add(codeBlock: CodeBlockWrapper): CodeBlockBuilderWrapper = this
    actual fun addStatement(format: String, vararg args: Any?): CodeBlockBuilderWrapper = this
    actual fun build(): CodeBlockWrapper = CodeBlockWrapper()
    actual fun clear(): CodeBlockBuilderWrapper = this
    actual fun indent(): CodeBlockBuilderWrapper = this
    actual fun unindent(): CodeBlockBuilderWrapper = this
    actual fun isEmpty(): Boolean = true
    actual fun beginControlFlow(controlFlow: String, vararg args: Any?): CodeBlockBuilderWrapper = this
    actual fun endControlFlow(): CodeBlockBuilderWrapper = this
}