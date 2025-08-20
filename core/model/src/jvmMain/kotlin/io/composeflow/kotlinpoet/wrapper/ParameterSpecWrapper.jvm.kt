package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.ParameterSpec

/**
 * JVM implementation of ParameterSpecWrapper that delegates to actual KotlinPoet's ParameterSpec.
 */
actual class ParameterSpecWrapper internal constructor(private val actual: ParameterSpec) {
    actual companion object {
        actual fun builder(name: String, type: TypeNameWrapper, vararg modifiers: KModifierWrapper): ParameterSpecBuilderWrapper = 
            ParameterSpecBuilderWrapper(ParameterSpec.builder(name, type.toKotlinPoet(), *modifiers.toKotlinPoet()))
        actual fun unnamed(type: TypeNameWrapper): ParameterSpecWrapper = 
            ParameterSpecWrapper(ParameterSpec.unnamed(type.toKotlinPoet()))
    }
    
    actual val name: String get() = actual.name
    actual val type: TypeNameWrapper get() = actual.type.toWrapper()
    actual val modifiers: Set<KModifierWrapper> get() = actual.modifiers.map { it.toWrapper() }.toSet()
    actual val annotations: List<AnnotationSpecWrapper> get() = actual.annotations.map { it.toWrapper() }
    actual val defaultValue: CodeBlockWrapper? get() = actual.defaultValue?.let { CodeBlockWrapper(it) }
    
    actual override fun toString(): String = actual.toString()
    
    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): ParameterSpec = actual
}

actual class ParameterSpecBuilderWrapper internal constructor(private val actual: ParameterSpec.Builder) {
    actual fun defaultValue(format: String, vararg args: Any?): ParameterSpecBuilderWrapper = 
        ParameterSpecBuilderWrapper(actual.defaultValue(format, *args))
    actual fun defaultValue(codeBlock: CodeBlockWrapper): ParameterSpecBuilderWrapper = 
        ParameterSpecBuilderWrapper(actual.defaultValue(codeBlock.toKotlinPoet()))
    actual fun addModifiers(vararg modifiers: KModifierWrapper): ParameterSpecBuilderWrapper = 
        ParameterSpecBuilderWrapper(actual.addModifiers(*modifiers.toKotlinPoet()))
    actual fun addAnnotation(annotationSpec: AnnotationSpecWrapper): ParameterSpecBuilderWrapper = 
        ParameterSpecBuilderWrapper(actual.addAnnotation(annotationSpec.toKotlinPoet()))
    actual fun build(): ParameterSpecWrapper = ParameterSpecWrapper(actual.build())
}

// Helper function
fun ParameterSpec.toWrapper(): ParameterSpecWrapper = ParameterSpecWrapper(this)