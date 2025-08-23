package io.composeflow.model.modifier

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Platform-specific helper for creating modifiers that require special handling.
 */
expect object ModifierHelper {
    /**
     * Creates a horizontal alignment modifier for use within a Column.
     */
    fun createHorizontalAlignModifier(alignment: Alignment.Horizontal): Modifier
    
    /**
     * Creates a vertical alignment modifier for use within a Row.
     */
    fun createVerticalAlignModifier(alignment: Alignment.Vertical): Modifier
    
    /**
     * Creates a weight modifier for use within Row/Column.
     */
    fun createWeightModifier(weight: Float, fill: Boolean): Modifier
}