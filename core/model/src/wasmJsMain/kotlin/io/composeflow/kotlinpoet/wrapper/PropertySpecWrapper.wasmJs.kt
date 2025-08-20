package io.composeflow.kotlinpoet.wrapper

/**
 * WASM implementation of PropertySpecWrapper that provides no-op implementations.
 * This is used when code generation is not supported on WASM platform.
 */
actual class PropertySpecWrapper(
    actual val name: String = "",
    actual val type: TypeNameWrapper = ClassNameWrapper(),
    actual val modifiers: Set<KModifierWrapper> = emptySet(),
    actual val annotations: List<AnnotationSpecWrapper> = emptyList(),
    actual val getter: FunSpecWrapper? = null,
    actual val setter: FunSpecWrapper? = null
) {
    actual companion object {
        actual fun builder(name: String, type: TypeNameWrapper, vararg modifiers: KModifierWrapper): PropertySpecBuilderWrapper = 
            PropertySpecBuilderWrapper()
    }
    
    actual fun toBuilder(): PropertySpecBuilderWrapper = PropertySpecBuilderWrapper()
    actual fun toBuilder(name: String): PropertySpecBuilderWrapper = PropertySpecBuilderWrapper()
    
    actual override fun toString(): String = "$name: $type"
}

actual class PropertySpecBuilderWrapper {
    actual fun initializer(format: String, vararg args: Any?): PropertySpecBuilderWrapper = this
    actual fun initializer(codeBlock: CodeBlockWrapper): PropertySpecBuilderWrapper = this
    actual fun delegate(format: String, vararg args: Any?): PropertySpecBuilderWrapper = this
    actual fun delegate(codeBlock: CodeBlockWrapper): PropertySpecBuilderWrapper = this
    actual fun getter(getter: FunSpecWrapper): PropertySpecBuilderWrapper = this
    actual fun setter(setter: FunSpecWrapper): PropertySpecBuilderWrapper = this
    actual fun addModifiers(vararg modifiers: KModifierWrapper): PropertySpecBuilderWrapper = this
    actual fun addAnnotation(annotationSpec: AnnotationSpecWrapper): PropertySpecBuilderWrapper = this
    actual fun mutable(mutable: Boolean): PropertySpecBuilderWrapper = this
    actual fun build(): PropertySpecWrapper = PropertySpecWrapper()
}