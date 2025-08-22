package io.composeflow.kotlinpoet.wrapper

/**
 * Multiplatform wrapper for KotlinPoet's FunSpec.
 * On JVM, delegates to actual FunSpec. On WASM, provides no-op implementation.
 */
expect class FunSpecWrapper {
    companion object {
        fun builder(name: String): FunSpecBuilderWrapper
        fun constructorBuilder(): FunSpecBuilderWrapper
        fun getterBuilder(): FunSpecBuilderWrapper
        fun setterBuilder(): FunSpecBuilderWrapper
    }
    
    val name: String
    val returnType: TypeNameWrapper?
    val parameters: List<ParameterSpecWrapper>
    val annotations: List<AnnotationSpecWrapper>
    val modifiers: Set<KModifierWrapper>
    val body: CodeBlockWrapper
    
    fun toBuilder(): FunSpecBuilderWrapper
    fun toBuilder(name: String): FunSpecBuilderWrapper
    
    override fun toString(): String
}

expect class FunSpecBuilderWrapper {
    fun addParameter(parameterSpec: ParameterSpecWrapper): FunSpecBuilderWrapper
    fun addParameter(name: String, type: TypeNameWrapper, vararg modifiers: KModifierWrapper): FunSpecBuilderWrapper
    fun addParameters(parameterSpecs: List<ParameterSpecWrapper>): FunSpecBuilderWrapper
    fun addCode(format: String, vararg args: Any?): FunSpecBuilderWrapper
    fun addCode(codeBlock: CodeBlockWrapper): FunSpecBuilderWrapper
    fun addStatement(format: String, vararg args: Any?): FunSpecBuilderWrapper
    fun returns(returnType: TypeNameWrapper): FunSpecBuilderWrapper
    fun addModifiers(vararg modifiers: KModifierWrapper): FunSpecBuilderWrapper
    fun addAnnotation(annotationSpec: AnnotationSpecWrapper): FunSpecBuilderWrapper
    fun build(): FunSpecWrapper
}