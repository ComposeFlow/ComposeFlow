package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.CodeBlock

/**
 * JVM implementation of CodeBlockWrapper that delegates to actual KotlinPoet's CodeBlock.
 */
actual class CodeBlockWrapper internal constructor(private val actual: CodeBlock) {
    actual companion object {
        actual fun builder(): CodeBlockBuilderWrapper = CodeBlockBuilderWrapper(CodeBlock.builder())
        actual fun of(format: String, vararg args: Any?): CodeBlockWrapper {
            val convertedArgs = args.map { arg ->
                when (arg) {
                    is ClassNameWrapper -> arg.toKotlinPoetClassName()
                    is TypeNameWrapper -> arg.toKotlinPoet()
                    is MemberNameWrapper -> arg.toKotlinPoet()
                    is CodeBlockWrapper -> arg.toKotlinPoet()
                    else -> arg
                }
            }.toTypedArray()
            return CodeBlockWrapper(CodeBlock.of(format, *convertedArgs))
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
        val convertedArgs = args.map { arg ->
            when (arg) {
                is ClassNameWrapper -> arg.toKotlinPoetClassName()
                is TypeNameWrapper -> arg.toKotlinPoet()
                is MemberNameWrapper -> arg.toKotlinPoet()
                is CodeBlockWrapper -> arg.toKotlinPoet()
                else -> arg
            }
        }.toTypedArray()
        return CodeBlockBuilderWrapper(actual.add(format, *convertedArgs))
    }
    actual fun add(codeBlock: CodeBlockWrapper): CodeBlockBuilderWrapper = 
        CodeBlockBuilderWrapper(actual.add(codeBlock.toKotlinPoet()))
    actual fun addStatement(format: String, vararg args: Any?): CodeBlockBuilderWrapper {
        val convertedArgs = args.map { arg ->
            when (arg) {
                is ClassNameWrapper -> arg.toKotlinPoetClassName()
                is TypeNameWrapper -> arg.toKotlinPoet()
                is MemberNameWrapper -> arg.toKotlinPoet()
                is CodeBlockWrapper -> arg.toKotlinPoet()
                else -> arg
            }
        }.toTypedArray()
        return CodeBlockBuilderWrapper(actual.addStatement(format, *convertedArgs))
    }
    actual fun build(): CodeBlockWrapper = CodeBlockWrapper(actual.build())
    actual fun clear(): CodeBlockBuilderWrapper = CodeBlockBuilderWrapper(actual.clear())
    actual fun indent(): CodeBlockBuilderWrapper = CodeBlockBuilderWrapper(actual.indent())
    actual fun unindent(): CodeBlockBuilderWrapper = CodeBlockBuilderWrapper(actual.unindent())
    actual fun isEmpty(): Boolean = actual.isEmpty()
    actual fun beginControlFlow(controlFlow: String, vararg args: Any?): CodeBlockBuilderWrapper {
        val convertedArgs = args.map { arg ->
            when (arg) {
                is ClassNameWrapper -> arg.toKotlinPoetClassName()
                is TypeNameWrapper -> arg.toKotlinPoet()
                is MemberNameWrapper -> arg.toKotlinPoet()
                is CodeBlockWrapper -> arg.toKotlinPoet()
                else -> arg
            }
        }.toTypedArray()
        return CodeBlockBuilderWrapper(actual.beginControlFlow(controlFlow, *convertedArgs))
    }
    actual fun endControlFlow(): CodeBlockBuilderWrapper = 
        CodeBlockBuilderWrapper(actual.endControlFlow())
}