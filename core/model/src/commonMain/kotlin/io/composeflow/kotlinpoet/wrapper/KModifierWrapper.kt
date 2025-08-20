package io.composeflow.kotlinpoet.wrapper

/**
 * Multiplatform wrapper for KotlinPoet's KModifier.
 * On JVM, delegates to actual KModifier. On WASM, provides no-op implementation.
 */
expect enum class KModifierWrapper {
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
    TAILREC;
    
    override fun toString(): String
}