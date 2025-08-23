package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.FunSpec

/**
 * JVM implementation of FunSpecWrapper that delegates to actual KotlinPoet's FunSpec.
 */
actual class FunSpecWrapper internal constructor(
    private val actual: FunSpec,
) {
    actual companion object {
        actual fun builder(name: String): FunSpecBuilderWrapper = FunSpecBuilderWrapper(FunSpec.builder(name))

        actual fun constructorBuilder(): FunSpecBuilderWrapper = FunSpecBuilderWrapper(FunSpec.constructorBuilder())

        actual fun getterBuilder(): FunSpecBuilderWrapper = FunSpecBuilderWrapper(FunSpec.getterBuilder())

        actual fun setterBuilder(): FunSpecBuilderWrapper = FunSpecBuilderWrapper(FunSpec.setterBuilder())
    }

    actual val name: String get() = actual.name
    actual val returnType: TypeNameWrapper?
        get() =
            actual.returnType.let {
                when (it) {
                    is com.squareup.kotlinpoet.ClassName -> ClassNameWrapper(it)
                    is com.squareup.kotlinpoet.ParameterizedTypeName -> ParameterizedTypeNameWrapper(it)
                    else ->
                        object : TypeNameWrapper(it) {
                            override val isNullable: Boolean get() = it.isNullable

                            override fun copy(nullable: Boolean): TypeNameWrapper =
                                it.copy(nullable).let { copied ->
                                    when (copied) {
                                        is com.squareup.kotlinpoet.ClassName -> ClassNameWrapper(copied)
                                        is com.squareup.kotlinpoet.ParameterizedTypeName ->
                                            ParameterizedTypeNameWrapper(
                                                copied,
                                            )

                                        else -> this
                                    }
                                }
                        }
                }
            }
    actual val parameters: List<ParameterSpecWrapper>
        get() =
            actual.parameters.map {
                ParameterSpecWrapper(
                    it,
                )
            }
    actual val annotations: List<AnnotationSpecWrapper>
        get() =
            actual.annotations.map {
                AnnotationSpecWrapper(
                    it,
                )
            }
    actual val modifiers: Set<KModifierWrapper>
        get() =
            actual.modifiers
                .map { kmod ->
                    KModifierWrapper.entries.first { it.toKotlinPoet() == kmod }
                }.toSet()
    actual val body: CodeBlockWrapper get() = CodeBlockWrapper(actual.body)

    actual fun toBuilder(): FunSpecBuilderWrapper = FunSpecBuilderWrapper(actual.toBuilder())

    actual fun toBuilder(name: String): FunSpecBuilderWrapper = FunSpecBuilderWrapper(actual.toBuilder(name))

    actual override fun toString(): String = actual.toString()

    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): FunSpec = actual
}

actual class FunSpecBuilderWrapper internal constructor(
    private val actual: FunSpec.Builder,
) {
    actual fun addParameter(parameterSpec: ParameterSpecWrapper): FunSpecBuilderWrapper =
        FunSpecBuilderWrapper(actual.addParameter(parameterSpec.toKotlinPoet()))

    actual fun addParameter(
        name: String,
        type: TypeNameWrapper,
        vararg modifiers: KModifierWrapper,
    ): FunSpecBuilderWrapper =
        FunSpecBuilderWrapper(
            actual.addParameter(
                name,
                type.toKotlinPoet(),
                *modifiers.map { it.toKotlinPoet() }.toTypedArray(),
            ),
        )

    actual fun addParameters(parameterSpecs: List<ParameterSpecWrapper>): FunSpecBuilderWrapper =
        FunSpecBuilderWrapper(actual.addParameters(parameterSpecs.map { it.toKotlinPoet() }))

    actual fun addCode(
        format: String,
        vararg args: Any?,
    ): FunSpecBuilderWrapper = FunSpecBuilderWrapper(actual.addCode(format, *convertArgsArray(args)))

    actual fun addCode(codeBlock: CodeBlockWrapper): FunSpecBuilderWrapper = FunSpecBuilderWrapper(actual.addCode(codeBlock.toKotlinPoet()))

    actual fun addStatement(
        format: String,
        vararg args: Any?,
    ): FunSpecBuilderWrapper = FunSpecBuilderWrapper(actual.addStatement(format, *convertArgsArray(args)))

    actual fun returns(returnType: TypeNameWrapper): FunSpecBuilderWrapper =
        FunSpecBuilderWrapper(actual.returns(returnType.toKotlinPoet()))

    actual fun addModifiers(vararg modifiers: KModifierWrapper): FunSpecBuilderWrapper =
        FunSpecBuilderWrapper(
            actual.addModifiers(
                *modifiers
                    .map { it.toKotlinPoet() }
                    .toTypedArray(),
            ),
        )

    actual fun addAnnotation(annotationSpec: AnnotationSpecWrapper): FunSpecBuilderWrapper =
        FunSpecBuilderWrapper(actual.addAnnotation(annotationSpec.toKotlinPoet()))

    actual fun receiver(receiverType: TypeNameWrapper): FunSpecBuilderWrapper =
        FunSpecBuilderWrapper(actual.receiver(receiverType.toKotlinPoet()))

    actual fun build(): FunSpecWrapper = FunSpecWrapper(actual.build())
}

// Helper function
// Removed duplicate - defined in KotlinPoetExtensions.jvm.kt
