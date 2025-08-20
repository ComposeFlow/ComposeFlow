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
actual fun CodeBlock.toWrapper(): CodeBlockWrapper = CodeBlockWrapper(this)
actual fun FunSpec.toWrapper(): FunSpecWrapper = FunSpecWrapper(this)  
actual fun PropertySpec.toWrapper(): PropertySpecWrapper = PropertySpecWrapper(this)
actual fun MemberName.toWrapper(): MemberNameWrapper = MemberNameWrapper(this)
actual fun ParameterSpec.toWrapper(): ParameterSpecWrapper = ParameterSpecWrapper(this)
actual fun TypeName.toWrapper(): TypeNameWrapper = 
    when (this) {
        is ClassName -> ClassNameWrapper(this)
        is com.squareup.kotlinpoet.ParameterizedTypeName -> ParameterizedTypeNameWrapper(this)
        else -> object : TypeNameWrapper(this) {
            override val isNullable: Boolean get() = this@toWrapper.isNullable
            override fun copy(nullable: Boolean): TypeNameWrapper = this@toWrapper.copy(nullable).toWrapper()
        }
    }
actual fun ClassName.toWrapper(): ClassNameWrapper = ClassNameWrapper(this)