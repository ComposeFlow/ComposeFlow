package io.composeflow.ui.propertyeditor.variable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.composeflow.model.apieditor.ApiDefinition
import io.composeflow.model.apieditor.JsonWithJsonPath
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.ui.jsonpath.createJsonTreeWithJsonPath
import io.composeflow.ui.popup.PositionCustomizablePopup
import io.composeflow.ui.treeview.TreeView
import io.composeflow.ui.treeview.node.Node
import kotlinx.serialization.json.JsonArray

@Composable
fun JsonElementSelectionDialog(
    apiDefinition: ApiDefinition,
    onCloseDialog: () -> Unit,
    onElementClicked: (AssignableProperty) -> Unit,
    modifier: Modifier = Modifier,
) {
    PositionCustomizablePopup(
        onDismissRequest = onCloseDialog,
    ) {
        Surface(
            modifier = modifier.size(600.dp, 700.dp),
        ) {
            (apiDefinition.exampleJsonResponse?.jsonElement as? JsonArray)
                ?.get(0)
                ?.let { firstItem ->
                    val tree = createJsonTreeWithJsonPath(firstItem.toString())
                    val lazyListState = rememberLazyListState()

                    TreeView(
                        tree = tree,
                        listState = lazyListState,
                        onClick = { node: Node<JsonWithJsonPath>, _, _ ->
                            val element = node.content
                            onElementClicked(
                                StringProperty.ValueByJsonPath(
                                    jsonPath = element.jsonPath,
                                    jsonElement = firstItem,
                                ),
                            )
                            onCloseDialog()
                        },
                    )

                    // Expand root nodes initially
                    LaunchedEffect(tree) {
                        tree.nodes.forEach { node ->
                            if (node.depth == 0) {
                                tree.expandNode(node)
                            }
                        }
                    }
                }
        }
    }
}