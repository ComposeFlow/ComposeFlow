package io.composeflow.ui.inspector

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.model.parameter.AbstractIconTrait
import io.composeflow.model.parameter.BottomAppBarTrait
import io.composeflow.model.parameter.BoxTrait
import io.composeflow.model.parameter.ButtonTrait
import io.composeflow.model.parameter.CardTrait
import io.composeflow.model.parameter.CheckboxTrait
import io.composeflow.model.parameter.ChipGroupTrait
import io.composeflow.model.parameter.ColumnTrait
import io.composeflow.model.parameter.ComponentTrait
import io.composeflow.model.parameter.DividerTrait
import io.composeflow.model.parameter.DropdownTrait
import io.composeflow.model.parameter.EmptyTrait
import io.composeflow.model.parameter.FabTrait
import io.composeflow.model.parameter.GoogleSignInButtonTrait
import io.composeflow.model.parameter.HorizontalDividerTrait
import io.composeflow.model.parameter.HorizontalPagerTrait
import io.composeflow.model.parameter.IconButtonTrait
import io.composeflow.model.parameter.IconTrait
import io.composeflow.model.parameter.ImageTrait
import io.composeflow.model.parameter.LazyColumnTrait
import io.composeflow.model.parameter.LazyHorizontalGridTrait
import io.composeflow.model.parameter.LazyListTrait
import io.composeflow.model.parameter.LazyRowTrait
import io.composeflow.model.parameter.LazyVerticalGridTrait
import io.composeflow.model.parameter.NavigationDrawerItemTrait
import io.composeflow.model.parameter.NavigationDrawerTrait
import io.composeflow.model.parameter.PagerTrait
import io.composeflow.model.parameter.RowTrait
import io.composeflow.model.parameter.ScreenTrait
import io.composeflow.model.parameter.SliderTrait
import io.composeflow.model.parameter.SpacerTrait
import io.composeflow.model.parameter.SwitchTrait
import io.composeflow.model.parameter.TabContentTrait
import io.composeflow.model.parameter.TabRowTrait
import io.composeflow.model.parameter.TabTrait
import io.composeflow.model.parameter.TabsTrait
import io.composeflow.model.parameter.TextFieldTrait
import io.composeflow.model.parameter.TextTrait
import io.composeflow.model.parameter.TopAppBarTrait
import io.composeflow.model.parameter.VerticalDividerTrait
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.model.project.component.Component
import io.composeflow.model.project.findComponentOrThrow
import io.composeflow.multiple_composables_selected
import io.composeflow.select_single_composable_to_edit_properties
import io.composeflow.selected_composables
import io.composeflow.ui.inspector.component.ComponentInspector
import io.composeflow.ui.inspector.component.ComponentParameterInspector
import io.composeflow.ui.inspector.component.ScreenParameterInspector
import io.composeflow.ui.inspector.dynamicitems.DynamicItemsInspector
import io.composeflow.ui.inspector.lazylist.LazyListChildInspector
import io.composeflow.ui.inspector.modifier.modifierInspector
import io.composeflow.ui.inspector.parameter.BottomAppBarParamsInspector
import io.composeflow.ui.inspector.parameter.BoxParamsInspector
import io.composeflow.ui.inspector.parameter.ButtonParamsInspector
import io.composeflow.ui.inspector.parameter.CardParamsInspector
import io.composeflow.ui.inspector.parameter.CheckboxParamsInspector
import io.composeflow.ui.inspector.parameter.ChipGroupParamsInspector
import io.composeflow.ui.inspector.parameter.ColumnParamsInspector
import io.composeflow.ui.inspector.parameter.DividerParamsInspector
import io.composeflow.ui.inspector.parameter.DropdownParamsInspector
import io.composeflow.ui.inspector.parameter.FabParamsInspector
import io.composeflow.ui.inspector.parameter.GoogleSignInButtonParamsInspector
import io.composeflow.ui.inspector.parameter.HorizontalPagerParamsInspector
import io.composeflow.ui.inspector.parameter.IconButtonParamsInspector
import io.composeflow.ui.inspector.parameter.IconParamsInspector
import io.composeflow.ui.inspector.parameter.ImageParamsInspector
import io.composeflow.ui.inspector.parameter.LazyColumnParamsInspector
import io.composeflow.ui.inspector.parameter.LazyHorizontalGridParamsInspector
import io.composeflow.ui.inspector.parameter.LazyRowParamsInspector
import io.composeflow.ui.inspector.parameter.LazyVerticalGridParamsInspector
import io.composeflow.ui.inspector.parameter.NavigationDrawerItemParamsInspector
import io.composeflow.ui.inspector.parameter.NavigationDrawerParamsInspector
import io.composeflow.ui.inspector.parameter.RowParamsInspector
import io.composeflow.ui.inspector.parameter.SliderParamsInspector
import io.composeflow.ui.inspector.parameter.SwitchParamsInspector
import io.composeflow.ui.inspector.parameter.TabParamsInspector
import io.composeflow.ui.inspector.parameter.TabRowParamsInspector
import io.composeflow.ui.inspector.parameter.TextFieldParamsInspector
import io.composeflow.ui.inspector.parameter.TextParamsInspector
import io.composeflow.ui.inspector.parameter.TopAppBarParamsInspector
import io.composeflow.ui.inspector.propertyeditor.NonEditableTextProperty
import io.composeflow.ui.inspector.tabs.TabRowInspector
import io.composeflow.ui.inspector.validator.TextFieldValidatorInspector
import io.composeflow.ui.inspector.visibility.VisibilityInspector
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.text.EditableText
import io.composeflow.ui.utils.TreeExpanderInverse
import org.jetbrains.compose.resources.stringResource

@Composable
fun PropertyInspector(
    project: Project,
    composeNodeCallbacks: ComposeNodeCallbacks,
    modifier: Modifier = Modifier,
) {
    val focusedNodes = project.screenHolder.findFocusedNodes()
    val editable = project.screenHolder.currentEditable()
    if (focusedNodes.isEmpty()) {
        EmptyInspector()
    } else if (focusedNodes.size > 1) {
        MultipleSelectionInspector(
            focusedNodes = focusedNodes,
            modifier = modifier,
        )
    } else {
        val composeNode = focusedNodes[0]
        val lazyListState = rememberLazyListState()
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            state = lazyListState,
            modifier =
                modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxSize(),
        ) {
            item {
                SizeInspector(composeNode)
            }
            if (editable is Component) {
                item {
                    SectionDivider()
                    val component = project.findComponentOrThrow(editable.id)
                    ComponentParameterInspector(
                        project = project,
                        component = component,
                        composeNodeCallbacks = composeNodeCallbacks,
                    )
                }
            }
            if (composeNode.trait.value is ScreenTrait && editable is Screen) {
                item {
                    SectionDivider()
                    ScreenParameterInspector(
                        project = project,
                        screen = editable,
                        composeNodeCallbacks = composeNodeCallbacks,
                    )
                }
            }
            if (composeNode.inspectable) {
                if (composeNode.trait.value.hasDynamicItems()) {
                    item {
                        SectionDivider()
                        DynamicItemsInspector(
                            project = project,
                            node = composeNode,
                            composeNodeCallbacks = composeNodeCallbacks,
                        )
                    }
                }
                if (composeNode.trait.value is TabRowTrait) {
                    item {
                        SectionDivider()
                        TabRowInspector(
                            project = project,
                            node = composeNode,
                        )
                    }
                }
                composeNode.parentNode?.let { parent ->
                    if (parent.trait.value.isLazyList()) {
                        item {
                            SectionDivider()
                            LazyListChildInspector(
                                project = project,
                                node = composeNode,
                                composeNodeCallbacks = composeNodeCallbacks,
                            )
                        }
                    }
                }
                if (composeNode.trait.value !is ScreenTrait) {
                    item {
                        SectionDivider()
                        ParamsInspector(
                            composeNode = composeNode,
                            project = project,
                            composeNodeCallbacks = composeNodeCallbacks,
                        )
                    }
                }
                if (composeNode.trait.value is TextFieldTrait) {
                    item {
                        SectionDivider()
                        TextFieldValidatorInspector(
                            project = project,
                            node = composeNode,
                            composeNodeCallbacks = composeNodeCallbacks,
                        )
                    }
                }
                // Visibility of TopAppBar and Fab is configured through the toolbar in the canvas
                if (composeNode.trait.value.isVisibilityConditional()) {
                    item {
                        SectionDivider()
                        VisibilityInspector(
                            project = project,
                            node = composeNode,
                            composeNodeCallbacks = composeNodeCallbacks,
                        )
                    }
                }
                item {
                    SectionDivider()
                }
            }
            if (composeNode.trait.value.isModifierAttachable()) {
                modifierInspector(
                    project = project,
                    listState = lazyListState,
                    composeNodeCallbacks = composeNodeCallbacks,
                )
            }
        }
    }
}

@Composable
fun ParamsInspector(
    composeNode: ComposeNode,
    project: Project,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    Column(modifier = Modifier.animateContentSize(keyframes { durationMillis = 100 })) {
        var expanded by remember(composeNode.id) { mutableStateOf(true) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded }
                    .hoverIconClickable(),
        ) {
            EditableText(
                initialText = composeNode.label.value,
                onValueChange = {
                    composeNodeCallbacks.onComposeNodeLabelUpdated(composeNode, it)
                },
            )
            Spacer(modifier = Modifier.weight(1f))

            TreeExpanderInverse(
                expanded = expanded,
                onClick = { expanded = !expanded },
            )
        }

        if (expanded) {
            Column(modifier = Modifier.padding(start = 8.dp)) {
                when (composeNode.trait.value) {
                    is ColumnTrait -> {
                        ColumnParamsInspector(
                            composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is RowTrait -> {
                        RowParamsInspector(
                            composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is BoxTrait -> {
                        BoxParamsInspector(
                            composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is TextTrait -> {
                        TextParamsInspector(
                            project = project,
                            composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is IconTrait -> {
                        IconParamsInspector(
                            project = project,
                            composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is IconButtonTrait -> {
                        IconButtonParamsInspector(
                            project = project,
                            composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is ImageTrait -> {
                        ImageParamsInspector(
                            project = project,
                            composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is ButtonTrait -> {
                        ButtonParamsInspector(
                            project = project,
                            composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is TextFieldTrait -> {
                        TextFieldParamsInspector(
                            project = project,
                            node = composeNode,
                            composeNodeCallbacks = composeNodeCallbacks,
                        )
                    }

                    is LazyColumnTrait -> {
                        LazyColumnParamsInspector(
                            composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is LazyRowTrait -> {
                        LazyRowParamsInspector(
                            composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is HorizontalPagerTrait -> {
                        HorizontalPagerParamsInspector(
                            project = project,
                            composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is LazyVerticalGridTrait -> {
                        LazyVerticalGridParamsInspector(
                            project = project,
                            composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is LazyHorizontalGridTrait -> {
                        LazyHorizontalGridParamsInspector(
                            project = project,
                            composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is CardTrait -> {
                        CardParamsInspector(
                            project = project,
                            node = composeNode,
                            composeNodeCallbacks = composeNodeCallbacks,
                        )
                    }

                    is TabsTrait -> {
                    }

                    is TabRowTrait -> {
                        TabRowParamsInspector(
                            node = composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is TabTrait -> {
                        TabParamsInspector(
                            project = project,
                            node = composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is TabContentTrait -> {
                    }

                    is ChipGroupTrait -> {
                        ChipGroupParamsInspector(
                            project = project,
                            node = composeNode,
                            composeNodeCallbacks = composeNodeCallbacks,
                        )
                    }

                    is TopAppBarTrait -> {
                        TopAppBarParamsInspector(
                            node = composeNode,
                            project = project,
                            composeNodeCallbacks,
                        )
                    }

                    is BottomAppBarTrait -> {
                        BottomAppBarParamsInspector(
                            node = composeNode,
                            project = project,
                            composeNodeCallbacks,
                        )
                    }

                    is NavigationDrawerTrait -> {
                        NavigationDrawerParamsInspector(
                            node = composeNode,
                            project = project,
                            composeNodeCallbacks,
                        )
                    }

                    is NavigationDrawerItemTrait -> {
                        NavigationDrawerItemParamsInspector(
                            node = composeNode,
                            project = project,
                            composeNodeCallbacks,
                        )
                    }

                    is FabTrait -> {
                        FabParamsInspector(
                            project = project,
                            node = composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is HorizontalDividerTrait, is VerticalDividerTrait -> {
                        DividerParamsInspector(
                            project = project,
                            node = composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is SwitchTrait -> {
                        SwitchParamsInspector(
                            project = project,
                            node = composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is CheckboxTrait -> {
                        CheckboxParamsInspector(
                            project = project,
                            node = composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is SliderTrait -> {
                        SliderParamsInspector(
                            project = project,
                            node = composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is DropdownTrait -> {
                        DropdownParamsInspector(
                            project = project,
                            node = composeNode,
                            composeNodeCallbacks,
                        )
                    }

                    is SpacerTrait -> {}
                    is ComponentTrait -> {
                        composeNode.componentId?.let {
                            val component = project.findComponentOrThrow(it)
                            ComponentInspector(
                                project = project,
                                node = composeNode,
                                component = component,
                                composeNodeCallbacks = composeNodeCallbacks,
                            )
                        }
                    }

                    is ScreenTrait -> {
                        // Nothing for screen
                    }

                    is GoogleSignInButtonTrait -> {
                        GoogleSignInButtonParamsInspector(
                            node = composeNode,
                            composeNodeCallbacks = composeNodeCallbacks,
                        )
                    }

                    is DividerTrait -> {}
                    EmptyTrait -> {}
                    is LazyListTrait -> {}
                    is PagerTrait -> {}
                    is AbstractIconTrait -> {}
                }
            }
        }
    }
}

@Composable
private fun EmptyInspector() {
}

@Composable
private fun SizeInspector(composeNode: ComposeNode) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
    ) {
        NonEditableTextProperty(
            "Width",
            composeNode.boundsInWindow.value.size.width
                .toInt()
                .toString(),
            modifier = Modifier.weight(1f),
        )
        NonEditableTextProperty(
            "Height",
            composeNode.boundsInWindow.value.size.height
                .toInt()
                .toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun ParamInspectorHeaderRow(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.inverseOnSurface,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun MultipleSelectionInspector(
    focusedNodes: List<ComposeNode>,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        state = lazyListState,
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxSize(),
    ) {
        item {
            // Show collective size information for multiple selected nodes
            MultipleSelectionSizeInspector(focusedNodes)
        }
        item {
            SectionDivider()
            MultipleSelectionParamsInspector(
                focusedNodes = focusedNodes,
            )
        }
    }
}

@Composable
private fun MultipleSelectionSizeInspector(focusedNodes: List<ComposeNode>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
    ) {
        NonEditableTextProperty(
            "Selected",
            stringResource(Res.string.selected_composables, focusedNodes.size),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MultipleSelectionParamsInspector(focusedNodes: List<ComposeNode>) {
    Column(modifier = Modifier.animateContentSize(keyframes { durationMillis = 100 })) {
        var expanded by remember { mutableStateOf(true) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded }
                    .hoverIconClickable(),
        ) {
            Text(
                text = stringResource(Res.string.multiple_composables_selected, focusedNodes.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))

            TreeExpanderInverse(
                expanded = expanded,
                onClick = { expanded = !expanded },
            )
        }

        if (expanded) {
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = stringResource(Res.string.select_single_composable_to_edit_properties),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }
    }
}
