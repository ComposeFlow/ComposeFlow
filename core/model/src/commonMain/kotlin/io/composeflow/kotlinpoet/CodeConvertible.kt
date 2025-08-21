package io.composeflow.kotlinpoet

import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper

/**
 * Indicates the class that implements this interface is able to express itself as [CodeBlockWrapper].
 */
interface CodeConvertible {
    fun asCodeBlock(): CodeBlockWrapper
}
