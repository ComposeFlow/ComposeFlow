package io.composeflow.ui.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter

class WindowController(
    title: String? = null,
    icon: Painter? = null,
) {
    var title by mutableStateOf(title)
    var icon by mutableStateOf(icon)
    var start: (@Composable () -> Unit)? by mutableStateOf(null)
    var end: (@Composable () -> Unit)? by mutableStateOf(null)
    var showTitleInTitleBar by mutableStateOf(false)
    var showWindowIconInTitleBar by mutableStateOf(false)
}

@Composable
fun rememberWindowController(
    title: String? = null,
    icon: Painter? = null,
    showTitleInTitleBar: Boolean = false,
    showWindowIconInTitleBar: Boolean = false,
): WindowController {
    val controller =
        remember {
            WindowController(
                title,
                icon,
            )
        }
    LaunchedEffect(title) {
        controller.title = title
    }
    LaunchedEffect(icon) {
        controller.icon = icon
    }
    LaunchedEffect(showTitleInTitleBar) {
        controller.showTitleInTitleBar = showTitleInTitleBar
    }
    LaunchedEffect(showWindowIconInTitleBar) {
        controller.showWindowIconInTitleBar = showWindowIconInTitleBar
    }
    return controller
}

val LocalWindowController =
    compositionLocalOf<WindowController> { error("window controller not provided") }
