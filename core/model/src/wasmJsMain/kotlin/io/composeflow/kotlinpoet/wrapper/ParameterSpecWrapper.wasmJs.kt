package io.composeflow.kotlinpoet.wrapper

/**
 * WASM implementation of ParameterSpecWrapper that provides no-op implementations.
 * This is used when code generation is not supported on WASM platform.
 */
actual class ParameterSpecWrapper(
    actual val name: String = "",
    actual val type: TypeNameWrapper = ClassNameWrapper(),
    actual val modifiers: Set<KModifierWrapper> = emptySet(),
    actual val annotations: List<AnnotationSpecWrapper> = emptyList(),
    actual val defaultValue: CodeBlockWrapper? = null
) {
    actual companion object {
        actual fun builder(name: String, type: TypeNameWrapper, vararg modifiers: KModifierWrapper): ParameterSpecBuilderWrapper = 
            ParameterSpecBuilderWrapper()
        actual fun unnamed(type: TypeNameWrapper): ParameterSpecWrapper = 
            ParameterSpecWrapper(type = type)
    }
    
    actual override fun toString(): String = "$name: $type"
}

actual class ParameterSpecBuilderWrapper {
    actual fun defaultValue(format: String, vararg args: Any?): ParameterSpecBuilderWrapper = this
    actual fun defaultValue(codeBlock: CodeBlockWrapper): ParameterSpecBuilderWrapper = this
    actual fun addModifiers(vararg modifiers: KModifierWrapper): ParameterSpecBuilderWrapper = this
    actual fun addAnnotation(annotationSpec: AnnotationSpecWrapper): ParameterSpecBuilderWrapper = this
    actual fun build(): ParameterSpecWrapper = ParameterSpecWrapper()
}