package io.composeflow.ui.inspector.modifier

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.composeflow.editor.validator.RatioValidator
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.ui.propertyeditor.BasicEditableTextProperty
import io.composeflow.ui.propertyeditor.BooleanPropertyEditor

@Composable
fun AspectRatioModifierInspector(
    node: ComposeNode,
    wrapper: ModifierWrapper.AspectRatio,
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
            BasicEditableTextProperty(
                wrapper.ratio.toString(),
                label = "Ratio",
                validateInput = RatioValidator()::validate,
                onValidValueChanged = {
                    composeNodeCallbacks.onModifierUpdatedAt(
                        node,
                        modifierIndex,
                        wrapper.copy(
                            ratio = if (it.isEmpty()) 0f else it.toFloat(),
                            matchHeightConstraintsFirst = wrapper.matchHeightConstraintsFirst,
                        ),
                    )
                },
            )

            BooleanPropertyEditor(
                checked = wrapper.matchHeightConstraintsFirst == true,
                label = "Height first",
                onCheckedChange = {
                    composeNodeCallbacks.onModifierUpdatedAt(
                        node,
                        modifierIndex,
                        wrapper.copy(
                            ratio = wrapper.ratio,
                            matchHeightConstraintsFirst = it,
                        ),
                    )
                },
            )
        }
    }
}
