package io.composeflow.kotlinpoet.wrapper

/**
 * Suppress redundant visibility modifier warnings.
 * This method suppresses the warning by having redundant public modifiers.
 */
fun FileSpecBuilderWrapper.suppressRedundantVisibilityModifier(): FileSpecBuilderWrapper =
    suppressWarningTypes("RedundantVisibilityModifier")
