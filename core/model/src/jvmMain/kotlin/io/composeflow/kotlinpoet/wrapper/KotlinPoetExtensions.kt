package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ClassName

/**
 * Bridge extension functions to convert from KotlinPoet types to wrapper types.
 * These are temporary until full migration to wrapper types is complete.
 * 
 * Note: These use internal constructors - this is intentional as these bridge functions
 * are only for migration purposes and will be removed once migration is complete.
 */
@Suppress("INTERNAL_API_USAGE")
actual fun CodeBlock.toWrapper(): CodeBlockWrapper = CodeBlockWrapper(this)

@Suppress("INTERNAL_API_USAGE")
actual fun FunSpec.toWrapper(): FunSpecWrapper = FunSpecWrapper(this)

@Suppress("INTERNAL_API_USAGE")
actual fun PropertySpec.toWrapper(): PropertySpecWrapper = PropertySpecWrapper(this)

@Suppress("INTERNAL_API_USAGE")
actual fun MemberName.toWrapper(): MemberNameWrapper = MemberNameWrapper(this)

@Suppress("INTERNAL_API_USAGE")
actual fun ParameterSpec.toWrapper(): ParameterSpecWrapper = ParameterSpecWrapper(this)

@Suppress("INTERNAL_API_USAGE")
actual fun TypeName.toWrapper(): TypeNameWrapper = when (this) {
    is ClassName -> ClassNameWrapper(this)
    is com.squareup.kotlinpoet.ParameterizedTypeName -> ParameterizedTypeNameWrapper(this)
    else -> object : TypeNameWrapper(this) {}
}

@Suppress("INTERNAL_API_USAGE")
actual fun ClassName.toWrapper(): ClassNameWrapper = ClassNameWrapper(this)