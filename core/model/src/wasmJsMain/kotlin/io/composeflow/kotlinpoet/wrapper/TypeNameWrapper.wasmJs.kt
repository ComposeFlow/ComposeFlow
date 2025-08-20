package io.composeflow.kotlinpoet.wrapper

import kotlin.reflect.KClass

/**
 * WASM implementation of TypeNameWrapper that provides no-op implementations.
 * This is used when code generation is not supported on WASM platform.
 */
actual abstract class TypeNameWrapper {
    actual abstract val isNullable: Boolean
    actual abstract fun copy(nullable: Boolean): TypeNameWrapper
    actual override fun toString(): String = ""
}

actual class ClassNameWrapper(
    actual val packageName: String = "",
    actual val simpleName: String = "",
    override val isNullable: Boolean = false
) : TypeNameWrapper() {
    actual companion object {
        actual fun get(packageName: String, simpleName: String, vararg simpleNames: String): ClassNameWrapper = 
            ClassNameWrapper(packageName, simpleName)
        actual fun get(packageName: String, simpleName: String): ClassNameWrapper = 
            ClassNameWrapper(packageName, simpleName)
        actual fun bestGuess(classNameString: String): ClassNameWrapper = 
            ClassNameWrapper("", classNameString)
    }
    
    actual val canonicalName: String get() = "$packageName.$simpleName"
    
    actual fun nestedClass(name: String): ClassNameWrapper = ClassNameWrapper(packageName, "$simpleName.$name")
    actual fun peerClass(name: String): ClassNameWrapper = ClassNameWrapper(packageName, name)
    actual override fun copy(nullable: Boolean): ClassNameWrapper = ClassNameWrapper(packageName, simpleName, nullable)
}

actual class ParameterizedTypeNameWrapper(
    actual val rawType: ClassNameWrapper = ClassNameWrapper(),
    actual val typeArguments: List<TypeNameWrapper> = emptyList(),
    override val isNullable: Boolean = false
) : TypeNameWrapper() {
    actual companion object {
        actual fun get(rawType: ClassNameWrapper, vararg typeArguments: TypeNameWrapper): ParameterizedTypeNameWrapper = 
            ParameterizedTypeNameWrapper(rawType, typeArguments.toList())
    }
    
    actual override fun copy(nullable: Boolean): ParameterizedTypeNameWrapper = 
        ParameterizedTypeNameWrapper(rawType, typeArguments, nullable)
}

// Extension functions
actual fun KClass<*>.asTypeNameWrapper(): TypeNameWrapper = ClassNameWrapper("", this.simpleName ?: "")

actual fun TypeNameWrapper.parameterizedBy(vararg typeArguments: TypeNameWrapper): ParameterizedTypeNameWrapper = 
    ParameterizedTypeNameWrapper.get(this as ClassNameWrapper, *typeArguments)