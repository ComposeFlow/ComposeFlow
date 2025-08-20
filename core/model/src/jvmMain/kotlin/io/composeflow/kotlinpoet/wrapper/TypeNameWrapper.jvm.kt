package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass

/**
 * JVM implementation of TypeNameWrapper that delegates to actual KotlinPoet's TypeName.
 */
actual abstract class TypeNameWrapper internal constructor(private val actual: TypeName) {
    actual abstract val isNullable: Boolean
    actual abstract fun copy(nullable: Boolean): TypeNameWrapper
    actual override fun toString(): String = actual.toString()
    
    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): TypeName = actual
}

actual class ClassNameWrapper internal constructor(private val actual: ClassName) : TypeNameWrapper(actual) {
    actual companion object {
        actual fun get(packageName: String, simpleName: String, vararg simpleNames: String): ClassNameWrapper = 
            ClassNameWrapper(ClassName.get(packageName, simpleName, *simpleNames))
        actual fun get(packageName: String, simpleName: String): ClassNameWrapper = 
            ClassNameWrapper(ClassName.get(packageName, simpleName))
        actual fun bestGuess(classNameString: String): ClassNameWrapper = 
            ClassNameWrapper(ClassName.bestGuess(classNameString))
    }
    
    actual override val isNullable: Boolean get() = actual.isNullable
    actual val packageName: String get() = actual.packageName
    actual val simpleName: String get() = actual.simpleName
    actual val canonicalName: String get() = actual.canonicalName
    
    actual fun nestedClass(name: String): ClassNameWrapper = ClassNameWrapper(actual.nestedClass(name))
    actual fun peerClass(name: String): ClassNameWrapper = ClassNameWrapper(actual.peerClass(name))
    actual override fun copy(nullable: Boolean): ClassNameWrapper = ClassNameWrapper(actual.copy(nullable))
    
    // Internal accessor for other wrapper classes
    internal fun toKotlinPoetClassName(): ClassName = actual
}

actual class ParameterizedTypeNameWrapper internal constructor(private val actual: ParameterizedTypeName) : TypeNameWrapper(actual) {
    actual companion object {
        actual fun get(rawType: ClassNameWrapper, vararg typeArguments: TypeNameWrapper): ParameterizedTypeNameWrapper = 
            ParameterizedTypeNameWrapper(ParameterizedTypeName.get(
                rawType.toKotlinPoetClassName(), 
                *typeArguments.map { it.toKotlinPoet() }.toTypedArray()
            ))
    }
    
    actual override val isNullable: Boolean get() = actual.isNullable
    actual val rawType: ClassNameWrapper get() = ClassNameWrapper(actual.rawType)
    actual val typeArguments: List<TypeNameWrapper> get() = actual.typeArguments.map { it.toWrapper() }
    
    actual override fun copy(nullable: Boolean): ParameterizedTypeNameWrapper = 
        ParameterizedTypeNameWrapper(actual.copy(nullable))
}

// Extension functions
actual fun KClass<*>.asTypeNameWrapper(): TypeNameWrapper = this.asTypeName().toWrapper()

actual fun TypeNameWrapper.parameterizedBy(vararg typeArguments: TypeNameWrapper): ParameterizedTypeNameWrapper = 
    ParameterizedTypeNameWrapper.get(this as ClassNameWrapper, *typeArguments)

// Helper function to create TypeNameWrapper from KotlinPoet TypeName
fun TypeName.toWrapper(): TypeNameWrapper = when (this) {
    is ClassName -> ClassNameWrapper(this)
    is ParameterizedTypeName -> ParameterizedTypeNameWrapper(this)
    else -> object : TypeNameWrapper(this) {
        override val isNullable: Boolean get() = this@toWrapper.isNullable
        override fun copy(nullable: Boolean): TypeNameWrapper = this@toWrapper.copy(nullable).toWrapper()
    }
}