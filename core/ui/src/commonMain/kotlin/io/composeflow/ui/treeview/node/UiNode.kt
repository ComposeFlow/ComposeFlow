package io.composeflow.ui.treeview.node

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.composeflow.ui.treeview.TreeViewScope
import io.composeflow.ui.treeview.getNodeBackgroundColor

@Composable
fun <T> TreeViewScope<T>.NodeWithLines(
    node: Node<T>,
    index: Int,
    nodes: List<Node<T>>,
) {
    val density = LocalDensity.current

    with(density) {
        val depth = node.depth
        val indentWidth = 16.dp.toPx()
        val strokeColor = style.colors.stroke
        val strokeWidth = 1.dp.toPx()
        val toggleIconHalfSize = style.toggleIconSize.toPx()

        val nextNodes = nodes.drop(index + 1)

        Box(
            modifier = Modifier.fillMaxWidth().height(24.dp), // move to style
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val centerY = size.height / 2

                for (level in 0 until depth) {
                    val hasAncestorSiblingAtLevel = nextNodes.any { it.depth == level }

                    if (hasAncestorSiblingAtLevel) {
                        val x = level * indentWidth
                        drawLine(
                            color = strokeColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = strokeWidth,
                        )
                    }
                }

                val hasNextSibling =
                    nextNodes.takeWhile { it.depth >= depth }.any { it.depth == depth }
                val xLine = depth * indentWidth

                drawLine(
                    color = strokeColor,
                    start = Offset(xLine, 0f),
                    end = Offset(xLine, if (hasNextSibling) size.height else centerY),
                    strokeWidth = strokeWidth,
                )

                val endLineX =
                    if (node is LeafNode<T>) {
                        xLine + indentWidth + toggleIconHalfSize
                    } else {
                        xLine + indentWidth / 2 + (toggleIconHalfSize / 3)
                    }
                drawLine(
                    color = strokeColor,
                    start = Offset(xLine, centerY),
                    end = Offset(endLineX, centerY),
                    strokeWidth = strokeWidth,
                )
            }

            Node(
                node = node,
            )
        }
    }
}

@Composable
internal fun <T> TreeViewScope<T>.Node(node: Node<T>) {
    val backgroundColor = getNodeBackgroundColor(node)
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        onHover?.invoke(node, isHovered)
    }

    Box(
        modifier =
            Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
                .background(backgroundColor, style.nodeShape)
                .then(clickableNode(node, interactionSource))
                .hoverable(interactionSource),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .padding(vertical = 1.dp)
                    .padding(start = node.depth * style.toggleIconSize),
        ) {
            ToggleIcon(node)
            NodeContent(node)
        }
    }
}

@Composable
private fun <T> TreeViewScope<T>.ToggleIcon(node: Node<T>) {
    val toggleIcon = style.toggleIcon(node) ?: return

    if (node is BranchNode) {
        val rotationDegrees by animateFloatAsState(
            if (node.isExpanded) style.toggleIconRotationDegrees else 0f,
        )

        Image(
            painter = toggleIcon,
            contentDescription = if (node.isExpanded) "Collapse node" else "Expand node",
            colorFilter = style.toggleIconColorFilter,
            modifier =
                Modifier
                    .clip(style.toggleShape)
                    .clickable { expandableManager.toggleExpansion(node) }
                    .size(style.toggleIconSize)
                    .requiredSize(style.toggleIconSize)
                    .rotate(rotationDegrees),
        )
    } else {
        Spacer(Modifier.size(style.nodeIconSize))
    }
}

@Composable
private fun <T> TreeViewScope<T>.NodeContent(node: Node<T>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(style.nodePadding),
    ) {
        with(node) {
            iconComponent(node)
            nameComponent(node)
        }
    }
}

// TODO change this and implement long and double click
fun <T> TreeViewScope<T>.clickableNode(
    node: Node<T>,
    interactionSource: MutableInteractionSource,
): Modifier =
    Modifier
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {},
        ).pointerInput(node.key) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Press) {
                        val isCtrlPressed = event.keyboardModifiers.isCtrlPressed
                        val isShiftPressed = event.keyboardModifiers.isShiftPressed

                        onClick?.invoke(node, isCtrlPressed, isShiftPressed)
                    }
                }
            }
        }

@Composable
internal fun <T> TreeViewScope<T>.DefaultNodeIcon(node: Node<T>) {
    val (icon, colorFilter) =
        if (node is BranchNode && node.isExpanded) {
            style.nodeExpandedIcon(node) to style.nodeExpandedIconColorFilter
        } else {
            style.nodeCollapsedIcon(node) to style.nodeCollapsedIconColorFilter
        }

    if (icon != null) {
        Image(
            painter = icon,
            colorFilter = colorFilter,
            contentDescription = node.name,
        )
    }
}

@Composable
internal fun <T> TreeViewScope<T>.DefaultNodeName(node: Node<T>) {
    BasicText(
        text = node.name,
        style = style.nodeNameTextStyle,
        modifier = Modifier.padding(start = style.nodeNameStartPadding),
    )
}
