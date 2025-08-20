package io.composeflow.kotlinpoet.wrapper

/**
 * Multiplatform wrapper for KotlinPoet's LambdaTypeName.
 * On JVM, delegates to actual LambdaTypeName. On WASM, provides no-op implementation.
 */
expect class LambdaTypeNameWrapper : TypeNameWrapper {
    companion object {
        fun get(receiver: TypeNameWrapper?, parameters: List<ParameterSpecWrapper>, returnType: TypeNameWrapper): LambdaTypeNameWrapper
        
        // Convenience overload with no receiver and empty parameters (common for simple lambdas)
        fun get(returnType: TypeNameWrapper): LambdaTypeNameWrapper
        
        // Convenience overload with no receiver (common case)
        fun get(parameters: List<ParameterSpecWrapper>, returnType: TypeNameWrapper): LambdaTypeNameWrapper
    }
}