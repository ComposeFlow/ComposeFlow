package io.composeflow.ui.inspector.lazylist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.editor.validator.NotEmptyNotLessThanZeroIntValidator
import io.composeflow.lazy_list_child_description
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.propertyeditor.BasicEditableTextProperty
import org.jetbrains.compose.resources.stringResource

@Composable
fun LazyListChildInspector(
    project: Project,
    node: ComposeNode,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "LazyList child",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            val lazyListDesc = stringResource(Res.string.lazy_list_child_description)
            Tooltip(lazyListDesc) {
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = lazyListDesc,
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        Column(modifier = Modifier.padding(start = 8.dp)) {
            when (val childParams = node.lazyListChildParams.value) {
                is LazyListChildParams.FixedNumber -> {
                    FixedNumberChildrenInspector(
                        node = node,
                        childParams = childParams,
                        composeNodeCallbacks = composeNodeCallbacks,
                    )
                }

                is LazyListChildParams.DynamicItemsSource -> {
                    DynamicSourceChildrenInspector(
                        project = project,
                        node = node,
                        childParams = childParams,
                    )
                }
            }
        }
    }
}

@Composable
private fun FixedNumberChildrenInspector(
    node: ComposeNode,
    childParams: LazyListChildParams.FixedNumber,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    BasicEditableTextProperty(
        initialValue = childParams.numOfItems.toString(),
        label = "Items",
        validateInput = NotEmptyNotLessThanZeroIntValidator()::validate,
        onValidValueChanged = {
            composeNodeCallbacks.onLazyListChildParamsUpdated(
                node,
                childParams.copy(numOfItems = it.toInt()),
            )
        },
        modifier = Modifier.hoverOverlay(),
    )
}

@Composable
private fun DynamicSourceChildrenInspector(
    project: Project,
    node: ComposeNode,
    childParams: LazyListChildParams.DynamicItemsSource,
) {
    val lazyList =
        childParams.getSourceId().let { node.findDependentDynamicItemsHolderOrNull(node, it) }
            ?: return
    childParams.getNumOfItems(project, lazyList)
    BasicEditableTextProperty(
        initialValue = "${
            childParams.getNumOfItems(
                project,
                lazyList = lazyList,
            )
        } [set from ${lazyList.labelExposed}]",
        label = "Items",
        enabled = false,
        onValidValueChanged = {},
        modifier = Modifier.hoverOverlay(),
    )
}
