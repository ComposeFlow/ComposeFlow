package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.PropertySpec

/**
 * JVM implementation of PropertySpecWrapper that delegates to actual KotlinPoet's PropertySpec.
 */
actual class PropertySpecWrapper internal constructor(private val actual: PropertySpec) {
    actual companion object {
        actual fun builder(name: String, type: TypeNameWrapper, vararg modifiers: KModifierWrapper): PropertySpecBuilderWrapper = 
            PropertySpecBuilderWrapper(PropertySpec.builder(name, type.toKotlinPoet(), *modifiers.toKotlinPoet()))
    }
    
    actual val name: String get() = actual.name
    actual val type: TypeNameWrapper get() = actual.type.toWrapper()
    actual val modifiers: Set<KModifierWrapper> get() = actual.modifiers.map { it.toWrapper() }.toSet()
    actual val annotations: List<AnnotationSpecWrapper> get() = actual.annotations.map { it.toWrapper() }
    actual val getter: FunSpecWrapper? get() = actual.getter?.toWrapper()
    actual val setter: FunSpecWrapper? get() = actual.setter?.toWrapper()
    
    actual fun toBuilder(): PropertySpecBuilderWrapper = PropertySpecBuilderWrapper(actual.toBuilder())
    actual fun toBuilder(name: String): PropertySpecBuilderWrapper = PropertySpecBuilderWrapper(actual.toBuilder(name))
    
    actual override fun toString(): String = actual.toString()
    
    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): PropertySpec = actual
}

actual class PropertySpecBuilderWrapper internal constructor(private val actual: PropertySpec.Builder) {
    actual fun initializer(format: String, vararg args: Any?): PropertySpecBuilderWrapper = 
        PropertySpecBuilderWrapper(actual.initializer(format, *args))
    actual fun initializer(codeBlock: CodeBlockWrapper): PropertySpecBuilderWrapper = 
        PropertySpecBuilderWrapper(actual.initializer(codeBlock.toKotlinPoet()))
    actual fun delegate(format: String, vararg args: Any?): PropertySpecBuilderWrapper = 
        PropertySpecBuilderWrapper(actual.delegate(format, *args))
    actual fun delegate(codeBlock: CodeBlockWrapper): PropertySpecBuilderWrapper = 
        PropertySpecBuilderWrapper(actual.delegate(codeBlock.toKotlinPoet()))
    actual fun getter(getter: FunSpecWrapper): PropertySpecBuilderWrapper = 
        PropertySpecBuilderWrapper(actual.getter(getter.toKotlinPoet()))
    actual fun setter(setter: FunSpecWrapper): PropertySpecBuilderWrapper = 
        PropertySpecBuilderWrapper(actual.setter(setter.toKotlinPoet()))
    actual fun addModifiers(vararg modifiers: KModifierWrapper): PropertySpecBuilderWrapper = 
        PropertySpecBuilderWrapper(actual.addModifiers(*modifiers.toKotlinPoet()))
    actual fun addAnnotation(annotationSpec: AnnotationSpecWrapper): PropertySpecBuilderWrapper = 
        PropertySpecBuilderWrapper(actual.addAnnotation(annotationSpec.toKotlinPoet()))
    actual fun mutable(mutable: Boolean): PropertySpecBuilderWrapper = 
        PropertySpecBuilderWrapper(actual.mutable(mutable))
    actual fun build(): PropertySpecWrapper = PropertySpecWrapper(actual.build())
}

// Helper function
fun PropertySpec.toWrapper(): PropertySpecWrapper = PropertySpecWrapper(this)