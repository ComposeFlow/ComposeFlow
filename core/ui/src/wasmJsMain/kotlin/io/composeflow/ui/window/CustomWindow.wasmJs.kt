package io.composeflow.ui.window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
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
    CustomWindowImpl(
        onCloseRequest = onCloseRequest,
        windowController = windowController,
        titleBarStyle = titleBarStyle,
        onRequestMinimize = onRequestMinimize,
        onRequestToggleMaximize = onRequestToggleMaximize,
        onWindowIconClicked = onWindowIconClicked,
        content = content,
    )
}

@Composable
private fun CustomWindowImpl(
    onCloseRequest: () -> Unit,
    windowController: WindowController,
    titleBarStyle: TitleBarStyle,
    onWindowIconClicked: (() -> Unit)?,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
    content: @Composable (() -> Unit),
) {
    val titleBar = TitleBar.getPlatformTitleBar()
    val start = windowController.start
    val end = windowController.end
    val title = windowController.title.orEmpty()
    val icon = windowController.icon
    val showTitleInTitleBar = windowController.showTitleInTitleBar
    val showWindowIcon = windowController.showWindowIconInTitleBar

    CompositionLocalProvider(
        LocalWindowController provides windowController,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            titleBar.RenderTitleBar(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                titleBar = titleBar,
                title = title,
                windowIcon = icon,
                showTitle = showTitleInTitleBar,
                showWindowIcon = showWindowIcon,
                style = titleBarStyle,
                start = start,
                end = end,
                onWindowIconClicked = onWindowIconClicked,
                onRequestClose = onCloseRequest,
                onRequestMinimize = onRequestMinimize,
                onRequestToggleMaximize = onRequestToggleMaximize,
            )
            content()
        }
    }
}
