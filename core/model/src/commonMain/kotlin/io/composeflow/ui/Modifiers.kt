package io.composeflow.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.composeflow.model.modifier.sumPadding
import io.composeflow.model.palette.PaletteDraggable
import io.composeflow.model.palette.PaletteNodeCallbacks
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.parameter.BottomAppBarTrait
import io.composeflow.model.parameter.BoxTrait
import io.composeflow.model.parameter.ColumnTrait
import io.composeflow.model.parameter.HorizontalPagerTrait
import io.composeflow.model.parameter.LazyColumnTrait
import io.composeflow.model.parameter.LazyHorizontalGridTrait
import io.composeflow.model.parameter.LazyRowTrait
import io.composeflow.model.parameter.LazyVerticalGridTrait
import io.composeflow.model.parameter.NavigationDrawerTrait
import io.composeflow.model.parameter.RowTrait
import io.composeflow.model.parameter.ScreenTrait
import io.composeflow.model.parameter.TabContentTrait
import io.composeflow.model.parameter.TopAppBarTrait
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import io.composeflow.ui.zoomablecontainer.calculateAdjustedBoundsInZoomableContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

const val DEVICE_CANVAS_TEST_TAG = "DeviceCanvas"

/**
 * Modifier used for a Composable dropped in the canvas.
 *
 * @param node the [ComposeNode] representing the Composable
 * @param isDraggable set to false if this node isn't draggable
 * @param zoomableContainerStateHolder the [ZoomableContainerStateHolder] used to control the zoom and offset of the canvas.
 */
fun Modifier.modifierForCanvas(
    project: Project,
    node: ComposeNode,
    canvasNodeCallbacks: CanvasNodeCallbacks,
    zoomableContainerStateHolder: ZoomableContainerStateHolder,
    paletteRenderParams: PaletteRenderParams = PaletteRenderParams(),
    isDraggable: Boolean = true,
) = composed {
    val coroutineScope = rememberCoroutineScope()
    if (paletteRenderParams.isShadowNode) {
        Modifier.alpha(0.6f)
    } else if (node.isPartOfComponent) {
        // If the node is a child of any component, make the node as non-editable and non-draggable
        // since it's defined in another place where it's used.
        Modifier
    } else {
        val borderShape =
            if (node.trait.value is TopAppBarTrait) {
                RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)
            } else if (node.trait.value is BottomAppBarTrait) {
                RectangleShape
            } else if (node.trait.value is ScreenTrait) {
                RoundedCornerShape(16.dp)
            } else if (node.isRoot()) {
                val topCorner = 16.dp to 16.dp

                val bottomCorner =
                    if (project.screenHolder.showNavigation.value) {
                        0.dp to 0.dp
                    } else {
                        16.dp to 16.dp
                    }
                RoundedCornerShape(
                    topCorner.first,
                    topCorner.second,
                    bottomCorner.second,
                    bottomCorner.first,
                )
            } else {
                RectangleShape
            }

        Modifier
            .testTag("$DEVICE_CANVAS_TEST_TAG/" + node.displayName(project))
            .onGloballyPositioned {
                canvasNodeCallbacks.onBoundsInNodeUpdated(
                    node,
                    it
                        .boundsInWindow()
                        .calculateAdjustedBoundsInZoomableContainer(zoomableContainerStateHolder),
                )
            }.drawComposableLabel(project, node)
            .drawDropIndicator(node)
            .drawBorder(
                node,
                shape = borderShape,
                paletteRenderParams = paletteRenderParams,
            ).drawPadding(node)
            .drawOverlay(project, node)
            .dragHandlerAndTapGestures(
                node = node,
                canvasNodeCallbacks = canvasNodeCallbacks,
                coroutineScope = coroutineScope,
                isDraggable = isDraggable,
            )
    }
}

@Composable
private fun Modifier.drawOverlay(
    project: Project,
    node: ComposeNode,
    focusedOverlayColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    errorColor: Color = MaterialTheme.colorScheme.error,
): Modifier =
    composed {
        if (node.generateTrackableIssues(project).isNotEmpty()) {
            border(
                width = 1.dp,
                color = errorColor.copy(0.6f),
            ).drawWithContent {
                drawContent()
                drawRect(errorColor.copy(alpha = 0.2f))
            }
        } else if (node.isFocused.value) {
            Modifier.drawWithContent {
                drawContent()
                drawRect(focusedOverlayColor.copy(alpha = 0.1f))
            }
        } else {
            Modifier
        }
    }

private fun Modifier.dragHandlerAndTapGestures(
    node: ComposeNode,
    canvasNodeCallbacks: CanvasNodeCallbacks,
    coroutineScope: CoroutineScope,
    isDraggable: Boolean = true,
): Modifier =
    composed {
        val density = LocalDensity.current
        with(density) {
            val handleSize = 30.dp.toPx()
            val width = node.boundsInWindow.value.width
            val height = node.boundsInWindow.value.height
            val handleRightLongSide by remember(height) {
                derivedStateOf {
                    if (height >= handleSize) {
                        handleSize
                    } else {
                        height * 0.75f
                    }
                }
            }
            val handleBottomLongSide by remember(width) {
                derivedStateOf {
                    if (width >= handleSize) {
                        handleSize
                    } else {
                        width * 0.75f
                    }
                }
            }
            val handleShortSide = 6.dp.toPx()
            var onBottomDragHandler by remember { mutableStateOf(false) }
            var onRightDragHandler by remember { mutableStateOf(false) }
            var isHeightResizing by remember { mutableStateOf(false) }
            var isWidthResizing by remember { mutableStateOf(false) }

            val resizeHandlerColor = MaterialTheme.colorScheme.tertiary
            val resizeLabelColor = MaterialTheme.colorScheme.tertiaryContainer
            val borderWidth = 1.dp.toPx()
            val cornerRadius = CornerRadius(x = 4.dp.toPx(), y = 4.dp.toPx())

            val borderModifier =
                if (!(node.isRoot() || node.isContentRoot()) &&
                    node.trait.value.isResizeable() &&
                    node.isFocused.value
                ) {
                    Modifier
                        .drawWithContent {
                            // Draw the content first
                            drawContent()

                            drawRoundRect(
                                color = resizeHandlerColor,
                                topLeft =
                                    size.center.copy(
                                        x = size.center.x - handleBottomLongSide / 2,
                                        y = size.height - borderWidth - handleShortSide / 2,
                                    ),
                                size =
                                    size.copy(
                                        width = handleBottomLongSide,
                                        height = handleShortSide,
                                    ),
                                cornerRadius = cornerRadius,
                            )
                            drawRoundRect(
                                color = resizeHandlerColor,
                                topLeft =
                                    size.center.copy(
                                        x = size.width - handleShortSide / 2,
                                        y = size.center.y - handleRightLongSide / 2,
                                    ),
                                size =
                                    size.copy(
                                        width = handleShortSide,
                                        height = handleRightLongSide,
                                    ),
                                cornerRadius = cornerRadius,
                            )
                        }.then(
                            Modifier
                                .onPointerEvent(PointerEventType.Move) {
                                    val position = it.changes.first().position
                                    if (position.x >= size.width / 2 - handleBottomLongSide / 2 &&
                                        position.x <= size.width / 2 + handleBottomLongSide / 2 &&
                                        position.y >= size.height - handleShortSide / 2
                                    ) {
                                        // When the mouse cursor is on the bottom drag handler
                                        onBottomDragHandler = true
                                        onRightDragHandler = false
                                    } else if (
                                        position.y >= size.height / 2 - handleRightLongSide / 2 &&
                                        position.y <= size.height / 2 + handleRightLongSide / 2 &&
                                        position.x >= size.width - handleShortSide / 2
                                    ) {
                                        // When the mouse cursor is on the right drag handler
                                        onBottomDragHandler = false
                                        onRightDragHandler = true
                                    } else {
                                        onBottomDragHandler = false
                                        onRightDragHandler = false
                                    }
                                }.onPointerEvent(PointerEventType.Exit) {
                                    onBottomDragHandler = false
                                    onRightDragHandler = false
                                },
                        )
                } else {
                    Modifier
                }

            // Assigning separate pointerInput modifiers seem to override the other one. So we
            // assign te dragging and resizing behaviors in the same pointerInput modifier
            // each is identified by the area where the drag is originated even though
            // it raises the complexity of the method.
            val dragModifier =
                if (
                    !(node.isRoot() || node.isContentRoot()) && isDraggable
                ) {
                    var pointerPosition by remember { mutableStateOf(Offset.Zero) }
                    var isDragging by remember { mutableStateOf(false) }
                    // If the rendered node isn't root, make it draggable

                    fun isPositionCloseToEdge(
                        size: IntSize,
                        offset: Offset,
                        threshold: Float = handleShortSide + 2.dp.toPx(),
                    ): Boolean =
                        offset.x <= threshold ||
                            offset.x >= size.width - threshold ||
                            offset.y <= threshold ||
                            offset.y >= size.height - threshold

                    val onShowSnackbar = LocalOnShowSnackbar.current
                    Modifier
                        // Assigning pointerInput for tapGestures in the same modifier chain because
                        // assigning it as a different modifier introduces an undesired keyEvent behaviour
                        .pointerInput("tapGestures") {
                            detectTapGestures(
                                onDoubleTap = {
                                    canvasNodeCallbacks.onDoubleTap(node)
                                },
                            )
                        }.pointerInput("dragGesture") {
                            detectDragGestures(
                                onDragStart = {
                                    pointerPosition = it
                                    if (!isPositionCloseToEdge(size = size, offset = it)) {
                                        isDragging = true
                                        canvasNodeCallbacks.onDraggedNodeUpdated(node)
                                    } else if (onBottomDragHandler || isHeightResizing) {
                                        isHeightResizing = true
                                    } else if (onRightDragHandler || isWidthResizing) {
                                        isWidthResizing = true
                                    }
                                },
                                onDrag = { _, dragAmount ->
                                    pointerPosition += dragAmount

                                    val padding = node.modifierList.sumPadding()
                                    if (isDragging) {
                                        canvasNodeCallbacks.onDraggedPositionUpdated(
                                            pointerPosition + node.boundsInWindow.value.topLeft,
                                            node.trait.value,
                                        )
                                    } else if (onBottomDragHandler || isHeightResizing) {
                                        node.resizeHeightAsPending(
                                            deltaY = dragAmount.y,
                                            currentHeight = size.height.toFloat(),
                                            paddingHeight = padding.top.toPx() + padding.bottom.toPx(),
                                            density = density,
                                            parentHeight =
                                                node.parentNode
                                                    ?.boundsInWindow
                                                    ?.value
                                                    ?.height,
                                        )
                                    } else if (onRightDragHandler || isWidthResizing) {
                                        node.resizeWidthAsPending(
                                            deltaX = dragAmount.x,
                                            currentWidth = size.width.toFloat(),
                                            paddingWidth = padding.start.toPx() + padding.end.toPx(),
                                            density = density,
                                            parentWidth =
                                                node.parentNode
                                                    ?.boundsInWindow
                                                    ?.value
                                                    ?.width,
                                        )
                                    }
                                },
                                onDragEnd = {
                                    if (isDragging) {
                                        val eventResult =
                                            canvasNodeCallbacks.onNodeDropToPosition(
                                                pointerPosition + node.boundsInWindow.value.topLeft,
                                                node,
                                            )
                                        eventResult.errorMessages.forEach {
                                            coroutineScope.launch {
                                                onShowSnackbar(it, null)
                                            }
                                        }
                                    } else if (onBottomDragHandler || isHeightResizing) {
                                        canvasNodeCallbacks.onPendingHeightModifierCommitted(node)
                                    } else if (onRightDragHandler || isWidthResizing) {
                                        canvasNodeCallbacks.onPendingWidthModifierCommitted(node)
                                    }

                                    canvasNodeCallbacks.onDraggedNodeUpdated(null)
                                    canvasNodeCallbacks.onDragEnd()
                                    isDragging = false
                                    isHeightResizing = false
                                    isWidthResizing = false
                                },
                                onDragCancel = {
                                    isDragging = false
                                    isHeightResizing = false
                                    isWidthResizing = false

                                    canvasNodeCallbacks.onDraggedNodeUpdated(null)
                                    canvasNodeCallbacks.onDragEnd()
                                },
                            )
                        }
                } else {
                    Modifier
                }

            val labelHeight = 16.dp
            val resizeLabelModifier =
                if (onBottomDragHandler || isHeightResizing) {
                    val textMeasure = rememberTextMeasurer()
                    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                    textLayoutResult =
                        textMeasure.measure(
                            buildAnnotatedString {
                                withStyle(
                                    style =
                                        SpanStyle(
                                            fontSize = 12.sp,
                                        ),
                                ) {
                                    append(node.heightDecider())
                                }
                            },
                        )
                    val textColor = MaterialTheme.colorScheme.onBackground
                    Modifier.drawWithContent {
                        val lineMargin = 16.dp
                        drawContent()
                        drawLine(
                            color = resizeLabelColor,
                            strokeWidth = 2.dp.toPx(),
                            start = size.center.copy(x = lineMargin.toPx(), y = 4.dp.toPx()),
                            end =
                                size.center.copy(
                                    x = lineMargin.toPx(),
                                    y = size.height - 4.dp.toPx(),
                                ),
                        )
                        val left = center.x - size.width / 2f
                        val top = center.y - size.height / 2f
                        textLayoutResult?.let {
                            val labelRectTopLeft = Offset(left, top + labelHeight.toPx())
                            withTransform({
                                rotate(degrees = -90f, pivot = labelRectTopLeft)
                            }) {
                                drawRect(
                                    color = resizeLabelColor,
                                    topLeft =
                                        labelRectTopLeft.copy(
                                            x = labelRectTopLeft.x - size.height / 2 - it.size.width / 2 + 8.dp.toPx(),
                                            y = labelRectTopLeft.y + lineMargin.toPx() / 2,
                                        ),
                                    size = Size(it.size.width + 16.dp.toPx(), labelHeight.toPx()),
                                )
                                drawText(
                                    textLayoutResult = it,
                                    color = textColor,
                                    topLeft =
                                        Offset(
                                            x = left + 8.dp.toPx() - size.height / 2 - it.size.width / 2 + 8.dp.toPx(),
                                            y = top + labelHeight.toPx() + lineMargin.toPx() / 2,
                                        ),
                                )
                            }
                        }
                    }
                } else if (onRightDragHandler || isWidthResizing) {
                    val textMeasure = rememberTextMeasurer()
                    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                    textLayoutResult =
                        textMeasure.measure(
                            buildAnnotatedString {
                                withStyle(
                                    style =
                                        SpanStyle(
                                            fontSize = 12.sp,
                                        ),
                                ) {
                                    append(node.widthDecider())
                                }
                            },
                        )
                    val textColor = MaterialTheme.colorScheme.onBackground
                    Modifier.drawWithContent {
                        val lineMargin = 16.dp
                        drawContent()
                        drawLine(
                            color = resizeLabelColor,
                            strokeWidth = 2.dp.toPx(),
                            start = size.center.copy(x = 4.dp.toPx(), y = lineMargin.toPx()),
                            end =
                                size.center.copy(
                                    x = size.width - 4.dp.toPx(),
                                    y = lineMargin.toPx(),
                                ),
                        )
                        val left = center.x - size.width / 2f
                        val top = center.y - size.height / 2f

                        textLayoutResult?.let {
                            drawRect(
                                color = resizeLabelColor,
                                topLeft =
                                    Offset(
                                        x = left + size.width / 2 - it.size.width / 2,
                                        y = top + lineMargin.toPx() / 2,
                                    ),
                                size = Size(it.size.width + 16.dp.value, labelHeight.toPx()),
                            )
                            drawText(
                                textLayoutResult = it,
                                color = textColor,
                                topLeft =
                                    Offset(
                                        x = left + size.width / 2 - it.size.width / 2 + 4.dp.toPx(),
                                        y = top + lineMargin.toPx() / 2,
                                    ),
                            )
                        }
                    }
                } else {
                    Modifier
                }

            borderModifier
                .then(resizeLabelModifier)
                .then(dragModifier)
                .then(
                    if (onRightDragHandler || isWidthResizing) {
                        pointerHoverIcon(PointerIconResizeHorizontal)
                    } else if (onBottomDragHandler || isHeightResizing) {
                        pointerHoverIcon(PointerIconResizeVertical)
                    } else {
                        Modifier
                    },
                )
        }
    }

private fun Modifier.drawDropIndicator(node: ComposeNode): Modifier =
    composed {
        val dropIndicatorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        if (node.isContainer() && node.isDraggedOnBounds.value) {
            drawWithContent {
                drawContent()
                if (node.trait.value is ColumnTrait ||
                    node.trait.value is TabContentTrait ||
                    node.trait.value is NavigationDrawerTrait
                ) {
                    when (node.indexToBeDropped.value) {
                        0 -> {
                            drawCircle(
                                color = dropIndicatorColor,
                                radius = 30f,
                                center =
                                    this.center -
                                        Offset(
                                            0f,
                                            node.boundsInWindow.value.height / 2.0f,
                                        ),
                            )
                        }

                        else -> {
                            if (node.indexToBeDropped.value >= node.children.size) {
                                drawCircle(
                                    color = dropIndicatorColor,
                                    radius = 30f,
                                    center =
                                        this.center +
                                            Offset(
                                                0f,
                                                node.boundsInWindow.value.height / 2.0f,
                                            ),
                                )
                            } else {
                                val beforeChild = node.children[node.indexToBeDropped.value - 1]
                                val afterChild = node.children[node.indexToBeDropped.value]
                                val centerY =
                                    (
                                        beforeChild.boundsInWindow.value.bottom +
                                            afterChild.boundsInWindow.value.top
                                    ) / 2.0f
                                drawCircle(
                                    color = dropIndicatorColor,
                                    radius = 30f,
                                    center =
                                        Offset(
                                            x = this.center.x,
                                            y = centerY - node.boundsInWindow.value.top,
                                        ),
                                )
                            }
                        }
                    }
                } else if (node.trait.value is RowTrait || node.trait.value is HorizontalPagerTrait) {
                    when (node.indexToBeDropped.value) {
                        0 -> {
                            drawCircle(
                                color = dropIndicatorColor,
                                radius = 30f,
                                center =
                                    this.center -
                                        Offset(
                                            node.boundsInWindow.value.width / 2.0f,
                                            0f,
                                        ),
                            )
                        }

                        else -> {
                            if (node.indexToBeDropped.value >= node.children.size) {
                                drawCircle(
                                    color = dropIndicatorColor,
                                    radius = 30f,
                                    center =
                                        this.center +
                                            Offset(
                                                node.boundsInWindow.value.width / 2.0f,
                                                0f,
                                            ),
                                )
                            } else {
                                val beforeChild = node.children[node.indexToBeDropped.value - 1]
                                val afterChild = node.children[node.indexToBeDropped.value]
                                val centerX =
                                    (
                                        beforeChild.boundsInWindow.value.right +
                                            afterChild.boundsInWindow.value.left
                                    ) / 2.0f
                                drawCircle(
                                    color = dropIndicatorColor,
                                    radius = 30f,
                                    center =
                                        Offset(
                                            x = centerX - node.boundsInWindow.value.left,
                                            y = this.center.y,
                                        ),
                                )
                            }
                        }
                    }
                } else if (node.trait.value is BoxTrait) {
                    drawCircle(
                        color = dropIndicatorColor,
                        radius = 30f,
                        center =
                            this.center -
                                Offset(
                                    node.boundsInWindow.value.width / 2.0f,
                                    node.boundsInWindow.value.height / 2.0f,
                                ),
                    )
                } else if (
                    node.trait.value is LazyColumnTrait ||
                    node.trait.value is LazyVerticalGridTrait
                ) {
                    when (node.indexToBeDropped.value) {
                        0 -> {
                            drawCircle(
                                color = dropIndicatorColor,
                                radius = 30f,
                                center =
                                    this.center -
                                        Offset(
                                            0f,
                                            node.boundsInWindow.value.height / 2.0f,
                                        ),
                            )
                        }

                        else -> {
                            if (node.indexToBeDropped.value >= node.children.size) {
                                drawCircle(
                                    color = dropIndicatorColor,
                                    radius = 30f,
                                    center =
                                        this.center +
                                            Offset(
                                                0f,
                                                node.boundsInWindow.value.height / 2.0f,
                                            ),
                                )
                            } else {
                                val afterChild = node.children[node.indexToBeDropped.value]
                                drawCircle(
                                    color = dropIndicatorColor,
                                    radius = 30f,
                                    center =
                                        Offset(
                                            x = this.center.x,
                                            y =
                                                afterChild.boundsInWindow.value.top -
                                                    node.boundsInWindow.value.top,
                                        ),
                                )
                            }
                        }
                    }
                } else if (
                    node.trait.value is LazyRowTrait ||
                    node.trait.value is LazyHorizontalGridTrait
                ) {
                    when (node.indexToBeDropped.value) {
                        0 -> {
                            drawCircle(
                                color = dropIndicatorColor,
                                radius = 30f,
                                center =
                                    this.center -
                                        Offset(
                                            x = node.boundsInWindow.value.width / 2.0f,
                                            y = 0f,
                                        ),
                            )
                        }

                        else -> {
                            if (node.indexToBeDropped.value >= node.children.size) {
                                drawCircle(
                                    color = dropIndicatorColor,
                                    radius = 30f,
                                    center =
                                        this.center +
                                            Offset(
                                                x = node.boundsInWindow.value.width / 2.0f,
                                                y = 0f,
                                            ),
                                )
                            } else {
                                val afterChild = node.children[node.indexToBeDropped.value]
                                drawCircle(
                                    color = dropIndicatorColor,
                                    radius = 30f,
                                    center =
                                        Offset(
                                            x =
                                                afterChild.boundsInWindow.value.left -
                                                    node.boundsInWindow.value.left,
                                            y = this.center.y,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Modifier
        }
    }

fun Modifier.drawComposableLabel(
    project: Project,
    node: ComposeNode,
) = composed {
    if (node.isFocused.value || node.isHovered.value) {
        val textMeasure = rememberTextMeasurer()
        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
        textLayoutResult =
            textMeasure.measure(
                buildAnnotatedString {
                    withStyle(
                        style =
                            SpanStyle(
                                fontSize = 12.sp,
                            ),
                    ) {
                        append(node.displayName(project))
                    }
                },
            )
        val textColor =
            if (node.isFocused.value) {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            }
        val labelRectColor =
            if (node.isFocused.value) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
            }
        drawWithContent {
            drawContent()
            val left = center.x - size.width / 2f
            val top = center.y - size.height / 2f
            val labelHeight = 16.dp
            if (node.showInvertedLabel(labelHeight.toPx())) {
                textLayoutResult?.let {
                    drawRect(
                        color = labelRectColor,
                        topLeft = Offset(left, top),
                        size = Size(it.size.width + 8.dp.toPx(), labelHeight.toPx()),
                    )
                    drawText(
                        textLayoutResult = it,
                        color = textColor,
                        topLeft = Offset(left + 4.dp.toPx(), top),
                    )
                }
            } else {
                textLayoutResult?.let {
                    drawRect(
                        color = labelRectColor,
                        topLeft = Offset(left, top - labelHeight.toPx()),
                        size = Size(it.size.width + 8.dp.toPx(), labelHeight.toPx()),
                    )
                    drawText(
                        textLayoutResult = it,
                        color = textColor,
                        topLeft = Offset(left + 4.dp.toPx(), top - labelHeight.toPx()),
                    )
                }
            }
        }
    } else {
        Modifier
    }
}

@Composable
fun Modifier.drawLabel(
    labelName: String,
    textColor: Color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
    labelRectColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
) = composed {
    val textMeasure = rememberTextMeasurer()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    textLayoutResult =
        textMeasure.measure(
            buildAnnotatedString {
                withStyle(
                    style =
                        SpanStyle(
                            fontSize = 12.sp,
                        ),
                ) {
                    append(labelName)
                }
            },
        )
    drawWithContent {
        drawContent()
        val left = center.x - size.width / 2f
        val top = center.y - size.height / 2f
        val labelHeight = 16.dp
        textLayoutResult?.let {
            drawRect(
                color = labelRectColor,
                topLeft = Offset(left, top - labelHeight.toPx()),
                size = Size(it.size.width + 8.dp.toPx(), labelHeight.toPx()),
            )
            drawText(
                textLayoutResult = it,
                color = textColor,
                topLeft = Offset(left + 4.dp.toPx(), top - labelHeight.toPx()),
            )
        }
    }
}

fun Modifier.drawBorder(
    node: ComposeNode,
    shape: Shape = RectangleShape,
    paletteRenderParams: PaletteRenderParams,
) = composed {
    if (node.isDraggedOnBounds.value) {
        border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
            shape = shape,
        )
    } else if (node.isFocused.value) {
        Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
            shape = shape,
        )
    } else if (node.isHovered.value) {
        border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
            shape = shape,
        )
    } else if (paletteRenderParams.showBorder) {
        dashedBorder(
            color = MaterialTheme.colorScheme.outline,
            strokeWidth = 0.5.dp,
        )
    } else {
        Modifier
    }
}

fun Modifier.dashedBorder(
    color: Color,
    shape: Shape = RectangleShape,
    strokeWidth: Dp = 2.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp,
    cap: StrokeCap = StrokeCap.Round,
) = this.drawWithContent {
    val outline = shape.createOutline(size, layoutDirection, density = this)

    val dashedStroke =
        Stroke(
            cap = cap,
            width = strokeWidth.toPx(),
            pathEffect =
                PathEffect.dashPathEffect(
                    intervals = floatArrayOf(dashLength.toPx(), gapLength.toPx()),
                ),
        )
    drawContent()

    drawOutline(
        outline = outline,
        style = dashedStroke,
        brush = SolidColor(color),
    )
}

fun Modifier.drawPadding(node: ComposeNode) =
    composed {
        val color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        val padding = node.modifierList.sumPadding()

        if (node.isFocused.value || node.isHovered.value) {
            drawWithContent {
                drawContent()
                val left = center.x - size.width / 2f
                val top = center.y - size.height / 2f
                val right = center.x + size.width / 2f
                val bottom = center.y + size.height / 2f
                // Left padding
                drawRect(
                    color = color,
                    topLeft = Offset(top, left) - Offset(padding.start.toPx(), 0f),
                    size = Size(padding.start.toPx(), size.height.dp.value),
                )
                // Top padding
                drawRect(
                    color = color,
                    topLeft = Offset(top, left) - Offset(0f, padding.top.toPx()),
                    size = Size(size.width.dp.value, padding.top.toPx()),
                )
                // Right padding
                drawRect(
                    color = color,
                    topLeft = Offset(right, top),
                    size = Size(padding.end.toPx(), size.height.dp.value),
                )
                // Bottom padding
                drawRect(
                    color = color,
                    topLeft = Offset(left, bottom),
                    size = Size(size.width.dp.value, padding.bottom.toPx()),
                )
            }
        } else {
            Modifier
        }
    }

fun Modifier.switchByHovered(
    hovered: Modifier = Modifier,
    notHovered: Modifier = Modifier,
): Modifier =
    composed {
        var isHovered by remember { mutableStateOf(false) }

        onPointerEvent(PointerEventType.Enter) {
            isHovered = true
        }.onPointerEvent(PointerEventType.Exit) {
            isHovered = false
        }.then(
            if (isHovered) {
                hovered
            } else {
                notHovered
            },
        )
    }

fun Modifier.mousePointerEvents(
    node: ComposeNode,
    onFocusedStatusUpdated: (ComposeNode) -> Unit,
    onHoveredStatusUpdated: (ComposeNode, Boolean) -> Unit,
) = composed {
    onPointerEvent(PointerEventType.Enter) {
        onHoveredStatusUpdated(node, true)
    }.onPointerEvent(PointerEventType.Exit) {
        onHoveredStatusUpdated(node, false)
    }.onPointerEvent(PointerEventType.Press) {
        onFocusedStatusUpdated(node)
    }
}

fun Modifier.draggableFromPalette(
    project: Project,
    paletteNodeCallbacks: PaletteNodeCallbacks,
    paletteDraggable: PaletteDraggable,
    zoomableContainerStateHolder: ZoomableContainerStateHolder,
) = composed {
    // Pointer position relative to container where the drag event is dispatched
    var pointerPosition by remember { mutableStateOf(Offset.Zero) }
    var localToRoot by mutableStateOf(Offset.Zero)
    val coroutineScope = rememberCoroutineScope()

    pointerInput(Unit) {
        detectTapGestures(
            onPress = { offset ->
                pointerPosition = offset
            },
        )
    }.pointerInput(Unit) {
        detectDragGestures(
            onDrag = { change, dragAmount ->
                pointerPosition =
                    Offset(
                        pointerPosition.x + dragAmount.x,
                        pointerPosition.y + dragAmount.y,
                    )
                change.consume()
                paletteNodeCallbacks.onDraggedPositionUpdated(
                    localToRoot + (pointerPosition - zoomableContainerStateHolder.offset) / zoomableContainerStateHolder.scale,
                    paletteDraggable,
                )
            },
            onDragStart = {
                paletteNodeCallbacks.onDraggedNodeUpdated(
                    paletteDraggable.defaultComposeNode(project),
                )
            },
            onDragEnd = {
                coroutineScope.launch {
                    paletteDraggable.defaultComposeNode(project)?.let { composeNode ->
                        composeNode.updateComposeNodeReferencesForTrait()

                        paletteNodeCallbacks.onComposableDroppedToTarget(
                            localToRoot + (pointerPosition - zoomableContainerStateHolder.offset) / zoomableContainerStateHolder.scale,
                            composeNode,
                        )
                    }

                    paletteNodeCallbacks.onDraggedNodeUpdated(null)
                    paletteNodeCallbacks.onDragEnd()
                }
            },
        )
    }.onGloballyPositioned {
        localToRoot = it.localToRoot(Offset.Zero)
    }
}
