package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.LambdaTypeName

/**
 * JVM implementation of LambdaTypeNameWrapper that delegates to actual KotlinPoet's LambdaTypeName.
 */
actual class LambdaTypeNameWrapper internal constructor(
    private val actual: LambdaTypeName,
) : TypeNameWrapper(actual) {
    actual override val isNullable: Boolean get() = actual.isNullable

    actual override fun copy(nullable: Boolean): TypeNameWrapper = LambdaTypeNameWrapper(actual.copy(nullable) as LambdaTypeName)

    actual companion object {
        actual fun get(
            receiver: TypeNameWrapper?,
            parameters: List<ParameterSpecWrapper>,
            returnType: TypeNameWrapper,
        ): LambdaTypeNameWrapper {
            val kotlinPoetParameters = parameters.map { it.toKotlinPoet() }
            return LambdaTypeNameWrapper(
                LambdaTypeName.get(
                    receiver?.toKotlinPoet(),
                    kotlinPoetParameters,
                    returnType.toKotlinPoet(),
                ),
            )
        }

        // Convenience overload with no receiver and empty parameters
        actual fun get(returnType: TypeNameWrapper): LambdaTypeNameWrapper =
            get(receiver = null, parameters = emptyList(), returnType = returnType)

        // Convenience overload with no receiver
        actual fun get(
            parameters: List<ParameterSpecWrapper>,
            returnType: TypeNameWrapper,
        ): LambdaTypeNameWrapper = get(receiver = null, parameters = parameters, returnType = returnType)
    }

    // Internal accessor for other wrapper classes - using a different method name since toKotlinPoet() is final
    internal fun toLambdaTypeName(): LambdaTypeName = actual
}
