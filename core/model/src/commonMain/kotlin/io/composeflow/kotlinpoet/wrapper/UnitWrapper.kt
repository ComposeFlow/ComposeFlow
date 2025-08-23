package io.composeflow.kotlinpoet.wrapper

/**
 * Multiplatform wrapper for KotlinPoet's UNIT.
 * On JVM, delegates to actual UNIT. On WASM, provides no-op implementation.
 */
expect val UNIT: TypeNameWrapper
