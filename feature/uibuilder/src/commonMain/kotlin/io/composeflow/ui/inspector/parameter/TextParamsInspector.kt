package io.composeflow.ui.inspector.parameter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.editor.validator.IntValidator
import io.composeflow.model.enumwrapper.FontStyleWrapper
import io.composeflow.model.enumwrapper.TextAlignWrapper
import io.composeflow.model.enumwrapper.TextDecorationWrapper
import io.composeflow.model.enumwrapper.TextOverflowWrapper
import io.composeflow.model.enumwrapper.TextStyleWrapper
import io.composeflow.model.parameter.PlaceholderText
import io.composeflow.model.parameter.TextTrait
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.model.property.IntrinsicProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.parse_text_as_html
import io.composeflow.placeholder_content_desc
import io.composeflow.ui.LocalOnShowSnackbar
import io.composeflow.ui.Tooltip
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.propertyeditor.AssignableColorPropertyEditor
import io.composeflow.ui.propertyeditor.AssignableEditableTextPropertyEditor
import io.composeflow.ui.propertyeditor.AssignableEnumPropertyEditor
import io.composeflow.ui.propertyeditor.BasicEditableTextProperty
import io.composeflow.ui.propertyeditor.BooleanPropertyEditor
import io.composeflow.ui.propertyeditor.EditableTextProperty
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun TextParamsInspector(
    project: Project,
    node: ComposeNode,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    val textTrait = node.trait.value as TextTrait
    val coroutineScope = rememberCoroutineScope()
    val onShowSnackbar = LocalOnShowSnackbar.current
    Column {
        AssignableEditableTextPropertyEditor(
            project = project,
            node = node,
            label = "Text",
            initialProperty = textTrait.text,
            onInitializeProperty = {
                composeNodeCallbacks.onTraitUpdated(
                    node,
                    textTrait.copy(text = StringProperty.StringIntrinsicValue("")),
                )
            },
            onValidPropertyChanged = { property, lazyListSource ->
                val result =
                    composeNodeCallbacks.onParamsUpdatedWithLazyListSource(
                        node,
                        textTrait.copy(text = property),
                        lazyListSource,
                    )
                result.errorMessages.forEach {
                    coroutineScope.launch {
                        onShowSnackbar(it, null)
                    }
                }
            },
            modifier = Modifier.hoverOverlay().fillMaxWidth(),
            singleLine = false,
        )

        if (textTrait.text !is IntrinsicProperty<*>) {
            val placeholderContentDesc = stringResource(Res.string.placeholder_content_desc)
            Tooltip(placeholderContentDesc) {
                BooleanPropertyEditor(
                    checked = textTrait.placeholderText is PlaceholderText.Used,
                    onCheckedChange = { placeHolderUsed ->
                        val usage =
                            if (placeHolderUsed) {
                                PlaceholderText.Used()
                            } else {
                                PlaceholderText.NoUsage
                            }
                        composeNodeCallbacks.onTraitUpdated(
                            node,
                            textTrait.copy(placeholderText = usage),
                        )
                    },
                    label = "Use placeholder text",
                    modifier =
                        Modifier
                            .hoverOverlay(),
                )
            }

            when (val usage = textTrait.placeholderText) {
                PlaceholderText.NoUsage -> {}
                is PlaceholderText.Used -> {
                    EditableTextProperty(
                        initialValue = usage.value.transformedValueExpression(project),
                        onValidValueChanged = {
                            composeNodeCallbacks.onTraitUpdated(
                                node,
                                textTrait.copy(
                                    placeholderText =
                                        PlaceholderText.Used(
                                            StringProperty.StringIntrinsicValue(
                                                it,
                                            ),
                                        ),
                                ),
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .hoverOverlay(),
                        label = "Placeholder text",
                        placeholder = "placeholder text",
                        singleLine = true,
                        valueSetFromVariable = false,
                    )
                }
            }
        }

        AssignableColorPropertyEditor(
            project = project,
            node = node,
            label = "Color",
            acceptableType = ComposeFlowType.Color(),
            initialProperty = textTrait.colorWrapper,
            onValidPropertyChanged = { property, lazyListSource ->
                composeNodeCallbacks.onParamsUpdatedWithLazyListSource(
                    node,
                    textTrait.copy(colorWrapper = property),
                    lazyListSource,
                )
            },
            onInitializeProperty = {
                composeNodeCallbacks.onTraitUpdated(node, textTrait.copy(colorWrapper = null))
            },
            modifier =
                Modifier
                    .hoverOverlay()
                    .fillMaxWidth(),
        )

        AssignableEnumPropertyEditor(
            project = project,
            node = node,
            acceptableType =
                ComposeFlowType.Enum(
                    isList = false,
                    enumClass = TextStyleWrapper::class,
                ),
            initialProperty = textTrait.textStyleWrapper,
            items = TextStyleWrapper.entries,
            label = "Style",
            onValidPropertyChanged = { property, _ ->
                composeNodeCallbacks.onTraitUpdated(
                    node,
                    textTrait.copy(
                        textStyleWrapper = property,
                    ),
                )
            },
            onInitializeProperty = {
                composeNodeCallbacks.onTraitUpdated(
                    node,
                    textTrait.copy(
                        textStyleWrapper = null,
                    ),
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .hoverOverlay(),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            AssignableEnumPropertyEditor(
                project = project,
                node = node,
                acceptableType =
                    ComposeFlowType.Enum(
                        isList = false,
                        enumClass = TextDecorationWrapper::class,
                    ),
                initialProperty = textTrait.textDecoration,
                items = TextDecorationWrapper.entries,
                label = "Decoration",
                onValidPropertyChanged = { property, _ ->
                    composeNodeCallbacks.onTraitUpdated(
                        node,
                        textTrait.copy(
                            textDecoration = property,
                        ),
                    )
                },
                onInitializeProperty = {
                    composeNodeCallbacks.onTraitUpdated(
                        node,
                        textTrait.copy(
                            textDecoration = null,
                        ),
                    )
                },
                modifier =
                    Modifier
                        .hoverOverlay()
                        .weight(1f),
            )

            AssignableEnumPropertyEditor(
                project = project,
                node = node,
                acceptableType =
                    ComposeFlowType.Enum(
                        isList = false,
                        enumClass = FontStyleWrapper::class,
                    ),
                initialProperty = textTrait.fontStyle,
                items = FontStyleWrapper.entries,
                label = "Font style",
                onValidPropertyChanged = { property, _ ->
                    composeNodeCallbacks.onTraitUpdated(
                        node,
                        textTrait.copy(
                            fontStyle = property,
                        ),
                    )
                },
                onInitializeProperty = {
                    composeNodeCallbacks.onTraitUpdated(
                        node,
                        textTrait.copy(
                            fontStyle = null,
                        ),
                    )
                },
                modifier =
                    Modifier
                        .hoverOverlay()
                        .weight(1f),
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            AssignableEnumPropertyEditor(
                project = project,
                node = node,
                acceptableType =
                    ComposeFlowType.Enum(
                        isList = false,
                        enumClass = TextAlignWrapper::class,
                    ),
                initialProperty = textTrait.textAlign,
                items = TextAlignWrapper.entries,
                label = "Align",
                onValidPropertyChanged = { property, _ ->
                    composeNodeCallbacks.onTraitUpdated(
                        node,
                        textTrait.copy(
                            textAlign = property,
                        ),
                    )
                },
                onInitializeProperty = {
                    composeNodeCallbacks.onTraitUpdated(
                        node,
                        textTrait.copy(
                            textAlign = null,
                        ),
                    )
                },
                modifier =
                    Modifier
                        .hoverOverlay()
                        .weight(1f),
            )

            AssignableEnumPropertyEditor(
                project = project,
                node = node,
                acceptableType =
                    ComposeFlowType.Enum(
                        isList = false,
                        enumClass = TextOverflowWrapper::class,
                    ),
                initialProperty = textTrait.overflow,
                items = TextOverflowWrapper.entries,
                label = "Overflow",
                onValidPropertyChanged = { property, _ ->
                    composeNodeCallbacks.onTraitUpdated(
                        node,
                        textTrait.copy(
                            overflow = property,
                        ),
                    )
                },
                onInitializeProperty = {
                    composeNodeCallbacks.onTraitUpdated(
                        node,
                        textTrait.copy(
                            overflow = null,
                        ),
                    )
                },
                modifier =
                    Modifier
                        .hoverOverlay()
                        .weight(1f),
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            BooleanPropertyEditor(
                checked = textTrait.softWrap ?: true,
                label = "Soft wrap",
                onCheckedChange = {
                    composeNodeCallbacks.onTraitUpdated(node, textTrait.copy(softWrap = it))
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .hoverOverlay(),
            )

            BasicEditableTextProperty(
                label = "Max lines",
                initialValue = textTrait.maxLines?.toString() ?: "",
                onValidValueChanged = {
                    val value = if (it.isEmpty()) null else it.toInt()
                    composeNodeCallbacks.onTraitUpdated(node, textTrait.copy(maxLines = value))
                },
                validateInput = {
                    IntValidator(
                        allowLessThanZero = false,
                    ).validate(input = it)
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .hoverOverlay(),
                singleLine = true,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            val parseTextDescription = stringResource(Res.string.parse_text_as_html)
            Tooltip(parseTextDescription) {
                BooleanPropertyEditor(
                    checked = textTrait.parseHtml ?: false,
                    label = "Parse HTML",
                    onCheckedChange = {
                        composeNodeCallbacks.onTraitUpdated(node, textTrait.copy(parseHtml = it))
                    },
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .hoverOverlay(),
                )
            }

            Spacer(Modifier.weight(1f))
        }
    }
}
