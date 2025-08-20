package io.composeflow.kotlinpoet.wrapper

/**
 * Bridge extension functions to convert from KotlinPoet types to wrapper types.
 * These are temporary until full migration to wrapper types is complete.
 * 
 * These are expect declarations that will be implemented differently on each platform:
 * - JVM: Real conversion using actual KotlinPoet types
 * - WASM: No-op implementations since KotlinPoet doesn't exist
 */

// For platforms that have access to actual KotlinPoet types
@Suppress("EXPECT_WITHOUT_ACTUAL") // Actual implementations are in platform-specific source sets
expect fun com.squareup.kotlinpoet.CodeBlock.toWrapper(): CodeBlockWrapper

@Suppress("EXPECT_WITHOUT_ACTUAL") 
expect fun com.squareup.kotlinpoet.FunSpec.toWrapper(): FunSpecWrapper

@Suppress("EXPECT_WITHOUT_ACTUAL")
expect fun com.squareup.kotlinpoet.PropertySpec.toWrapper(): PropertySpecWrapper

@Suppress("EXPECT_WITHOUT_ACTUAL")
expect fun com.squareup.kotlinpoet.MemberName.toWrapper(): MemberNameWrapper

@Suppress("EXPECT_WITHOUT_ACTUAL")
expect fun com.squareup.kotlinpoet.ParameterSpec.toWrapper(): ParameterSpecWrapper

@Suppress("EXPECT_WITHOUT_ACTUAL")
expect fun com.squareup.kotlinpoet.TypeName.toWrapper(): TypeNameWrapper

@Suppress("EXPECT_WITHOUT_ACTUAL")
expect fun com.squareup.kotlinpoet.ClassName.toWrapper(): ClassNameWrapper