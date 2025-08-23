package io.composeflow.model.modifier

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlin.reflect.full.primaryConstructor

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
actual object ModifierHelper {
    actual fun createHorizontalAlignModifier(alignment: Alignment.Horizontal): Modifier {
        // Creating instance using reflection because modifiers that require LayoutScopeMarker
        // such as RowScope, ColumnScope can't be instantiated outside of those scopes.
        val constructor =
            androidx.compose.foundation.layout.HorizontalAlignElement::class.primaryConstructor
        return constructor!!.call(alignment) as Modifier
    }
    
    actual fun createVerticalAlignModifier(alignment: Alignment.Vertical): Modifier {
        // Creating instance using reflection because modifiers that require LayoutScopeMarker
        // such as RowScope, ColumnScope can't be instantiated outside of those scopes.
        val constructor =
            androidx.compose.foundation.layout.VerticalAlignElement::class.primaryConstructor
        return constructor!!.call(alignment) as Modifier
    }
    
    actual fun createWeightModifier(weight: Float, fill: Boolean): Modifier {
        // Creating instance using reflection because modifiers that require LayoutScopeMarker
        // such as RowScope, ColumnScope can't be instantiated outside of those scopes.
        val constructor =
            androidx.compose.foundation.layout.LayoutWeightElement::class.primaryConstructor
        return constructor!!.call(weight, fill) as Modifier
    }
}