package io.composeflow.ui.window

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.composeflow.ui.window.styling.TitleBarStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CommonTitleBarContent(
    modifier: Modifier,
    title: String,
    windowIcon: Painter?,
    showTitle: Boolean,
    showWindowIcon: Boolean,
    start: @Composable (() -> Unit)?,
    end: @Composable (() -> Unit)?,
    onWindowIconClicked: (() -> Unit)?,
) {
    Box(
        modifier = modifier,
    ) {
        Row(
            Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(16.dp))

            if (showWindowIcon) {
                windowIcon?.let {
                    Image(
                        painter = it,
                        contentDescription = null,
                        modifier =
                            Modifier.size(36.dp).clip(RoundedCornerShape(4.dp)).then(
                                if (onWindowIconClicked != null) {
                                    Modifier.clickable { onWindowIconClicked() }
                                } else {
                                    Modifier
                                },
                            ),
                    )
                }
            }

            start?.let {
                start()
            }
        }

        if (showTitle) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = title,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        end?.let {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                end()
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
internal fun CommonRenderTitleBar(
    modifier: Modifier,
    titleBar: TitleBar,
    title: String,
    windowIcon: Painter? = null,
    style: TitleBarStyle,
    showTitle: Boolean,
    showWindowIcon: Boolean,
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    onWindowIconClicked: (() -> Unit)?,
    onRequestClose: () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
) {
    val systemButtonsAtFirst = titleBar.systemButtonsPosition.isLeft
    val density = LocalDensity.current
    val background = style.colors.background
    val gradientStartColor = style.colors.gradientStartColor

    val backgroundBrush =
        remember {
            with(density) {
                Brush.horizontalGradient(
                    0.0f to background,
                    0.5f to gradientStartColor,
                    1.0f to background,
                    startX = style.metrics.gradientStartX.toPx(),
                    endX = style.metrics.gradientEndX.toPx(),
                )
            }
        }

    Row(
        modifier = modifier.background(backgroundBrush).height(titleBar.titleBarHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (systemButtonsAtFirst) {
            titleBar.RenderSystemButtons(
                onRequestClose = onRequestClose,
                onRequestMinimize = onRequestMinimize,
                onToggleMaximize = onRequestToggleMaximize,
            )
        }

        titleBar.RenderTitleBarContent(
            title = title,
            modifier = Modifier.weight(1f),
            windowIcon = windowIcon,
            showTitle = showTitle,
            showWindowIcon = showWindowIcon,
            start = start,
            end = end,
            onWindowIconClicked = onWindowIconClicked,
        )

        if (!systemButtonsAtFirst) {
            titleBar.RenderSystemButtons(
                onRequestClose = onRequestClose,
                onRequestMinimize = onRequestMinimize,
                onToggleMaximize = onRequestToggleMaximize,
            )
        }
    }
}

fun Modifier.windowButton(): Modifier =
    fillMaxHeight()
        .wrapContentHeight()
        .padding(
            horizontal = 15.dp,
        ).requiredSize(15.dp)
