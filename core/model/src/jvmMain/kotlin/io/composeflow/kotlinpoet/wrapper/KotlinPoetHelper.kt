package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.parameterizedBy

/**
 * Helper functions for calling KotlinPoet APIs without extension method conflicts.
 * This file is isolated to avoid conflicts with our own extension functions.
 */

/**
 * Create a ParameterizedTypeName using KotlinPoet's extension method.
 * This is isolated to avoid conflicts with our own parameterizedBy extensions.
 */
internal fun createParameterizedTypeNameFromKotlinPoet(
    rawType: ClassName, 
    typeArgs: Array<TypeName>
): ParameterizedTypeName {
    // Use KotlinPoet's parameterizedBy extension method
    return rawType.parameterizedBy(*typeArgs)
}