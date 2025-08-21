package io.composeflow.kotlinpoet.wrapper

/**
 * Multiplatform wrapper for KotlinPoet's CodeBlock.
 * On JVM, delegates to actual CodeBlock. On WASM, provides no-op implementation.
 */
expect class CodeBlockWrapper {
    companion object {
        fun builder(): CodeBlockBuilderWrapper
        fun of(format: String, vararg args: Any?): CodeBlockWrapper
        fun join(codeBlocks: Iterable<CodeBlockWrapper>, separator: String): CodeBlockWrapper
        fun join(codeBlocks: Iterable<CodeBlockWrapper>, separator: String, prefix: String, suffix: String): CodeBlockWrapper
    }
    
    fun isEmpty(): Boolean
    override fun toString(): String
}

expect class CodeBlockBuilderWrapper {
    fun add(format: String, vararg args: Any?): CodeBlockBuilderWrapper
    fun add(codeBlock: CodeBlockWrapper): CodeBlockBuilderWrapper
    fun addStatement(format: String, vararg args: Any?): CodeBlockBuilderWrapper
    fun build(): CodeBlockWrapper
    fun clear(): CodeBlockBuilderWrapper
    fun indent(): CodeBlockBuilderWrapper
    fun unindent(): CodeBlockBuilderWrapper
    fun isEmpty(): Boolean
}