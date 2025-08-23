package io.composeflow.kotlinpoet.wrapper

/**
 * Multiplatform wrapper for KotlinPoet's PropertySpec.
 * On JVM, delegates to actual PropertySpec. On WASM, provides no-op implementation.
 */
expect class PropertySpecWrapper {
    companion object {
        fun builder(
            name: String,
            type: TypeNameWrapper,
            vararg modifiers: KModifierWrapper,
        ): PropertySpecBuilderWrapper
    }

    val name: String
    val type: TypeNameWrapper
    val modifiers: Set<KModifierWrapper>
    val annotations: List<AnnotationSpecWrapper>
    val getter: FunSpecWrapper?
    val setter: FunSpecWrapper?

    fun toBuilder(): PropertySpecBuilderWrapper

    fun toBuilder(name: String): PropertySpecBuilderWrapper

    override fun toString(): String
}

expect class PropertySpecBuilderWrapper {
    fun initializer(
        format: String,
        vararg args: Any?,
    ): PropertySpecBuilderWrapper

    fun initializer(codeBlock: CodeBlockWrapper): PropertySpecBuilderWrapper

    fun delegate(
        format: String,
        vararg args: Any?,
    ): PropertySpecBuilderWrapper

    fun delegate(codeBlock: CodeBlockWrapper): PropertySpecBuilderWrapper

    fun getter(getter: FunSpecWrapper): PropertySpecBuilderWrapper

    fun setter(setter: FunSpecWrapper): PropertySpecBuilderWrapper

    fun addModifiers(vararg modifiers: KModifierWrapper): PropertySpecBuilderWrapper

    fun addAnnotation(annotationSpec: AnnotationSpecWrapper): PropertySpecBuilderWrapper

    fun mutable(mutable: Boolean = true): PropertySpecBuilderWrapper

    fun build(): PropertySpecWrapper
}
