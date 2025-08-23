package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.KModifier

/**
 * JVM implementation of KModifierWrapper that delegates to actual KotlinPoet's KModifier.
 */
actual enum class KModifierWrapper(
    private val actual: KModifier,
) {
    // Visibility modifiers
    PUBLIC(KModifier.PUBLIC),
    PROTECTED(KModifier.PROTECTED),
    PRIVATE(KModifier.PRIVATE),
    INTERNAL(KModifier.INTERNAL),

    // Inheritance modifiers
    FINAL(KModifier.FINAL),
    OPEN(KModifier.OPEN),
    ABSTRACT(KModifier.ABSTRACT),
    SEALED(KModifier.SEALED),

    // Function modifiers
    OVERRIDE(KModifier.OVERRIDE),
    INLINE(KModifier.INLINE),
    INFIX(KModifier.INFIX),
    OPERATOR(KModifier.OPERATOR),
    SUSPEND(KModifier.SUSPEND),

    // Property modifiers
    CONST(KModifier.CONST),
    LATEINIT(KModifier.LATEINIT),

    // Parameter modifiers
    VARARG(KModifier.VARARG),
    NOINLINE(KModifier.NOINLINE),
    CROSSINLINE(KModifier.CROSSINLINE),

    // Platform modifiers
    EXPECT(KModifier.EXPECT),
    ACTUAL(KModifier.ACTUAL),

    // Data class modifiers
    DATA(KModifier.DATA),

    // Other modifiers
    INNER(KModifier.INNER),
    ENUM(KModifier.ENUM),
    ANNOTATION(KModifier.ANNOTATION),
    FUN(KModifier.FUN),
    COMPANION(KModifier.COMPANION),
    EXTERNAL(KModifier.EXTERNAL),
    TAILREC(KModifier.TAILREC),
    ;

    actual override fun toString(): String = actual.toString()

    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): KModifier = actual
}

// Helper functions
fun KModifier.toWrapper(): KModifierWrapper = KModifierWrapper.entries.first { it.toKotlinPoet() == this }

fun Iterable<KModifierWrapper>.toKotlinPoet(): Array<KModifier> = this.map { it.toKotlinPoet() }.toTypedArray()

fun Set<KModifierWrapper>.toKotlinPoet(): Array<KModifier> = this.map { it.toKotlinPoet() }.toTypedArray()

fun Array<KModifierWrapper>.toKotlinPoet(): Array<KModifier> = this.map { it.toKotlinPoet() }.toTypedArray()
