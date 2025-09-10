package io.composeflow.ui.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import io.composeflow.ui.window.styling.TitleBarStyle

object WasmJsTitleBar : TitleBar {
    override val systemButtonsPosition: SystemButtonsPosition
        get() = SystemButtonsPosition(buttons = emptyList(), isLeft = false)

    @Composable
    override fun RenderSystemButtons(
        onRequestClose: () -> Unit,
        onRequestMinimize: (() -> Unit)?,
        onToggleMaximize: (() -> Unit)?,
    ) { }

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

actual fun TitleBar.Companion.getPlatformTitleBar(): TitleBar = WasmJsTitleBar
