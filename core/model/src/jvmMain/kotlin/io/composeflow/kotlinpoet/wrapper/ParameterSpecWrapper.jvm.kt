package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.ParameterSpec

/**
 * JVM implementation of ParameterSpecWrapper that delegates to actual KotlinPoet's ParameterSpec.
 */
actual class ParameterSpecWrapper internal constructor(private val actual: ParameterSpec) {
    actual companion object {
        actual fun builder(name: String, type: TypeNameWrapper, vararg modifiers: KModifierWrapper): ParameterSpecBuilderWrapper = 
            ParameterSpecBuilderWrapper(ParameterSpec.builder(name, type.toKotlinPoet(), *modifiers.map { it.toKotlinPoet() }.toTypedArray()))
        actual fun unnamed(type: TypeNameWrapper): ParameterSpecWrapper = 
            ParameterSpecWrapper(ParameterSpec.unnamed(type.toKotlinPoet()))
    }
    
    actual val name: String get() = actual.name
    actual val type: TypeNameWrapper get() = 
        when (val typeName = actual.type) {
            is com.squareup.kotlinpoet.ClassName -> ClassNameWrapper(typeName)
            is com.squareup.kotlinpoet.ParameterizedTypeName -> ParameterizedTypeNameWrapper(typeName)
            else -> object : TypeNameWrapper(typeName) {
                override val isNullable: Boolean get() = typeName.isNullable
                override fun copy(nullable: Boolean): TypeNameWrapper = typeName.copy(nullable).let {
                    when (it) {
                        is com.squareup.kotlinpoet.ClassName -> ClassNameWrapper(it)
                        is com.squareup.kotlinpoet.ParameterizedTypeName -> ParameterizedTypeNameWrapper(it)
                        else -> this
                    }
                }
            }
        }
    actual val modifiers: Set<KModifierWrapper> get() = actual.modifiers.map { it.toWrapper() }.toSet()
    actual val annotations: List<AnnotationSpecWrapper> get() = actual.annotations.map { AnnotationSpecWrapper(it) }
    actual val defaultValue: CodeBlockWrapper? get() = actual.defaultValue?.let { CodeBlockWrapper(it) }
    
    actual override fun toString(): String = actual.toString()
    
    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): ParameterSpec = actual
}

actual class ParameterSpecBuilderWrapper internal constructor(private val actual: ParameterSpec.Builder) {
    actual fun defaultValue(format: String, vararg args: Any?): ParameterSpecBuilderWrapper {
        return ParameterSpecBuilderWrapper(actual.defaultValue(format, *convertArgsArray(args)))
    }
    actual fun defaultValue(codeBlock: CodeBlockWrapper): ParameterSpecBuilderWrapper = 
        ParameterSpecBuilderWrapper(actual.defaultValue(codeBlock.toKotlinPoet()))
    actual fun addModifiers(vararg modifiers: KModifierWrapper): ParameterSpecBuilderWrapper = 
        ParameterSpecBuilderWrapper(actual.addModifiers(*modifiers.map { it.toKotlinPoet() }.toTypedArray()))
    actual fun addAnnotation(annotationSpec: AnnotationSpecWrapper): ParameterSpecBuilderWrapper = 
        ParameterSpecBuilderWrapper(actual.addAnnotation(annotationSpec.toKotlinPoet()))
    actual fun build(): ParameterSpecWrapper = ParameterSpecWrapper(actual.build())
}

// Helper function - using explicit type to avoid ambiguity
actual fun ParameterSpec.toWrapper(): ParameterSpecWrapper {
    return ParameterSpecWrapper(this)
}