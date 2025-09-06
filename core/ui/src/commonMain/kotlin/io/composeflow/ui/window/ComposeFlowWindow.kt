package io.composeflow.ui.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.KeyEvent
import io.composeflow.ui.window.styling.TitleBarStyle
import io.composeflow.ui.window.styling.dark

@Composable
expect fun ComposeFlowWindow(
    windowController: WindowController =
        remember {
            WindowController()
        },
    titleBarStyle: TitleBarStyle = TitleBarStyle.dark(),
    resizable: Boolean = true,
    onWindowIconClicked: (() -> Unit)? = null,
    onCloseRequest: () -> Unit = {},
    onRequestMinimize: (() -> Unit)? = {
    },
    onRequestToggleMaximize: (() -> Unit)? = {
    },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    alwaysOnTop: Boolean = false,
    preventMinimize: Boolean = onRequestMinimize == null,
    content: @Composable () -> Unit,
)
