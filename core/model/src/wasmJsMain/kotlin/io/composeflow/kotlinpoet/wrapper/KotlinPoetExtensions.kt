@file:Suppress("PackageDirectoryMismatch")

package io.composeflow.kotlinpoet.wrapper

// For WASM, we need to provide stub implementations since KotlinPoet doesn't exist
// We'll create a separate package namespace to avoid conflicts
package com.squareup.kotlinpoet {
    class CodeBlock
    class FunSpec
    class PropertySpec
    class MemberName
    class ParameterSpec
    open class TypeName
    class ClassName : TypeName()
}

// No-op implementations for WASM
actual fun com.squareup.kotlinpoet.CodeBlock.toWrapper(): CodeBlockWrapper = CodeBlockWrapper.of("")
actual fun com.squareup.kotlinpoet.FunSpec.toWrapper(): FunSpecWrapper = FunSpecWrapper.builder("").build()
actual fun com.squareup.kotlinpoet.PropertySpec.toWrapper(): PropertySpecWrapper = PropertySpecWrapper.builder("", String::class.asTypeNameWrapper()).build()
actual fun com.squareup.kotlinpoet.MemberName.toWrapper(): MemberNameWrapper = MemberNameWrapper.get("", "")
actual fun com.squareup.kotlinpoet.ParameterSpec.toWrapper(): ParameterSpecWrapper = ParameterSpecWrapper.builder("", String::class.asTypeNameWrapper()).build()
actual fun com.squareup.kotlinpoet.TypeName.toWrapper(): TypeNameWrapper = ClassNameWrapper.get("", "")
actual fun com.squareup.kotlinpoet.ClassName.toWrapper(): ClassNameWrapper = ClassNameWrapper.get("", "")