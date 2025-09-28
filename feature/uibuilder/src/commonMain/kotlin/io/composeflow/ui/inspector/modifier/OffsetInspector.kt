package io.composeflow.ui.inspector.modifier

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.composeflow.editor.validator.OffsetValidator
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.ui.propertyeditor.BasicEditableTextProperty

@Composable
fun OffsetModifierInspector(
    node: ComposeNode,
    wrapper: ModifierWrapper.Offset,
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
            fun initialValue(dp: Dp) = dp.value.toInt().toString()

            Row {
                BasicEditableTextProperty(
                    initialValue = initialValue(wrapper.x),
                    label = "X",
                    validateInput = OffsetValidator()::validate,
                    onValidValueChanged = {
                        composeNodeCallbacks.onModifierUpdatedAt(
                            node,
                            modifierIndex,
                            wrapper.copy(
                                x = if (it.isEmpty()) 0.dp else it.toInt().dp,
                                y = wrapper.y,
                            ),
                        )
                    },
                    modifier = Modifier.padding(end = 8.dp),
                )

                BasicEditableTextProperty(
                    initialValue = initialValue(wrapper.y),
                    label = "Y",
                    validateInput = OffsetValidator()::validate,
                    onValidValueChanged = {
                        composeNodeCallbacks.onModifierUpdatedAt(
                            node,
                            modifierIndex,
                            wrapper.copy(
                                x = wrapper.x,
                                y = if (it.isEmpty()) 0.dp else it.toInt().dp,
                            ),
                        )
                    },
                )
            }
        }
    }
}
