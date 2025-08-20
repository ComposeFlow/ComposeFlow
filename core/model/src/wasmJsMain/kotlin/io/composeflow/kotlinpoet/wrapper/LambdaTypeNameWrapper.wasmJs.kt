package io.composeflow.kotlinpoet.wrapper

/**
 * WASM implementation of LambdaTypeNameWrapper that provides no-op implementations.
 * This is used when code generation is not supported on WASM platform.
 */
actual class LambdaTypeNameWrapper : TypeNameWrapper("LambdaType") {
    actual companion object {
        actual fun get(receiver: TypeNameWrapper?, parameters: List<ParameterSpecWrapper>, returnType: TypeNameWrapper): LambdaTypeNameWrapper = 
            LambdaTypeNameWrapper()
        
        // Convenience overload with no receiver and empty parameters
        actual fun get(returnType: TypeNameWrapper): LambdaTypeNameWrapper = 
            LambdaTypeNameWrapper()
        
        // Convenience overload with no receiver
        actual fun get(parameters: List<ParameterSpecWrapper>, returnType: TypeNameWrapper): LambdaTypeNameWrapper = 
            LambdaTypeNameWrapper()
    }
}