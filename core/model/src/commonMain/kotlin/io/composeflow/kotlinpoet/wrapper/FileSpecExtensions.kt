package io.composeflow.kotlinpoet.wrapper

/**
 * Extension functions for FileSpecWrapper to provide additional functionality.
 */

/**
 * Suppress redundant visibility modifier warnings.
 * This method suppresses the warning by having redundant public modifiers.
 */
fun FileSpecBuilderWrapper.suppressRedundantVisibilityModifier(): FileSpecBuilderWrapper {
    return suppressWarningTypes("RedundantVisibilityModifier")
}