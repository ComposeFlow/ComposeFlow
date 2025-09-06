package io.composeflow.ui.window.titlebar.linux

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import io.composeflow.ui.window.CommonRenderTitleBar
import io.composeflow.ui.window.CommonTitleBarContent
import io.composeflow.ui.window.SystemButtonType
import io.composeflow.ui.window.SystemButtonsPosition
import io.composeflow.ui.window.TitleBar
import io.composeflow.ui.window.styling.TitleBarStyle
import io.composeflow.ui.window.titlebar.CommonSystemButtons

object LinuxTitleBar : TitleBar {
    override val systemButtonsPosition: SystemButtonsPosition by lazy {
        LinuxSystemButtonsProvider.getPositions() ?: SystemButtonsPosition(
            buttons =
                listOf(
                    SystemButtonType.Minimize,
                    SystemButtonType.Maximize,
                    SystemButtonType.Close,
                ),
            isLeft = false,
        )
    }

    @Composable
    override fun RenderSystemButtons(
        onRequestClose: () -> Unit,
        onRequestMinimize: (() -> Unit)?,
        onToggleMaximize: (() -> Unit)?,
    ) {
        CommonSystemButtons(
            onRequestClose = onRequestClose,
            onRequestMinimize = onRequestMinimize,
            onToggleMaximize = onToggleMaximize,
            buttons = systemButtonsPosition.buttons,
        )
    }

    @Composable
    override fun RenderTitleBarContent(
        title: String,
        modifier: Modifier,
        windowIcon: Painter?,
        showTitle: Boolean,
        showWindowIcon: Boolean,
        start: @Composable (() -> Unit)?,
        end: @Composable (() -> Unit)?,
        onWindowIconClicked: (() -> Unit)?,
    ) {
        CommonTitleBarContent(
            modifier = modifier,
            title = title,
            windowIcon = windowIcon,
            showTitle = showTitle,
            showWindowIcon = showWindowIcon,
            start = start,
            end = end,
            onWindowIconClicked = onWindowIconClicked,
        )
    }

    @Composable
    override fun RenderTitleBar(
        modifier: Modifier,
        titleBar: TitleBar,
        title: String,
        windowIcon: Painter?,
        showTitle: Boolean,
        showWindowIcon: Boolean,
        style: TitleBarStyle,
        start: @Composable (() -> Unit)?,
        end: @Composable (() -> Unit)?,
        onWindowIconClicked: (() -> Unit)?,
        onRequestClose: () -> Unit,
        onRequestMinimize: (() -> Unit)?,
        onRequestToggleMaximize: (() -> Unit)?,
    ) {
        CommonRenderTitleBar(
            modifier = modifier,
            titleBar = titleBar,
            title = title,
            windowIcon = windowIcon,
            style = style,
            showTitle = showTitle,
            showWindowIcon = showWindowIcon,
            start = start,
            end = end,
            onWindowIconClicked = onWindowIconClicked,
            onRequestClose = onRequestClose,
            onRequestMinimize = onRequestMinimize,
            onRequestToggleMaximize = onRequestToggleMaximize,
        )
    }
}
