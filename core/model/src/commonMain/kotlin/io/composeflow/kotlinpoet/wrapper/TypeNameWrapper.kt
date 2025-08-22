package io.composeflow.kotlinpoet.wrapper

import kotlin.reflect.KClass

/**
 * Multiplatform wrapper for KotlinPoet's TypeName and related classes.
 * On JVM, delegates to actual TypeName. On WASM, provides no-op implementation.
 */
expect abstract class TypeNameWrapper {
    abstract val isNullable: Boolean
    abstract fun copy(nullable: Boolean = this.isNullable): TypeNameWrapper
    override fun toString(): String
}

expect class ClassNameWrapper : TypeNameWrapper {
    companion object {
        fun get(packageName: String, simpleName: String, vararg simpleNames: String): ClassNameWrapper
        fun get(packageName: String, simpleName: String): ClassNameWrapper
        fun bestGuess(classNameString: String): ClassNameWrapper
    }
    
    val packageName: String
    val simpleName: String
    val canonicalName: String
    
    fun nestedClass(name: String): ClassNameWrapper
    fun peerClass(name: String): ClassNameWrapper
}

expect class ParameterizedTypeNameWrapper : TypeNameWrapper {
    companion object {
        fun get(rawType: ClassNameWrapper, vararg typeArguments: TypeNameWrapper): ParameterizedTypeNameWrapper
    }
    
    val rawType: ClassNameWrapper
    val typeArguments: List<TypeNameWrapper>
}

// Extension functions
expect fun KClass<*>.asTypeNameWrapper(): TypeNameWrapper
expect fun KClass<*>.parameterizedBy(vararg typeArguments: KClass<*>): ParameterizedTypeNameWrapper
expect fun TypeNameWrapper.parameterizedBy(vararg typeArguments: TypeNameWrapper): ParameterizedTypeNameWrapper
expect fun ClassNameWrapper.parameterizedBy(vararg typeArguments: TypeNameWrapper): ParameterizedTypeNameWrapper