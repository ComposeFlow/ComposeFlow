package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass

/**
 * JVM implementation of TypeNameWrapper that delegates to actual KotlinPoet's TypeName.
 */
actual abstract class TypeNameWrapper internal constructor(
    private val actual: TypeName,
) {
    actual abstract val isNullable: Boolean

    actual abstract fun copy(nullable: Boolean): TypeNameWrapper

    actual override fun toString(): String = actual.toString()

    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): TypeName = actual
}

actual class ClassNameWrapper internal constructor(
    private val actual: ClassName,
) : TypeNameWrapper(actual) {
    actual companion object {
        actual fun get(
            packageName: String,
            simpleName: String,
            vararg simpleNames: String,
        ): ClassNameWrapper {
            val simpleNamesList = mutableListOf(simpleName).apply { addAll(simpleNames) }
            val kotlinPoetClassName = com.squareup.kotlinpoet.ClassName(packageName, simpleNamesList)
            return ClassNameWrapper(kotlinPoetClassName)
        }

        actual fun get(
            packageName: String,
            simpleName: String,
        ): ClassNameWrapper {
            val kotlinPoetClassName = com.squareup.kotlinpoet.ClassName(packageName, listOf(simpleName))
            return ClassNameWrapper(kotlinPoetClassName)
        }

        actual fun bestGuess(classNameString: String): ClassNameWrapper = ClassNameWrapper(ClassName.bestGuess(classNameString))
    }

    actual override val isNullable: Boolean get() = actual.isNullable
    actual val packageName: String get() = actual.packageName
    actual val simpleName: String get() = actual.simpleName
    actual val canonicalName: String get() = actual.canonicalName

    actual fun nestedClass(name: String): ClassNameWrapper = ClassNameWrapper(actual.nestedClass(name))

    actual fun peerClass(name: String): ClassNameWrapper = ClassNameWrapper(actual.peerClass(name))

    actual override fun copy(nullable: Boolean): TypeNameWrapper = ClassNameWrapper(actual.copy(nullable) as ClassName)

    // Internal accessor for other wrapper classes
    internal fun toKotlinPoetClassName(): ClassName = actual
}

actual class ParameterizedTypeNameWrapper internal constructor(
    private val actual: ParameterizedTypeName,
) : TypeNameWrapper(actual) {
    actual companion object {
        actual fun get(
            rawType: ClassNameWrapper,
            vararg typeArguments: TypeNameWrapper,
        ): ParameterizedTypeNameWrapper {
            val rawClassName = rawType.toKotlinPoetClassName()
            val typeArgs = typeArguments.map { it.toKotlinPoet() }.toTypedArray()

            // Create the ParameterizedTypeName manually since we have conflicts with extension methods
            // First, let's try the most direct approach using the internal ParameterizedTypeName constructor
            val parameterizedTypeName = createParameterizedTypeNameFromKotlinPoet(rawClassName, typeArgs)
            return ParameterizedTypeNameWrapper(parameterizedTypeName)
        }
    }

    actual override val isNullable: Boolean get() = actual.isNullable
    actual val rawType: ClassNameWrapper get() = ClassNameWrapper(actual.rawType)
    actual val typeArguments: List<TypeNameWrapper> get() =
        actual.typeArguments.map { typeName ->
            when (typeName) {
                is com.squareup.kotlinpoet.ClassName -> ClassNameWrapper(typeName)
                is com.squareup.kotlinpoet.ParameterizedTypeName -> ParameterizedTypeNameWrapper(typeName)
                else ->
                    object : TypeNameWrapper(typeName) {
                        override val isNullable: Boolean get() = typeName.isNullable

                        override fun copy(nullable: Boolean): TypeNameWrapper =
                            typeName.copy(nullable).let { copied ->
                                when (copied) {
                                    is com.squareup.kotlinpoet.ClassName -> ClassNameWrapper(copied)
                                    is com.squareup.kotlinpoet.ParameterizedTypeName -> ParameterizedTypeNameWrapper(copied)
                                    else -> this
                                }
                            }
                    }
            }
        }

    actual override fun copy(nullable: Boolean): TypeNameWrapper =
        ParameterizedTypeNameWrapper(actual.copy(nullable) as ParameterizedTypeName)
}

// Extension functions
actual fun KClass<*>.asTypeNameWrapper(): TypeNameWrapper = this.asTypeName().toWrapper()

actual fun KClass<*>.parameterizedBy(vararg typeArguments: KClass<*>): ParameterizedTypeNameWrapper {
    val baseTypeName = this.asTypeName() as com.squareup.kotlinpoet.ClassName
    val typeArgs = typeArguments.map { it.asTypeName() as com.squareup.kotlinpoet.TypeName }.toTypedArray()

    // Use the same helper method to avoid conflicts
    val parameterizedType = createParameterizedTypeNameFromKotlinPoet(baseTypeName, typeArgs)
    return ParameterizedTypeNameWrapper(parameterizedType)
}

actual fun TypeNameWrapper.parameterizedBy(vararg typeArguments: TypeNameWrapper): ParameterizedTypeNameWrapper =
    ParameterizedTypeNameWrapper.get(this as ClassNameWrapper, *typeArguments)

actual fun ClassNameWrapper.parameterizedBy(vararg typeArguments: TypeNameWrapper): ParameterizedTypeNameWrapper =
    ParameterizedTypeNameWrapper.get(this, *typeArguments)

// Helper function to create TypeNameWrapper from KotlinPoet TypeName
internal fun TypeName.toWrapper(): TypeNameWrapper =
    when (this) {
        is ClassName -> ClassNameWrapper(this)
        is ParameterizedTypeName -> ParameterizedTypeNameWrapper(this)
        else ->
            object : TypeNameWrapper(this) {
                override val isNullable: Boolean get() = this@toWrapper.isNullable

                override fun copy(nullable: Boolean): TypeNameWrapper = this@toWrapper.copy(nullable).toWrapper()
            }
    }

internal fun ClassName.toWrapper(): ClassNameWrapper = ClassNameWrapper(this)
