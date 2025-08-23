package io.composeflow.kotlinpoet.wrapper

/**
 * WASM implementation of FunSpecWrapper that provides no-op implementations.
 * This is used when code generation is not supported on WASM platform.
 */
actual class FunSpecWrapper(
    actual val name: String = "",
    actual val returnType: TypeNameWrapper? = null,
    actual val parameters: List<ParameterSpecWrapper> = emptyList(),
    actual val annotations: List<AnnotationSpecWrapper> = emptyList(),
    actual val modifiers: Set<KModifierWrapper> = emptySet(),
    actual val body: CodeBlockWrapper = CodeBlockWrapper(),
) {
    actual companion object {
        actual fun builder(name: String): FunSpecBuilderWrapper = FunSpecBuilderWrapper()

        actual fun constructorBuilder(): FunSpecBuilderWrapper = FunSpecBuilderWrapper()

        actual fun getterBuilder(): FunSpecBuilderWrapper = FunSpecBuilderWrapper()

        actual fun setterBuilder(): FunSpecBuilderWrapper = FunSpecBuilderWrapper()
    }

    actual fun toBuilder(): FunSpecBuilderWrapper = FunSpecBuilderWrapper()

    actual fun toBuilder(name: String): FunSpecBuilderWrapper = FunSpecBuilderWrapper()

    actual override fun toString(): String = "fun $name()"
}

actual class FunSpecBuilderWrapper {
    actual fun addParameter(parameterSpec: ParameterSpecWrapper): FunSpecBuilderWrapper = this

    actual fun addParameter(
        name: String,
        type: TypeNameWrapper,
        vararg modifiers: KModifierWrapper,
    ): FunSpecBuilderWrapper = this

    actual fun addParameters(parameterSpecs: List<ParameterSpecWrapper>): FunSpecBuilderWrapper = this

    actual fun addCode(
        format: String,
        vararg args: Any?,
    ): FunSpecBuilderWrapper = this

    actual fun addCode(codeBlock: CodeBlockWrapper): FunSpecBuilderWrapper = this

    actual fun addStatement(
        format: String,
        vararg args: Any?,
    ): FunSpecBuilderWrapper = this

    actual fun returns(returnType: TypeNameWrapper): FunSpecBuilderWrapper = this

    actual fun addModifiers(vararg modifiers: KModifierWrapper): FunSpecBuilderWrapper = this

    actual fun addAnnotation(annotationSpec: AnnotationSpecWrapper): FunSpecBuilderWrapper = this

    actual fun receiver(receiverType: TypeNameWrapper): FunSpecBuilderWrapper = this

    actual fun build(): FunSpecWrapper = FunSpecWrapper()
}
