package io.composeflow.ui.treeview.node

import androidx.compose.runtime.Composable
import io.composeflow.ui.treeview.TreeViewScope
import io.composeflow.ui.treeview.node.extension.ExpandableNode
import io.composeflow.ui.treeview.node.extension.ExpandableNodeHandler
import io.composeflow.ui.treeview.node.extension.HoverableNode
import io.composeflow.ui.treeview.node.extension.HoverableNodeHandler
import io.composeflow.ui.treeview.node.extension.SelectableNode
import io.composeflow.ui.treeview.node.extension.SelectableNodeHandler
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias NodeComponent<T> = @Composable TreeViewScope<T>.(Node<T>) -> Unit

sealed interface Node<T> {
    val key: String

    val content: T

    val name: String

    val depth: Int

    val isSelected: Boolean
    val isHovered: Boolean

    val iconComponent: NodeComponent<T>

    val nameComponent: NodeComponent<T>
}

class LeafNode<T>
    @OptIn(ExperimentalUuidApi::class)
    internal constructor(
        override val content: T,
        override val depth: Int,
        override val key: String = Uuid.random().toString(),
        override val name: String = content.toString(),
        override val iconComponent: NodeComponent<T> = { DefaultNodeIcon(it) },
        override val nameComponent: NodeComponent<T> = { DefaultNodeName(it) },
    ) : Node<T>,
        SelectableNode by SelectableNodeHandler(),
        HoverableNode by HoverableNodeHandler()


class BranchNode<T>
    @OptIn(ExperimentalUuidApi::class)
    internal constructor(
        override val content: T,
        override val depth: Int,
        override val key: String = Uuid.random().toString(),
        override val name: String = content.toString(),
        override val iconComponent: NodeComponent<T> = { DefaultNodeIcon(it) },
        override val nameComponent: NodeComponent<T> = { DefaultNodeName(it) },
    ) : Node<T>,
        SelectableNode by SelectableNodeHandler(),
        ExpandableNode by ExpandableNodeHandler(),
        HoverableNode by HoverableNodeHandler()
