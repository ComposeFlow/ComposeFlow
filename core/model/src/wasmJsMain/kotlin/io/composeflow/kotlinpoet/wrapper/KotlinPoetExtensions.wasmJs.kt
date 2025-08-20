package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ClassName

/**
 * WASM implementation of bridge extension functions - no-op implementations.
 */
actual fun CodeBlock.toWrapper(): CodeBlockWrapper = CodeBlockWrapper()
actual fun FunSpec.toWrapper(): FunSpecWrapper = FunSpecWrapper()
actual fun PropertySpec.toWrapper(): PropertySpecWrapper = PropertySpecWrapper()
actual fun MemberName.toWrapper(): MemberNameWrapper = MemberNameWrapper()
actual fun ParameterSpec.toWrapper(): ParameterSpecWrapper = ParameterSpecWrapper()
actual fun TypeName.toWrapper(): TypeNameWrapper = TypeNameWrapper("")
actual fun ClassName.toWrapper(): ClassNameWrapper = ClassNameWrapper.get("", "")