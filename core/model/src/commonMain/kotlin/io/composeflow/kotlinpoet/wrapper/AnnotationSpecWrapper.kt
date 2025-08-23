package io.composeflow.kotlinpoet.wrapper

/**
 * Multiplatform wrapper for KotlinPoet's AnnotationSpec.
 * On JVM, delegates to actual AnnotationSpec. On WASM, provides no-op implementation.
 */
expect class AnnotationSpecWrapper {
    companion object {
        fun builder(type: ClassNameWrapper): AnnotationSpecBuilderWrapper

        fun get(annotation: Annotation): AnnotationSpecWrapper

        fun get(klass: kotlin.reflect.KClass<*>): AnnotationSpecWrapper
    }

    val type: TypeNameWrapper

    override fun toString(): String
}

expect class AnnotationSpecBuilderWrapper {
    fun addMember(
        format: String,
        vararg args: Any?,
    ): AnnotationSpecBuilderWrapper

    fun addMember(codeBlock: CodeBlockWrapper): AnnotationSpecBuilderWrapper

    fun build(): AnnotationSpecWrapper
}
