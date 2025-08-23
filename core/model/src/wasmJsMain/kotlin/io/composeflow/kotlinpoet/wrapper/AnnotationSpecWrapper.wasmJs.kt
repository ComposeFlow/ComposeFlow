package io.composeflow.kotlinpoet.wrapper

/**
 * WASM implementation of AnnotationSpecWrapper that provides no-op implementations.
 * This is used when code generation is not supported on WASM platform.
 */
actual class AnnotationSpecWrapper(
    actual val type: TypeNameWrapper = ClassNameWrapper(),
) {
    actual companion object {
        actual fun builder(type: ClassNameWrapper): AnnotationSpecBuilderWrapper = AnnotationSpecBuilderWrapper()

        actual fun get(annotation: Annotation): AnnotationSpecWrapper = AnnotationSpecWrapper()

        actual fun get(klass: kotlin.reflect.KClass<*>): AnnotationSpecWrapper = AnnotationSpecWrapper()
    }

    actual override fun toString(): String = "@$type"
}

actual class AnnotationSpecBuilderWrapper {
    actual fun addMember(
        format: String,
        vararg args: Any?,
    ): AnnotationSpecBuilderWrapper = this

    actual fun addMember(codeBlock: CodeBlockWrapper): AnnotationSpecBuilderWrapper = this

    actual fun build(): AnnotationSpecWrapper = AnnotationSpecWrapper()
}
