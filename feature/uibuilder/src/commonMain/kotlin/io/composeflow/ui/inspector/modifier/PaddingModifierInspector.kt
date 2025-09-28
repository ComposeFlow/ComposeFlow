package io.composeflow.ui.inspector.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BorderHorizontal
import androidx.compose.material.icons.outlined.BorderInner
import androidx.compose.material.icons.outlined.BorderOuter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.composeflow.editor.validator.DpValidator
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconToggleButton
import io.composeflow.ui.propertyeditor.BasicEditableTextProperty

@Composable
fun PaddingModifierInspector(
    node: ComposeNode,
    wrapper: ModifierWrapper.Padding,
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
        Column {
            var selectedPaddingSpec by remember { mutableStateOf(wrapper.spec()) }

            @Composable
            fun runIconToggleButton(
                paddingSpec: ModifierWrapper.Padding.PaddingSpec,
                imageVector: ImageVector? = null,
                contentDesc: String,
            ) = run {
                val thisItemSelected = selectedPaddingSpec == paddingSpec
                Tooltip(contentDesc) {
                    ComposeFlowIconToggleButton(
                        checked = thisItemSelected,
                        onCheckedChange = {
                            selectedPaddingSpec = paddingSpec
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
                            ComposeFlowIcon(
                                imageVector = imageVector,
                                contentDescription = contentDesc,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            Row(modifier = Modifier.padding(start = 36.dp)) {
                runIconToggleButton(
                    paddingSpec = ModifierWrapper.Padding.PaddingSpec.All,
                    imageVector = Icons.Outlined.BorderOuter,
                    contentDesc = "All",
                )
                runIconToggleButton(
                    paddingSpec = ModifierWrapper.Padding.PaddingSpec.HorizontalVertical,
                    imageVector = Icons.Outlined.BorderHorizontal,
                    contentDesc = "Horizontal and vertical",
                )
                runIconToggleButton(
                    paddingSpec = ModifierWrapper.Padding.PaddingSpec.Individual,
                    imageVector = Icons.Outlined.BorderInner,
                    contentDesc = "Individual",
                )
            }
            when (selectedPaddingSpec) {
                ModifierWrapper.Padding.PaddingSpec.All -> {
                    AllPaddingInspector(
                        node,
                        wrapper = wrapper,
                        modifierIndex = modifierIndex,
                        composeNodeCallbacks = composeNodeCallbacks,
                    )
                }

                ModifierWrapper.Padding.PaddingSpec.HorizontalVertical -> {
                    HorizontalAndVerticalPaddingInspector(
                        node,
                        wrapper = wrapper,
                        modifierIndex = modifierIndex,
                        composeNodeCallbacks = composeNodeCallbacks,
                    )
                }

                ModifierWrapper.Padding.PaddingSpec.Individual -> {
                    IndividualPaddingInspector(
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
fun AllPaddingInspector(
    node: ComposeNode,
    wrapper: ModifierWrapper.Padding,
    modifierIndex: Int,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    BasicEditableTextProperty(
        initialValue =
            wrapper.start.value
                .toInt()
                .toString(),
        label = "All",
        validateInput = DpValidator()::validate,
        onValidValueChanged = {
            val newValue = if (it.isEmpty()) 0.dp else it.toFloat().dp
            composeNodeCallbacks.onModifierUpdatedAt(
                node,
                modifierIndex,
                wrapper.copy(
                    start = newValue,
                    top = newValue,
                    end = newValue,
                    bottom = newValue,
                ),
            )
        },
        modifier = Modifier.padding(start = 36.dp),
    )
}

@Composable
fun HorizontalAndVerticalPaddingInspector(
    node: ComposeNode,
    wrapper: ModifierWrapper.Padding,
    modifierIndex: Int,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 36.dp),
    ) {
        BasicEditableTextProperty(
            initialValue =
                wrapper.start.value
                    .toInt()
                    .toString(),
            label = "Horizontal",
            validateInput = DpValidator()::validate,
            onValidValueChanged = {
                val newValue = if (it.isEmpty()) 0.dp else it.toFloat().dp
                composeNodeCallbacks.onModifierUpdatedAt(
                    node,
                    modifierIndex,
                    wrapper.copy(
                        start = newValue,
                        top = wrapper.top,
                        end = newValue,
                        bottom = wrapper.bottom,
                    ),
                )
            },
            modifier = Modifier.padding(end = 8.dp),
        )
        BasicEditableTextProperty(
            initialValue =
                wrapper.top.value
                    .toInt()
                    .toString(),
            label = "Vertical",
            validateInput = DpValidator()::validate,
            onValidValueChanged = {
                val newValue = if (it.isEmpty()) 0.dp else it.toFloat().dp
                composeNodeCallbacks.onModifierUpdatedAt(
                    node,
                    modifierIndex,
                    wrapper.copy(
                        start = wrapper.start,
                        top = newValue,
                        end = wrapper.end,
                        bottom = newValue,
                    ),
                )
            },
        )
    }
}

@Composable
fun IndividualPaddingInspector(
    node: ComposeNode,
    wrapper: ModifierWrapper.Padding,
    modifierIndex: Int,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    Box(
        modifier =
            Modifier
                .padding(start = 36.dp)
                .fillMaxWidth()
                .height(156.dp),
    ) {
        BasicEditableTextProperty(
            initialValue =
                wrapper.start.value
                    .toInt()
                    .toString(),
            label = "Start",
            validateInput = DpValidator()::validate,
            onValidValueChanged = {
                composeNodeCallbacks.onModifierUpdatedAt(
                    node,
                    modifierIndex,
                    wrapper.copy(
                        start = if (it.isEmpty()) 0.dp else it.toFloat().dp,
                        top = wrapper.top,
                        end = wrapper.end,
                        bottom = wrapper.bottom,
                    ),
                )
            },
            modifier = Modifier.align(Alignment.CenterStart),
        )
        BasicEditableTextProperty(
            initialValue =
                wrapper.top.value
                    .toInt()
                    .toString(),
            label = "Top",
            validateInput = DpValidator()::validate,
            onValidValueChanged = {
                composeNodeCallbacks.onModifierUpdatedAt(
                    node,
                    modifierIndex,
                    wrapper.copy(
                        start = wrapper.start,
                        top = if (it.isEmpty()) 0.dp else it.toFloat().dp,
                        end = wrapper.end,
                        bottom = wrapper.bottom,
                    ),
                )
            },
            modifier = Modifier.align(Alignment.TopCenter),
        )
        BasicEditableTextProperty(
            initialValue =
                wrapper.end.value
                    .toInt()
                    .toString(),
            label = "End",
            validateInput = DpValidator()::validate,
            onValidValueChanged = {
                composeNodeCallbacks.onModifierUpdatedAt(
                    node,
                    modifierIndex,
                    wrapper.copy(
                        start = wrapper.start,
                        top = wrapper.top,
                        end = if (it.isEmpty()) 0.dp else it.toFloat().dp,
                        bottom = wrapper.bottom,
                    ),
                )
            },
            modifier = Modifier.align(Alignment.CenterEnd),
        )
        BasicEditableTextProperty(
            initialValue =
                wrapper.bottom.value
                    .toInt()
                    .toString(),
            label = "Bottom",
            validateInput = DpValidator()::validate,
            onValidValueChanged = {
                composeNodeCallbacks.onModifierUpdatedAt(
                    node,
                    modifierIndex,
                    wrapper.copy(
                        start = wrapper.start,
                        top = wrapper.top,
                        end = wrapper.end,
                        bottom = if (it.isEmpty()) 0.dp else it.toFloat().dp,
                    ),
                )
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
