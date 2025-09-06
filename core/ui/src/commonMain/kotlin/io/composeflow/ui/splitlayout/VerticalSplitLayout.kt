package io.composeflow.ui.splitlayout

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.composeflow.ui.PointerIconResizeVertical

@Composable
fun VerticalSplitLayout(
    modifier: Modifier = Modifier,
    sliderThickness: Dp = 1.dp,
    sliderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    initialSliderPosition: Dp = 100.dp,
    minSliderRatio: Float = 0f,
    maxSliderRatio: Float = 1f,
    draggableWidth: Dp = 8.dp,
    topContent: @Composable BoxWithConstraintsScope.() -> Unit,
    bottomContent: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    require(minSliderRatio in 0f..1f) { "minSliderRatio must be between 0f and 1f" }
    require(maxSliderRatio in 0f..1f) { "maxSliderRatio must be between 0f and 1f" }
    require(minSliderRatio < maxSliderRatio) { "minSliderRatio must be less than maxSliderRatio" }

    BoxWithConstraints(
        modifier = modifier,
    ) {
        val containerHeight = maxHeight
        val density = LocalDensity.current

        var sliderRatio by remember {
            mutableStateOf(
                initialSliderPosition.let { dp ->
                    (dp / containerHeight).coerceIn(minSliderRatio, maxSliderRatio)
                },
            )
        }
        val sliderOffsetY by remember(sliderThickness, containerHeight, sliderRatio) {
            derivedStateOf {
                sliderRatio
                    .times(containerHeight - sliderThickness)
                    .coerceIn(0.dp, containerHeight - sliderThickness)
            }
        }

        BoxWithConstraints(
            modifier = Modifier.height(sliderOffsetY + sliderThickness / 2).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            topContent(this)
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(top = sliderOffsetY + sliderThickness / 2),
            contentAlignment = Alignment.Center,
        ) {
            bottomContent(this)
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(draggableWidth)
                    .offset(y = sliderOffsetY)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state =
                            rememberDraggableState { delta ->
                                val deltaDp = with(density) { delta.toDp() }
                                val newOffset =
                                    (sliderOffsetY + deltaDp).coerceIn(
                                        0.dp,
                                        containerHeight - sliderThickness,
                                    )
                                sliderRatio =
                                    (newOffset / (containerHeight - sliderThickness))
                                        .coerceIn(minSliderRatio, maxSliderRatio)
                            },
                    ).pointerHoverIcon(PointerIconResizeVertical),
        ) {
            HorizontalDivider(
                thickness = sliderThickness,
                color = sliderColor,
            )
        }
    }
}
