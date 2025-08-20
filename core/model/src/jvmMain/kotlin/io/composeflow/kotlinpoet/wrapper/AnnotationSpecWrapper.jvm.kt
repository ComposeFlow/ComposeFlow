package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.AnnotationSpec

/**
 * JVM implementation of AnnotationSpecWrapper that delegates to actual KotlinPoet's AnnotationSpec.
 */
actual class AnnotationSpecWrapper internal constructor(private val actual: AnnotationSpec) {
    actual companion object {
        actual fun builder(type: ClassNameWrapper): AnnotationSpecBuilderWrapper = 
            AnnotationSpecBuilderWrapper(AnnotationSpec.builder(type.toKotlinPoetClassName()))
        actual fun get(annotation: Annotation): AnnotationSpecWrapper = 
            AnnotationSpecWrapper(AnnotationSpec.get(annotation))
        actual fun get(klass: kotlin.reflect.KClass<*>): AnnotationSpecWrapper = 
            AnnotationSpecWrapper(AnnotationSpec.get(klass))
    }
    
    actual val type: TypeNameWrapper get() = actual.type.toWrapper()
    
    actual override fun toString(): String = actual.toString()
    
    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): AnnotationSpec = actual
}

actual class AnnotationSpecBuilderWrapper internal constructor(private val actual: AnnotationSpec.Builder) {
    actual fun addMember(format: String, vararg args: Any?): AnnotationSpecBuilderWrapper = 
        AnnotationSpecBuilderWrapper(actual.addMember(format, *args))
    actual fun addMember(codeBlock: CodeBlockWrapper): AnnotationSpecBuilderWrapper = 
        AnnotationSpecBuilderWrapper(actual.addMember(codeBlock.toKotlinPoet()))
    actual fun build(): AnnotationSpecWrapper = AnnotationSpecWrapper(actual.build())
}

// Helper function
fun AnnotationSpec.toWrapper(): AnnotationSpecWrapper = AnnotationSpecWrapper(this)