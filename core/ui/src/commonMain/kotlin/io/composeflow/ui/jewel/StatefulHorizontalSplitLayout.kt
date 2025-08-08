package io.composeflow.ui.jewel

import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.composeflow.ui.PointerIconResizeHorizontal
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.HorizontalSplitLayout
import kotlin.math.roundToInt

@Immutable
sealed class SplitLayoutState {
    data class DpBased(
        val dividerPosition: Dp,
    ) : SplitLayoutState()

    data class RatioBased(
        val ratio: Float,
    ) : SplitLayoutState()

    companion object {
        val DpSaver: Saver<DpBased, Float> =
            Saver(
                save = { it.dividerPosition.value },
                restore = { DpBased(it.dp) },
            )
        val RatioSaver: Saver<RatioBased, Float> =
            Saver(
                save = { it.ratio },
                restore = { RatioBased(it) },
            )
    }
}

@Composable
fun rememberSplitLayoutState(initialDividerPosition: Dp): SplitLayoutState.DpBased =
    rememberSaveable(
        key = initialDividerPosition.toString(),
        saver = SplitLayoutState.DpSaver,
    ) {
        SplitLayoutState.DpBased(initialDividerPosition)
    }

@Composable
fun rememberSplitLayoutState(initialRatio: Float): SplitLayoutState.RatioBased =
    rememberSaveable(key = initialRatio.toString(), saver = SplitLayoutState.RatioSaver) {
        SplitLayoutState.RatioBased(initialRatio)
    }

@Composable
fun StatefulHorizontalSplitLayout(
    state: SplitLayoutState,
    maxRatio: Float = 1f,
    minRatio: Float = 0f,
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    onDividerPositionChanged: ((Dp) -> Unit)? = null,
) {
    when (state) {
        is SplitLayoutState.DpBased -> {
            HorizontalSplitLayoutWrapper(
                maxRatio = maxRatio,
                minRatio = minRatio,
                initialDividerPosition = state.dividerPosition,
                first = first,
                second = second,
                modifier = modifier,
                onDividerPositionChanged = onDividerPositionChanged,
            )
        }

        is SplitLayoutState.RatioBased -> {
            BoxWithConstraints(modifier = modifier) {
                val dividerPosition = maxWidth * state.ratio
                HorizontalSplitLayoutWrapper(
                    maxRatio = maxRatio,
                    minRatio = minRatio,
                    initialDividerPosition = dividerPosition,
                    first = first,
                    second = second,
                    modifier = Modifier.fillMaxSize(),
                    onDividerPositionChanged = onDividerPositionChanged,
                )
            }
        }
    }
}

/**
 * Wrapper of [HorizontalSplitLayout] that accepts [onDividerPositionChanged] callback that emits
 * the changed divider position.
 * Also changed the behavior (or an issue?) that passing different initialDividerPosition value
 * will affect the actual divider's position in the next recomposition.
 */
@Composable
fun HorizontalSplitLayoutWrapper(
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    dividerColor: Color = JewelTheme.globalColors.borders.normal,
    dividerThickness: Dp = 1.dp,
    dividerIndent: Dp = 0.dp,
    draggableWidth: Dp = 8.dp,
    minRatio: Float = 0f,
    maxRatio: Float = 1f,
    initialDividerPosition: Dp = 300.dp,
    onDividerPositionChanged: ((Dp) -> Unit)? = null,
) {
    val density = LocalDensity.current
    // Original JetBrain's implementation doesn't pass the initialDividerPosition in remember's key,
    // but this results in a behavior that different initialDividerPosition doesn't reflected
    // in the next recomposition
    var dividerX by remember(initialDividerPosition) { mutableStateOf(with(density) { initialDividerPosition.roundToPx() }) }

    // Adds a LaunchedEffect to track the changed divider position
    LaunchedEffect(dividerX) {
        onDividerPositionChanged?.let { callback ->
            val newPositionInDp =
                with(density) {
                    dividerX.toDp()
                }
            callback(newPositionInDp)
        }
    }

    Layout(
        modifier = modifier,
        content = {
            val dividerInteractionSource = remember { MutableInteractionSource() }
            first(Modifier.layoutId("first"))

            Divider(
                orientation = Orientation.Vertical,
                modifier = Modifier.fillMaxHeight().layoutId("divider"),
                color = dividerColor,
                thickness = dividerThickness,
                startIndent = dividerIndent,
            )

            second(Modifier.layoutId("second"))

            Box(
                Modifier
                    .fillMaxHeight()
                    .width(draggableWidth)
                    .draggable(
                        interactionSource = dividerInteractionSource,
                        orientation = androidx.compose.foundation.gestures.Orientation.Horizontal,
                        state = rememberDraggableState { delta -> dividerX += delta.toInt() },
                    ).pointerHoverIcon(PointerIconResizeHorizontal)
                    .layoutId("divider-handle"),
            )
        },
    ) { measurables, incomingConstraints ->
        val availableWidth = incomingConstraints.maxWidth
        val actualDividerX =
            dividerX
                .coerceIn(0, availableWidth)
                .coerceIn(
                    (availableWidth * minRatio).roundToInt(),
                    (availableWidth * maxRatio).roundToInt(),
                )

        val dividerMeasurable = measurables.single { it.layoutId == "divider" }
        val dividerPlaceable =
            dividerMeasurable.measure(
                Constraints.fixed(dividerThickness.roundToPx(), incomingConstraints.maxHeight),
            )

        val firstComponentConstraints =
            Constraints.fixed((actualDividerX).coerceAtLeast(0), incomingConstraints.maxHeight)
        val firstPlaceable =
            measurables
                .find { it.layoutId == "first" }
                ?.measure(firstComponentConstraints)
                ?: error("No first component found. Have you applied the provided Modifier to it?")

        val secondComponentConstraints =
            Constraints.fixed(
                width = availableWidth - actualDividerX + dividerPlaceable.width,
                height = incomingConstraints.maxHeight,
            )
        val secondPlaceable =
            measurables
                .find { it.layoutId == "second" }
                ?.measure(secondComponentConstraints)
                ?: error("No second component found. Have you applied the provided Modifier to it?")

        val dividerHandlePlaceable =
            measurables
                .single { it.layoutId == "divider-handle" }
                .measure(Constraints.fixedHeight(incomingConstraints.maxHeight))

        layout(availableWidth, incomingConstraints.maxHeight) {
            firstPlaceable.placeRelative(0, 0)
            dividerPlaceable.placeRelative(actualDividerX - dividerPlaceable.width / 2, 0)
            secondPlaceable.placeRelative(actualDividerX + dividerPlaceable.width, 0)
            dividerHandlePlaceable.placeRelative(
                actualDividerX - dividerHandlePlaceable.measuredWidth / 2,
                0,
            )
        }
    }
}
