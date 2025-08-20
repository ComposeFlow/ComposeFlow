package io.composeflow.kotlinpoet.wrapper

/**
 * Multiplatform wrapper for KotlinPoet's ParameterSpec.
 * On JVM, delegates to actual ParameterSpec. On WASM, provides no-op implementation.
 */
expect class ParameterSpecWrapper {
    companion object {
        fun builder(name: String, type: TypeNameWrapper, vararg modifiers: KModifierWrapper): ParameterSpecBuilderWrapper
        fun unnamed(type: TypeNameWrapper): ParameterSpecWrapper
    }
    
    val name: String
    val type: TypeNameWrapper
    val modifiers: Set<KModifierWrapper>
    val annotations: List<AnnotationSpecWrapper>
    val defaultValue: CodeBlockWrapper?
    
    override fun toString(): String
}

expect class ParameterSpecBuilderWrapper {
    fun defaultValue(format: String, vararg args: Any?): ParameterSpecBuilderWrapper
    fun defaultValue(codeBlock: CodeBlockWrapper): ParameterSpecBuilderWrapper
    fun addModifiers(vararg modifiers: KModifierWrapper): ParameterSpecBuilderWrapper
    fun addAnnotation(annotationSpec: AnnotationSpecWrapper): ParameterSpecBuilderWrapper
    fun build(): ParameterSpecWrapper
}