package io.composeflow.ui.inspector.modifier

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.composeflow.editor.validator.SizeValidator
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.ui.propertyeditor.BasicEditableTextProperty

@Composable
fun SizeModifierInspector(
    node: ComposeNode,
    wrapper: ModifierWrapper.Size,
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
            fun initialValue(dp: Dp) = if (dp == Dp.Unspecified) "" else dp.value.toInt().toString()

            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicEditableTextProperty(
                    initialValue = initialValue(wrapper.width),
                    label = "Width",
                    validateInput = SizeValidator()::validate,
                    onValidValueChanged = {
                        composeNodeCallbacks.onModifierUpdatedAt(
                            node,
                            modifierIndex,
                            wrapper.copy(
                                width = if (it.isEmpty()) Dp.Unspecified else it.toFloat().dp,
                                height = wrapper.height,
                            ),
                        )
                    },
                    modifier = Modifier.padding(end = 8.dp),
                )

                BasicEditableTextProperty(
                    initialValue = initialValue(wrapper.height),
                    label = "Height",
                    validateInput = SizeValidator()::validate,
                    onValidValueChanged = {
                        composeNodeCallbacks.onModifierUpdatedAt(
                            node,
                            modifierIndex,
                            wrapper.copy(
                                width = wrapper.width,
                                height = if (it.isEmpty()) Dp.Unspecified else it.toFloat().dp,
                            ),
                        )
                    },
                )
            }
        }
    }
}
