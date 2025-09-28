package io.composeflow.ui.inspector.modifier

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.composeflow.editor.validator.ScaleValidator
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.ui.propertyeditor.BasicEditableTextProperty

@Composable
fun ZIndexModifierInspector(
    node: ComposeNode,
    wrapper: ModifierWrapper.ZIndex,
    modifierIndex: Int,
    composeNodeCallbacks: ComposeNodeCallbacks,
    onVisibilityToggleClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModifierInspectorContainer(
        node = node,
        wrapper = wrapper,
        modifierIndex = modifierIndex,
        composeNodeCallbacks = composeNodeCallbacks,
        onVisibilityToggleClicked = onVisibilityToggleClicked,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(start = 36.dp)) {
            fun initialValue(float: Float?) = float?.toInt()?.toString() ?: ""

            BasicEditableTextProperty(
                initialValue = initialValue(wrapper.zIndex),
                label = "zIndex",
                validateInput = ScaleValidator()::validate,
                onValidValueChanged = {
                    composeNodeCallbacks.onModifierUpdatedAt(
                        node,
                        modifierIndex,
                        if (it.isEmpty()) {
                            ModifierWrapper.ZIndex(id = wrapper.id)
                        } else {
                            wrapper.copy(
                                zIndex = it.toFloat(),
                            )
                        },
                    )
                },
            )
        }
    }
}
