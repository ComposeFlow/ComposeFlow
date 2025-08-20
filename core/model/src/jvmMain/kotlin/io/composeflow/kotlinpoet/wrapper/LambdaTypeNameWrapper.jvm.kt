package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.LambdaTypeName

/**
 * JVM implementation of LambdaTypeNameWrapper that delegates to actual KotlinPoet's LambdaTypeName.
 */
actual class LambdaTypeNameWrapper internal constructor(private val actual: LambdaTypeName) : TypeNameWrapper(actual) {
    actual companion object {
        actual fun get(receiver: TypeNameWrapper?, parameters: List<ParameterSpecWrapper>, returnType: TypeNameWrapper): LambdaTypeNameWrapper {
            val kotlinPoetParameters = parameters.map { it.toKotlinPoet() }
            return LambdaTypeNameWrapper(
                LambdaTypeName.get(
                    receiver?.toKotlinPoet(),
                    kotlinPoetParameters,
                    returnType.toKotlinPoet()
                )
            )
        }
        
        // Convenience overload with no receiver and empty parameters
        actual fun get(returnType: TypeNameWrapper): LambdaTypeNameWrapper {
            return get(receiver = null, parameters = emptyList(), returnType = returnType)
        }
        
        // Convenience overload with no receiver
        actual fun get(parameters: List<ParameterSpecWrapper>, returnType: TypeNameWrapper): LambdaTypeNameWrapper {
            return get(receiver = null, parameters = parameters, returnType = returnType)
        }
    }
    
    // Internal accessor for other wrapper classes
    internal override fun toKotlinPoet(): LambdaTypeName = actual
}