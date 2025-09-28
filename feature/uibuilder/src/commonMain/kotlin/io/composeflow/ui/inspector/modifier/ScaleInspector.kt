package io.composeflow.ui.inspector.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.composeflow.editor.validator.ScaleValidator
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIconToggleButton
import io.composeflow.ui.propertyeditor.BasicEditableTextProperty

@Composable
fun ScaleModifierInspector(
    node: ComposeNode,
    wrapper: ModifierWrapper.Scale,
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
            var selectedScaleSpec by remember { mutableStateOf(wrapper.spec()) }

            @Composable
            fun runIconToggleButton(
                scaleSpec: ModifierWrapper.Scale.ScaleSpec,
                imageVector: ImageVector? = null,
                contentDesc: String,
            ) = run {
                val thisItemSelected = selectedScaleSpec == scaleSpec
                Tooltip(contentDesc) {
                    ComposeFlowIconToggleButton(
                        checked = thisItemSelected,
                        onCheckedChange = {
                            selectedScaleSpec = scaleSpec
                        },
                        modifier =
                            Modifier.then(
                                if (thisItemSelected) {
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                alpha = 0.3f,
                                            ),
                                        )
                                } else {
                                    Modifier
                                },
                            ),
                    ) {
                        imageVector?.let {
                            Icon(
                                imageVector = imageVector,
                                contentDescription = contentDesc,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            Row(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
                runIconToggleButton(
                    scaleSpec = ModifierWrapper.Scale.ScaleSpec.All,
                    imageVector = Icons.Outlined.OpenInFull,
                    contentDesc = "All",
                )
                runIconToggleButton(
                    scaleSpec = ModifierWrapper.Scale.ScaleSpec.XandY,
                    imageVector = Icons.Outlined.SwapHoriz,
                    contentDesc = "X and Y",
                )
            }
            when (selectedScaleSpec) {
                ModifierWrapper.Scale.ScaleSpec.All -> {
                    AllScaleInspector(
                        node,
                        wrapper = wrapper,
                        modifierIndex = modifierIndex,
                        composeNodeCallbacks = composeNodeCallbacks,
                    )
                }

                ModifierWrapper.Scale.ScaleSpec.XandY -> {
                    XandYScaleInspector(
                        node,
                        wrapper = wrapper,
                        modifierIndex = modifierIndex,
                        composeNodeCallbacks = composeNodeCallbacks,
                    )
                }
            }
        }
    }
}

@Composable
private fun AllScaleInspector(
    node: ComposeNode,
    wrapper: ModifierWrapper.Scale,
    modifierIndex: Int,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    fun initialValue(float: Float?) = float?.toString() ?: ""
    Row {
        BasicEditableTextProperty(
            initialValue = initialValue(wrapper.scaleX),
            label = "All",
            validateInput = ScaleValidator()::validate,
            onValidValueChanged = {
                composeNodeCallbacks.onModifierUpdatedAt(
                    node,
                    modifierIndex,
                    wrapper.copy(
                        scaleX = it.toFloat(),
                        scaleY = it.toFloat(),
                    ),
                )
            },
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun XandYScaleInspector(
    node: ComposeNode,
    wrapper: ModifierWrapper.Scale,
    modifierIndex: Int,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    fun initialValue(float: Float?) = float?.toString() ?: ""
    Row {
        BasicEditableTextProperty(
            initialValue = initialValue(wrapper.scaleX),
            label = "X",
            validateInput = ScaleValidator()::validate,
            onValidValueChanged = {
                composeNodeCallbacks.onModifierUpdatedAt(
                    node,
                    modifierIndex,
                    wrapper.copy(
                        scaleX = it.toFloat(),
                        scaleY = wrapper.scaleY,
                    ),
                )
            },
            modifier = Modifier.padding(end = 8.dp),
        )

        BasicEditableTextProperty(
            initialValue = initialValue(wrapper.scaleY),
            label = "Y",
            validateInput = ScaleValidator()::validate,
            onValidValueChanged = {
                composeNodeCallbacks.onModifierUpdatedAt(
                    node,
                    modifierIndex,
                    wrapper.copy(
                        scaleX = wrapper.scaleX,
                        scaleY = it.toFloat(),
                    ),
                )
            },
        )
    }
}
