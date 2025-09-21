package io.composeflow.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.IntSize
import io.composeflow.model.palette.PaletteDraggable
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode

data class CanvasNodeCallbacks(
    val onBoundsInNodeUpdated: (ComposeNode, Rect) -> Unit,
    val onDraggedNodeUpdated: (ComposeNode?) -> Unit,
    val onDraggedPositionUpdated: (Offset, PaletteDraggable) -> Unit,
    val onDragEnd: () -> Unit,
    val onNodeDropToPosition: suspend (Offset, ComposeNode) -> EventResult,
    val onKeyPressed: (KeyEvent) -> EventResult,
    val onDoubleTap: (ComposeNode) -> EventResult,
    val onPopEditedComponent: () -> Unit,
    val onUndo: () -> Unit,
    val onRedo: () -> Unit,
    val onCopyFocusedNode: () -> EventResult,
    val onPaste: () -> EventResult,
    val onDeleteFocusedNode: () -> EventResult,
    val onBringToFront: () -> Unit,
    val onSendToBack: () -> Unit,
    val onPendingHeightModifierCommitted: (ComposeNode) -> Unit,
    val onPendingWidthModifierCommitted: (ComposeNode) -> Unit,
    val onConvertToComponent: (String, ComposeNode) -> Unit,
    val onFormFactorChanged: (FormFactor) -> Unit,
    val onUiBuilderCanvasSizeChanged: (IntSize) -> Unit,
)

val emptyCanvasNodeCallbacks =
    CanvasNodeCallbacks(
        onBoundsInNodeUpdated = { _, _ -> },
        onDraggedNodeUpdated = {},
        onDraggedPositionUpdated = { _, _ -> },
        onDragEnd = {},
        onNodeDropToPosition = { _, _ -> EventResult() },
        onKeyPressed = { _ -> EventResult() },
        onDoubleTap = { _ -> EventResult() },
        onPopEditedComponent = {},
        onUndo = {},
        onRedo = {},
        onCopyFocusedNode = { EventResult() },
        onPaste = { EventResult() },
        onDeleteFocusedNode = { EventResult() },
        onBringToFront = {},
        onSendToBack = {},
        onPendingHeightModifierCommitted = {},
        onPendingWidthModifierCommitted = {},
        onConvertToComponent = { _, _ -> },
        onFormFactorChanged = {},
        onUiBuilderCanvasSizeChanged = {},
    )
