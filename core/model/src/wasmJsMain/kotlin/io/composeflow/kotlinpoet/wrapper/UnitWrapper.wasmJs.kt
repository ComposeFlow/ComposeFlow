package io.composeflow.kotlinpoet.wrapper

/**
 * WASM implementation of UNIT wrapper that provides no-op implementation.
 * This is used when code generation is not supported on WASM platform.
 */
actual val UNIT: TypeNameWrapper = ClassNameWrapper("kotlin", "Unit")
