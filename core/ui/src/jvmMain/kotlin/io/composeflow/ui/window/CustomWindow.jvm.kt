package io.composeflow.ui.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import io.composeflow.ui.window.frame.CustomWindow
import io.composeflow.ui.window.styling.TitleBarStyle

@Composable
actual fun ComposeFlowWindow(
    windowController: WindowController,
    titleBarStyle: TitleBarStyle,
    resizable: Boolean,
    onWindowIconClicked: (() -> Unit)?,
    onCloseRequest: () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
    onKeyEvent: (KeyEvent) -> Boolean,
    alwaysOnTop: Boolean,
    preventMinimize: Boolean,
    content: @Composable (() -> Unit),
) {
    val state = rememberWindowState(WindowPlacement.Maximized)
    CustomWindow(
        state = state,
        onCloseRequest = onCloseRequest,
        resizable = resizable,
        titleBarStyle = titleBarStyle,
        onWindowIconClicked = onWindowIconClicked,
        onRequestMinimize = {
            state.isMinimized = true
        },
        onRequestToggleMaximize = {
            if (state.placement == WindowPlacement.Maximized) {
                state.placement = WindowPlacement.Floating
            } else {
                state.placement = WindowPlacement.Maximized
            }
        },
        onKeyEvent = onKeyEvent,
        alwaysOnTop = alwaysOnTop,
        preventMinimize = preventMinimize,
        windowController = windowController,
        content = {
            content()
        },
    )
}
