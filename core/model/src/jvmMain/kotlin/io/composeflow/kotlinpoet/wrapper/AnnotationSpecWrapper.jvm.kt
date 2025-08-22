package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.AnnotationSpec
import kotlin.reflect.KClass

/**
 * JVM implementation of AnnotationSpecWrapper that delegates to actual KotlinPoet's AnnotationSpec.
 */
actual class AnnotationSpecWrapper internal constructor(private val actual: AnnotationSpec) {
    actual companion object {
        actual fun builder(type: ClassNameWrapper): AnnotationSpecBuilderWrapper =
            AnnotationSpecBuilderWrapper(AnnotationSpec.builder(type.toKotlinPoetClassName()))

        actual fun get(annotation: Annotation): AnnotationSpecWrapper =
            AnnotationSpecWrapper(AnnotationSpec.get(annotation))

        actual fun get(klass: KClass<*>): AnnotationSpecWrapper =
            @Suppress("UNCHECKED_CAST")
            AnnotationSpecWrapper(AnnotationSpec.builder(klass as KClass<out Annotation>).build())
    }

    actual val type: TypeNameWrapper
        get() =
            when (val typeName = actual.typeName) {
                is com.squareup.kotlinpoet.ClassName -> ClassNameWrapper(typeName)
                is com.squareup.kotlinpoet.ParameterizedTypeName -> ParameterizedTypeNameWrapper(
                    typeName
                )

                else -> object : TypeNameWrapper(typeName) {
                    override val isNullable: Boolean get() = typeName.isNullable
                    override fun copy(nullable: Boolean): TypeNameWrapper =
                        typeName.copy(nullable).let {
                            when (it) {
                                is com.squareup.kotlinpoet.ClassName -> ClassNameWrapper(it)
                                is com.squareup.kotlinpoet.ParameterizedTypeName -> ParameterizedTypeNameWrapper(
                                    it
                                )

                                else -> this
                            }
                        }
                }
            }

    actual override fun toString(): String = actual.toString()

    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): AnnotationSpec = actual
}

actual class AnnotationSpecBuilderWrapper internal constructor(private val actual: AnnotationSpec.Builder) {
    actual fun addMember(format: String, vararg args: Any?): AnnotationSpecBuilderWrapper =
        AnnotationSpecBuilderWrapper(actual.addMember(format, *args.filterNotNull().toTypedArray()))

    actual fun addMember(codeBlock: CodeBlockWrapper): AnnotationSpecBuilderWrapper =
        AnnotationSpecBuilderWrapper(actual.addMember(codeBlock.toKotlinPoet()))

    actual fun build(): AnnotationSpecWrapper = AnnotationSpecWrapper(actual.build())
}

// Helper function
fun AnnotationSpec.toWrapper(): AnnotationSpecWrapper = AnnotationSpecWrapper(this)
