package io.composeflow.kotlinpoet.wrapper

/**
 * WASM implementation of MemberNameWrapper that provides no-op implementations.
 * This is used when code generation is not supported on WASM platform.
 */
actual class MemberNameWrapper(
    actual val packageName: String = "",
    actual val simpleName: String = "",
    actual val isExtension: Boolean = false,
) {
    actual companion object {
        actual fun get(
            packageName: String,
            simpleName: String,
            isExtension: Boolean,
        ): MemberNameWrapper = MemberNameWrapper(packageName, simpleName, isExtension)
    }

    actual override fun toString(): String = "$packageName.$simpleName"
}
