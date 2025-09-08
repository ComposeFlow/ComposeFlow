package io.composeflow.ui.inspector.parameter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.East
import androidx.compose.material.icons.outlined.FilterCenterFocus
import androidx.compose.material.icons.outlined.North
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.NorthWest
import androidx.compose.material.icons.outlined.South
import androidx.compose.material.icons.outlined.SouthEast
import androidx.compose.material.icons.outlined.SouthWest
import androidx.compose.material.icons.outlined.West
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.auth.LocalFirebaseIdToken
import io.composeflow.editor.validator.AlphaValidator
import io.composeflow.model.enumwrapper.ContentScaleWrapper
import io.composeflow.model.parameter.DEFAULT_URL
import io.composeflow.model.parameter.ImageAssetType
import io.composeflow.model.parameter.ImageTrait
import io.composeflow.model.parameter.PlaceholderUrl
import io.composeflow.model.parameter.wrapper.AlignmentWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.model.property.IntrinsicProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.placeholder_content_desc
import io.composeflow.ui.LocalOnShowSnackbar
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIconToggleButton
import io.composeflow.ui.inspector.ParamInspectorHeaderRow
import io.composeflow.ui.labeledbox.LabeledBorderBox
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.propertyeditor.AssignableEditableTextPropertyEditor
import io.composeflow.ui.propertyeditor.AssignableEnumPropertyEditor
import io.composeflow.ui.propertyeditor.BasicEditableTextProperty
import io.composeflow.ui.propertyeditor.BooleanPropertyEditor
import io.composeflow.ui.propertyeditor.DropdownProperty
import io.composeflow.ui.propertyeditor.EditableTextProperty
import io.composeflow.ui.utils.asImageComposable
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun ImageParamsInspector(
    project: Project,
    node: ComposeNode,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    val imageTrait = node.trait.value as ImageTrait
    val coroutineScope = rememberCoroutineScope()
    val onShowSnackbar = LocalOnShowSnackbar.current
    Column {
        LabeledBorderBox(
            label = "Image type",
            modifier = Modifier.hoverOverlay(),
        ) {
            DropdownProperty(
                project = project,
                items = ImageAssetType.entries,
                onValueChanged = { index, _ ->
                    composeNodeCallbacks.onTraitUpdated(
                        node,
                        imageTrait.copy(assetType = ImageAssetType.entries[index]),
                    )
                },
                selectedIndex = imageTrait.assetType.ordinal,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        when (imageTrait.assetType) {
            ImageAssetType.Network -> {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    label = "Url",
                    initialProperty = imageTrait.url,
                    onInitializeProperty = {
                        composeNodeCallbacks.onTraitUpdated(
                            node,
                            imageTrait.copy(url = StringProperty.StringIntrinsicValue(DEFAULT_URL)),
                        )
                    },
                    onValidPropertyChanged = { property, lazyListSource ->
                        val result =
                            composeNodeCallbacks.onParamsUpdatedWithLazyListSource(
                                node,
                                imageTrait.copy(url = property),
                                lazyListSource,
                            )
                        result.errorMessages.forEach {
                            coroutineScope.launch {
                                onShowSnackbar(it, null)
                            }
                        }
                    },
                    modifier = Modifier.hoverOverlay(),
                )

                if (imageTrait.url !is IntrinsicProperty<*>) {
                    val placeholderContentDesc = stringResource(Res.string.placeholder_content_desc)
                    Tooltip(placeholderContentDesc) {
                        BooleanPropertyEditor(
                            checked = imageTrait.placeholderUrl is PlaceholderUrl.Used,
                            onCheckedChange = { placeHolderUsed ->
                                val usage =
                                    if (placeHolderUsed) {
                                        PlaceholderUrl.Used(
                                            StringProperty.StringIntrinsicValue(
                                                DEFAULT_URL,
                                            ),
                                        )
                                    } else {
                                        PlaceholderUrl.NoUsage
                                    }
                                composeNodeCallbacks.onTraitUpdated(
                                    node,
                                    imageTrait.copy(placeholderUrl = usage),
                                )
                            },
                            label = "Use placeholder image",
                            modifier =
                                Modifier
                                    .hoverOverlay(),
                        )
                    }

                    when (val usage = imageTrait.placeholderUrl) {
                        PlaceholderUrl.NoUsage -> {}
                        is PlaceholderUrl.Used -> {
                            EditableTextProperty(
                                initialValue = usage.url.transformedValueExpression(project),
                                onValidValueChanged = {
                                    composeNodeCallbacks.onTraitUpdated(
                                        node,
                                        imageTrait.copy(
                                            placeholderUrl =
                                                PlaceholderUrl.Used(
                                                    StringProperty.StringIntrinsicValue(it),
                                                ),
                                        ),
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .hoverOverlay(),
                                label = "Placeholder image URL",
                                placeholder = "https://example.com/image1",
                                singleLine = true,
                                valueSetFromVariable = false,
                            )
                        }
                    }
                }
            }

            ImageAssetType.Asset -> {
                LabeledBorderBox(
                    label = "Asset",
                    modifier =
                        Modifier
                            .hoverOverlay()
                            .fillMaxWidth(),
                ) {
                    val assets = project.assetHolder.images
                    val userId = LocalFirebaseIdToken.current.user_id
                    DropdownProperty(
                        project = project,
                        items = assets,
                        dropDownMenuText = {
                            Row {
                                it.asImageComposable(
                                    userId = userId,
                                    projectId = project.id,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(48.dp),
                                )

                                Text(
                                    it.fileName,
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                            }
                        },
                        displayText = {
                            imageTrait.blobInfoWrapper?.let {
                                Text(
                                    it.fileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        onValueChanged = { index, _ ->
                            composeNodeCallbacks.onTraitUpdated(
                                node,
                                imageTrait.copy(blobInfoWrapper = assets[index]),
                            )
                        },
                        selectedItem = imageTrait.blobInfoWrapper,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
        }

        AlignmentInspector(node, imageTrait, composeNodeCallbacks)

        Row(verticalAlignment = Alignment.CenterVertically) {
            AssignableEnumPropertyEditor(
                project = project,
                node = node,
                acceptableType =
                    ComposeFlowType.Enum(
                        isList = false,
                        enumClass = ContentScaleWrapper::class,
                    ),
                initialProperty = imageTrait.contentScaleWrapper,
                items = ContentScaleWrapper.entries,
                label = "Content scale",
                onValidPropertyChanged = { property, _ ->
                    composeNodeCallbacks.onTraitUpdated(
                        node,
                        imageTrait.copy(
                            contentScaleWrapper = property,
                        ),
                    )
                },
                onInitializeProperty = {
                    composeNodeCallbacks.onTraitUpdated(
                        node,
                        imageTrait.copy(
                            contentScaleWrapper = null,
                        ),
                    )
                },
                modifier =
                    Modifier
                        .hoverOverlay()
                        .weight(1f),
            )
        }

        Row {
            BasicEditableTextProperty(
                initialValue = imageTrait.alpha?.toString() ?: "",
                label = "Alpha",
                validateInput = AlphaValidator()::validate,
                onValidValueChanged = {
                    composeNodeCallbacks.onTraitUpdated(
                        node,
                        imageTrait.copy(
                            alpha = if (it.isEmpty()) null else it.toFloat(),
                        ),
                    )
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .hoverOverlay(),
            )
        }
    }
}

@Composable
private fun AlignmentInspector(
    node: ComposeNode,
    params: ImageTrait,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    Column(modifier = Modifier.hoverOverlay().padding(vertical = 4.dp)) {
        ParamInspectorHeaderRow(
            label = "Alignment",
        )

        val selected = params.alignmentWrapper

        @Composable
        fun runIconToggleButton(
            alignmentWrapper: AlignmentWrapper,
            alignment: Alignment,
            imageVector: ImageVector? = null,
            contentDesc: String,
        ) = run {
            val thisItemSelected = selected == alignmentWrapper
            Tooltip(contentDesc) {
                ComposeFlowIconToggleButton(
                    checked = thisItemSelected,
                    onCheckedChange = {
                        composeNodeCallbacks.onTraitUpdated(
                            node,
                            params.copy(alignmentWrapper = AlignmentWrapper.fromAlignment(alignment)),
                        )
                    },
                    modifier =
                        Modifier.padding(horizontal = 8.dp).size(24.dp).then(
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
        Row(modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)) {
            runIconToggleButton(
                AlignmentWrapper.TopStart,
                Alignment.TopStart,
                imageVector = Icons.Outlined.NorthWest,
                contentDesc = "Alignment Top Start",
            )
            runIconToggleButton(
                AlignmentWrapper.TopCenter,
                Alignment.TopCenter,
                imageVector = Icons.Outlined.North,
                contentDesc = "Alignment Top Center",
            )
            runIconToggleButton(
                AlignmentWrapper.TopEnd,
                Alignment.TopEnd,
                imageVector = Icons.Outlined.NorthEast,
                contentDesc = "Alignment Top End",
            )
        }
        Row(modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)) {
            runIconToggleButton(
                AlignmentWrapper.CenterStart,
                Alignment.CenterStart,
                imageVector = Icons.Outlined.West,
                contentDesc = "Alignment Center Start",
            )
            runIconToggleButton(
                AlignmentWrapper.Center,
                Alignment.Center,
                imageVector = Icons.Outlined.FilterCenterFocus,
                contentDesc = "Alignment Center",
            )
            runIconToggleButton(
                AlignmentWrapper.CenterEnd,
                Alignment.CenterEnd,
                imageVector = Icons.Outlined.East,
                contentDesc = "Alignment Center End",
            )
        }
        Row(modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)) {
            runIconToggleButton(
                AlignmentWrapper.BottomStart,
                Alignment.BottomStart,
                imageVector = Icons.Outlined.SouthWest,
                contentDesc = "Alignment Bottom Start",
            )
            runIconToggleButton(
                AlignmentWrapper.BottomCenter,
                Alignment.BottomCenter,
                imageVector = Icons.Outlined.South,
                contentDesc = "Alignment Bottom Center",
            )
            runIconToggleButton(
                AlignmentWrapper.BottomEnd,
                Alignment.BottomEnd,
                imageVector = Icons.Outlined.SouthEast,
                contentDesc = "Alignment Bottom End",
            )
        }
    }
}
