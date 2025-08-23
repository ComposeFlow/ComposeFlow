package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

/**
 * Create a ParameterizedTypeName using KotlinPoet's extension method.
 * This is isolated to avoid conflicts with our own parameterizedBy extensions.
 */
internal fun createParameterizedTypeNameFromKotlinPoet(
    rawType: ClassName,
    typeArgs: Array<TypeName>,
): ParameterizedTypeName {
    // Use KotlinPoet's parameterizedBy extension method
    return rawType.parameterizedBy(*typeArgs)
}
