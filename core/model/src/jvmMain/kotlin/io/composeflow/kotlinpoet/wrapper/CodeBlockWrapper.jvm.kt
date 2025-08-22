package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.CodeBlock

/**
 * JVM implementation of CodeBlockWrapper that delegates to actual KotlinPoet's CodeBlock.
 */
actual class CodeBlockWrapper internal constructor(private val actual: CodeBlock) {
    actual companion object {
        actual fun builder(): CodeBlockBuilderWrapper = CodeBlockBuilderWrapper(CodeBlock.builder())
        actual fun of(format: String, vararg args: Any?): CodeBlockWrapper {
            return CodeBlockWrapper(CodeBlock.of(format, *convertArgsArray(args)))
        }
        actual fun join(codeBlocks: Iterable<CodeBlockWrapper>, separator: String): CodeBlockWrapper {
            val builder = CodeBlock.builder()
            val codeBlockList = codeBlocks.toList()
            codeBlockList.forEachIndexed { index, codeBlock ->
                builder.add(codeBlock.toKotlinPoet())
                if (index < codeBlockList.size - 1) {
                    builder.add(separator)
                }
            }
            return CodeBlockWrapper(builder.build())
        }
        actual fun join(codeBlocks: Iterable<CodeBlockWrapper>, separator: String, prefix: String, suffix: String): CodeBlockWrapper {
            val builder = CodeBlock.builder()
            builder.add(prefix)
            val codeBlockList = codeBlocks.toList()
            codeBlockList.forEachIndexed { index, codeBlock ->
                builder.add(codeBlock.toKotlinPoet())
                if (index < codeBlockList.size - 1) {
                    builder.add(separator)
                }
            }
            builder.add(suffix)
            return CodeBlockWrapper(builder.build())
        }
    }
    
    actual fun isEmpty(): Boolean = actual.isEmpty()
    actual override fun toString(): String = actual.toString()
    
    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): CodeBlock = actual
}

actual class CodeBlockBuilderWrapper internal constructor(private val actual: CodeBlock.Builder) {
    actual fun add(format: String, vararg args: Any?): CodeBlockBuilderWrapper {
        return CodeBlockBuilderWrapper(actual.add(format, *convertArgsArray(args)))
    }
    actual fun add(codeBlock: CodeBlockWrapper): CodeBlockBuilderWrapper = 
        CodeBlockBuilderWrapper(actual.add(codeBlock.toKotlinPoet()))
    actual fun addStatement(format: String, vararg args: Any?): CodeBlockBuilderWrapper {
        return CodeBlockBuilderWrapper(actual.addStatement(format, *convertArgsArray(args)))
    }
    actual fun build(): CodeBlockWrapper = CodeBlockWrapper(actual.build())
    actual fun clear(): CodeBlockBuilderWrapper = CodeBlockBuilderWrapper(actual.clear())
    actual fun indent(): CodeBlockBuilderWrapper = CodeBlockBuilderWrapper(actual.indent())
    actual fun unindent(): CodeBlockBuilderWrapper = CodeBlockBuilderWrapper(actual.unindent())
    actual fun isEmpty(): Boolean = actual.isEmpty()
    actual fun beginControlFlow(controlFlow: String, vararg args: Any?): CodeBlockBuilderWrapper {
        return CodeBlockBuilderWrapper(actual.beginControlFlow(controlFlow, *convertArgsArray(args)))
    }
    actual fun endControlFlow(): CodeBlockBuilderWrapper = 
        CodeBlockBuilderWrapper(actual.endControlFlow())
}