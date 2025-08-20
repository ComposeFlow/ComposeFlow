package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.*

/**
 * Bridge extensions for converting between wrapper types and actual KotlinPoet types on JVM.
 * These help during the migration period.
 */

// Convert from KotlinPoet to wrapper types
fun CodeBlock.toWrapper(): CodeBlockWrapper = CodeBlockWrapper(this)
fun MemberName.toWrapper(): MemberNameWrapper = MemberNameWrapper(this)
fun PropertySpec.toWrapper(): PropertySpecWrapper = PropertySpecWrapper(this)
fun FunSpec.toWrapper(): FunSpecWrapper = FunSpecWrapper(this)
fun TypeSpec.toWrapper(): TypeSpecWrapper = TypeSpecWrapper(this)

// Convert from wrapper types to KotlinPoet types
fun PropertySpecWrapper.toKotlinPoet(): PropertySpec = this.toKotlinPoet()
fun FunSpecWrapper.toKotlinPoet(): FunSpec = this.toKotlinPoet()
fun CodeBlockWrapper.toKotlinPoet(): CodeBlock = this.toKotlinPoet()
fun MemberNameWrapper.toKotlinPoet(): MemberName = this.toKotlinPoet()
fun TypeSpecWrapper.toKotlinPoet(): TypeSpec = this.toKotlinPoet()

// Helper for collections
fun Collection<PropertySpecWrapper>.toKotlinPoet(): List<PropertySpec> = this.map { it.toKotlinPoet() }
fun Collection<FunSpecWrapper>.toKotlinPoet(): List<FunSpec> = this.map { it.toKotlinPoet() }
fun Collection<CodeBlockWrapper>.toKotlinPoet(): List<CodeBlock> = this.map { it.toKotlinPoet() }

fun Collection<PropertySpec>.toWrapper(): List<PropertySpecWrapper> = this.map { it.toWrapper() }
fun Collection<FunSpec>.toWrapper(): List<FunSpecWrapper> = this.map { it.toWrapper() }
fun Collection<CodeBlock>.toWrapper(): List<CodeBlockWrapper> = this.map { it.toWrapper() }