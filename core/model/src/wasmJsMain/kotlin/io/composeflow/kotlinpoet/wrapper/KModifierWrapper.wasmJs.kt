package io.composeflow.kotlinpoet.wrapper

/**
 * WASM implementation of KModifierWrapper that provides no-op implementations.
 * This is used when code generation is not supported on WASM platform.
 */
actual enum class KModifierWrapper {
    // Visibility modifiers
    PUBLIC,
    PROTECTED,
    PRIVATE,
    INTERNAL,

    // Inheritance modifiers
    FINAL,
    OPEN,
    ABSTRACT,
    SEALED,

    // Function modifiers
    OVERRIDE,
    INLINE,
    INFIX,
    OPERATOR,
    SUSPEND,

    // Property modifiers
    CONST,
    LATEINIT,

    // Parameter modifiers
    VARARG,
    NOINLINE,
    CROSSINLINE,

    // Platform modifiers
    EXPECT,
    ACTUAL,

    // Data class modifiers
    DATA,

    // Other modifiers
    INNER,
    ENUM,
    ANNOTATION,
    FUN,
    COMPANION,
    EXTERNAL,
    TAILREC,
    ;

    actual override fun toString(): String = name.lowercase()
}
