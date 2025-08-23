package io.composeflow.model.modifier

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

actual object ModifierHelper {
    actual fun createHorizontalAlignModifier(alignment: Alignment.Horizontal): Modifier {
        // For WASM, we return an empty modifier since we can't create scope-specific modifiers
        // The actual alignment will be handled during code generation
        return Modifier
    }
    
    actual fun createVerticalAlignModifier(alignment: Alignment.Vertical): Modifier {
        // For WASM, we return an empty modifier since we can't create scope-specific modifiers
        // The actual alignment will be handled during code generation
        return Modifier
    }
    
    actual fun createWeightModifier(weight: Float, fill: Boolean): Modifier {
        // For WASM, we return an empty modifier since we can't create scope-specific modifiers
        // The actual weight will be handled during code generation
        return Modifier
    }
}