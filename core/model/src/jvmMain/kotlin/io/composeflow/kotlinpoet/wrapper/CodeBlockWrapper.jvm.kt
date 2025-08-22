package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.CodeBlock

/**
 * JVM implementation of CodeBlockWrapper that delegates to actual KotlinPoet's CodeBlock.
 */
actual class CodeBlockWrapper internal constructor(private val actual: CodeBlock) {
    actual companion object {
        actual fun builder(): CodeBlockBuilderWrapper = CodeBlockBuilderWrapper(CodeBlock.builder())
        actual fun of(format: String, vararg args: Any?): CodeBlockWrapper = CodeBlockWrapper(CodeBlock.of(format, *args))
        actual fun join(codeBlocks: Iterable<CodeBlockWrapper>, separator: String): CodeBlockWrapper = 
            CodeBlockWrapper(CodeBlock.join(codeBlocks.map { it.actual }, separator))
        actual fun join(codeBlocks: Iterable<CodeBlockWrapper>, separator: String, prefix: String, suffix: String): CodeBlockWrapper = 
            CodeBlockWrapper(CodeBlock.join(codeBlocks.map { it.actual }, separator, prefix, suffix))
    }
    
    actual fun isEmpty(): Boolean = actual.isEmpty()
    actual override fun toString(): String = actual.toString()
    
    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): CodeBlock = actual
}

actual class CodeBlockBuilderWrapper internal constructor(private val actual: CodeBlock.Builder) {
    actual fun add(format: String, vararg args: Any?): CodeBlockBuilderWrapper = 
        CodeBlockBuilderWrapper(actual.add(format, *args))
    actual fun add(codeBlock: CodeBlockWrapper): CodeBlockBuilderWrapper = 
        CodeBlockBuilderWrapper(actual.add(codeBlock.toKotlinPoet()))
    actual fun addStatement(format: String, vararg args: Any?): CodeBlockBuilderWrapper = 
        CodeBlockBuilderWrapper(actual.addStatement(format, *args))
    actual fun build(): CodeBlockWrapper = CodeBlockWrapper(actual.build())
    actual fun clear(): CodeBlockBuilderWrapper = CodeBlockBuilderWrapper(actual.clear())
    actual fun indent(): CodeBlockBuilderWrapper = CodeBlockBuilderWrapper(actual.indent())
    actual fun unindent(): CodeBlockBuilderWrapper = CodeBlockBuilderWrapper(actual.unindent())
    actual fun isEmpty(): Boolean = actual.isEmpty()
    actual fun beginControlFlow(controlFlow: String, vararg args: Any?): CodeBlockBuilderWrapper = 
        CodeBlockBuilderWrapper(actual.beginControlFlow(controlFlow, *args))
    actual fun endControlFlow(): CodeBlockBuilderWrapper = 
        CodeBlockBuilderWrapper(actual.endControlFlow())
}