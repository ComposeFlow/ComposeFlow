package io.composeflow.ui.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.composeflow.ui.window.styling.TitleBarStyle

interface TitleBar {
    val titleBarHeight: Dp
        get() = DefaultTitleBarHeigh
    val systemButtonsPosition: SystemButtonsPosition

    @Composable
    fun RenderSystemButtons(
        onRequestClose: () -> Unit,
        onRequestMinimize: (() -> Unit)?,
        onToggleMaximize: (() -> Unit)?,
    )

    @Composable
    fun RenderTitleBarContent(
        title: String,
        modifier: Modifier,
        windowIcon: Painter?,
        showTitle: Boolean,
        showWindowIcon: Boolean,
        start: (@Composable () -> Unit)?,
        end: (@Composable () -> Unit)?,
        onWindowIconClicked: (() -> Unit)?,
    )

    @Composable
    fun RenderTitleBar(
        modifier: Modifier,
        titleBar: TitleBar,
        title: String,
        windowIcon: Painter?,
        showTitle: Boolean,
        showWindowIcon: Boolean,
        style: TitleBarStyle,
        start: (@Composable () -> Unit)?,
        end: (@Composable () -> Unit)?,
        onWindowIconClicked: (() -> Unit)?,
        onRequestClose: () -> Unit,
        onRequestMinimize: (() -> Unit)?,
        onRequestToggleMaximize: (() -> Unit)?,
    )

    companion object {
        val DefaultTitleBarHeigh = 40.dp
    }
}

expect fun TitleBar.Companion.getPlatformTitleBar(): TitleBar

enum class SystemButtonType {
    Close,
    Minimize,
    Maximize,
}

data class SystemButtonsPosition(
    val buttons: List<SystemButtonType>,
    val isLeft: Boolean,
)
