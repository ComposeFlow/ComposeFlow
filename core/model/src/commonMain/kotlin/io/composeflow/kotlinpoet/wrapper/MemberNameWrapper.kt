package io.composeflow.kotlinpoet.wrapper

/**
 * Multiplatform wrapper for KotlinPoet's MemberName.
 * On JVM, delegates to actual MemberName. On WASM, provides no-op implementation.
 */
expect class MemberNameWrapper {
    companion object {
        fun get(
            packageName: String,
            simpleName: String,
            isExtension: Boolean = false,
        ): MemberNameWrapper
    }

    val packageName: String
    val simpleName: String
    val isExtension: Boolean

    override fun toString(): String
}
