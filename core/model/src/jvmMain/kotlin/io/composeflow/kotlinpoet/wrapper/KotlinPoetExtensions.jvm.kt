package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ClassName

/**
 * JVM implementation of bridge extension functions.
 */

// Actual implementations for expect declarations
actual fun CodeBlock.toWrapper(): CodeBlockWrapper = CodeBlockWrapper(this)
actual fun FunSpec.toWrapper(): FunSpecWrapper = FunSpecWrapper(this)

// Other toWrapper() functions are implemented in their respective wrapper files:
// - PropertySpec.toWrapper() in PropertySpecWrapper.jvm.kt
// - MemberName.toWrapper() in MemberNameWrapper.jvm.kt  
// - ParameterSpec.toWrapper() in ParameterSpecWrapper.jvm.kt
// - TypeName.toWrapper() in TypeNameWrapper.jvm.kt
// - ClassName.toWrapper() in TypeNameWrapper.jvm.kt