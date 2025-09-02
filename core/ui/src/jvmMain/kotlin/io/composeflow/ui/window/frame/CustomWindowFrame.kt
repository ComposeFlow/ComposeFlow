package io.composeflow.ui.window.frame

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.jetbrains.JBR
import com.jetbrains.WindowDecorations
import com.jetbrains.WindowMove
import io.composeflow.ui.window.LocalWindowController
import io.composeflow.ui.window.TitleBar
import io.composeflow.ui.window.WindowController
import io.composeflow.ui.window.getPlatformTitleBar
import io.composeflow.ui.window.styling.TitleBarStyle
import io.composeflow.ui.window.titlebar.windows.applyUiScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.awt.Window
import java.awt.event.MouseEvent

@Composable
private fun FrameWindowScope.CustomWindowFrame(
    onWindowIconClicked: (() -> Unit)?,
    onRequestMinimize: (() -> Unit)?,
    onRequestClose: () -> Unit,
    onRequestToggleMaximize: (() -> Unit)?,
    title: String,
    titleBarStyle: TitleBarStyle,
    showTitleInTitleBar: Boolean,
    showWindowIconInTitleBar: Boolean,
    windowIcon: Painter? = null,
    background: Color,
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .ifThen(!JBR.isWindowDecorationsSupported()) {
                ifThen(isWindowFloating()) {
                    border(1.dp, Color.Gray.copy(0.25f), RectangleShape).padding(1.dp)
                }
            }.background(background),
    ) {
        SnapDraggableToolbar(
            title = title,
            windowIcon = windowIcon,
            titleBarStyle = titleBarStyle,
            showTitleInTitleBar = showTitleInTitleBar,
            showWindowIconInTitleBar = showWindowIconInTitleBar,
            start = start,
            end = end,
            onWindowIconClicked = onWindowIconClicked,
            onRequestMinimize = onRequestMinimize,
            onRequestClose = onRequestClose,
            onRequestToggleMaximize = onRequestToggleMaximize,
        )

        content()
    }
}

@Composable
fun isWindowFocused(): Boolean = LocalWindowInfo.current.isWindowFocused

@Composable
fun isWindowMaximized(): Boolean = LocalWindowState.current.placement == WindowPlacement.Maximized

@Composable
fun isWindowFloating(): Boolean = LocalWindowState.current.placement == WindowPlacement.Floating

@Composable
fun FrameWindowScope.SnapDraggableToolbar(
    title: String,
    windowIcon: Painter? = null,
    titleBarStyle: TitleBarStyle,
    showTitleInTitleBar: Boolean,
    showWindowIconInTitleBar: Boolean,
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    onWindowIconClicked: (() -> Unit)?,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
    onRequestClose: () -> Unit,
) {
    val titleBar = TitleBar.getPlatformTitleBar()
    if (JBR.isWindowDecorationsSupported()) {
        val density = LocalDensity.current
        val uiScale = LocalUiScale.current

        fun computeHeaderHeight(height: Dp): Float = height.value.applyUiScale(uiScale)

        var headerHeight by remember {
            mutableStateOf(computeHeaderHeight(titleBar.titleBarHeight))
        }
        val customTitleBar =
            remember {
                JBR.getWindowDecorations().createCustomTitleBar()
            }
        LaunchedEffect(headerHeight) {
            customTitleBar.height = headerHeight
            customTitleBar.putProperty("controls.visible", false)
            JBR.getWindowDecorations().setCustomTitleBar(window, customTitleBar)
        }
        Box(
            Modifier.onSizeChanged {
                headerHeight =
                    computeHeaderHeight(
                        density.run { it.height.toDp() },
                    )
            },
        ) {
            Spacer(
                Modifier.matchParentSize().customTitleBarMouseEventHandler(customTitleBar),
            )
            FrameContent(
                titleBar = titleBar,
                modifier = Modifier,
                title = title,
                windowIcon = windowIcon,
                showTitleInTitleBar = showTitleInTitleBar,
                showWindowIconInTitleBar = showWindowIconInTitleBar,
                titleBarStyle = titleBarStyle,
                start = start,
                end = end,
                onRequestMinimize = onRequestMinimize,
                onRequestToggleMaximize = onRequestToggleMaximize,
                onRequestClose = onRequestClose,
                onWindowIconClicked = onWindowIconClicked,
            )
        }
    } else {
        SystemDraggableSection(
            onRequestToggleMaximize = onRequestToggleMaximize,
        ) { modifier ->
            FrameContent(
                titleBar = titleBar,
                modifier = modifier,
                title = title,
                windowIcon = windowIcon,
                showTitleInTitleBar = showTitleInTitleBar,
                showWindowIconInTitleBar = showWindowIconInTitleBar,
                titleBarStyle = titleBarStyle,
                start = start,
                end = end,
                onRequestMinimize = onRequestMinimize,
                onRequestToggleMaximize = onRequestToggleMaximize,
                onRequestClose = onRequestClose,
                onWindowIconClicked = onWindowIconClicked,
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun FrameWindowScope.SystemDraggableSection(
    onRequestToggleMaximize: (() -> Unit)?,
    content: @Composable (Modifier) -> Unit,
) {
    val windowMove: WindowMove? = JBR.getWindowMove()
    val viewConfig = LocalViewConfiguration.current
    var lastPress by remember { mutableStateOf(0L) }
    if (windowMove != null) {
        content(
            Modifier.onPointerEvent(PointerEventType.Press, PointerEventPass.Main) {
                if (this.currentEvent.button == PointerButton.Primary && this.currentEvent.changes.any { changed -> !changed.isConsumed }) {
                    windowMove.startMovingTogetherWithMouse(window, MouseEvent.BUTTON1)
                    if (System.currentTimeMillis() - lastPress in viewConfig.doubleTapMinTimeMillis..viewConfig.doubleTapTimeoutMillis) {
                        onRequestToggleMaximize?.invoke()
                    }
                    lastPress = System.currentTimeMillis()
                }
            },
        )
    } else {
        WindowDraggableArea {
            content(Modifier)
        }
    }
}

internal fun Modifier.customTitleBarMouseEventHandler(titleBar: WindowDecorations.CustomTitleBar): Modifier =
    pointerInput(Unit) {
        val currentContext = currentCoroutineContext()
        awaitPointerEventScope {
            var inUserControl = false
            while (currentContext.isActive) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                event.changes.forEach {
                    if (!it.isConsumed && !inUserControl) {
                        titleBar.forceHitTest(false)
                    } else {
                        if (event.type == PointerEventType.Press) {
                            inUserControl = true
                        }
                        if (event.type == PointerEventType.Release) {
                            inUserControl = false
                        }
                        titleBar.forceHitTest(true)
                    }
                }
            }
        }
    }

@Composable
private fun FrameContent(
    titleBar: TitleBar,
    modifier: Modifier,
    title: String,
    windowIcon: Painter? = null,
    showTitleInTitleBar: Boolean,
    showWindowIconInTitleBar: Boolean,
    titleBarStyle: TitleBarStyle,
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
    onRequestClose: () -> Unit,
    onWindowIconClicked: (() -> Unit)?,
) {
    titleBar.RenderTitleBar(
        modifier = modifier.fillMaxWidth(),
        titleBar = titleBar,
        title = title,
        windowIcon = windowIcon,
        showTitle = showTitleInTitleBar,
        showWindowIcon = showWindowIconInTitleBar,
        style = titleBarStyle,
        start = start,
        end = end,
        onWindowIconClicked = onWindowIconClicked,
        onRequestClose = onRequestClose,
        onRequestMinimize = onRequestMinimize,
        onRequestToggleMaximize = onRequestToggleMaximize,
    )
}

private fun Color.toWindowColorType() =
    java.awt.Color(
        red,
        green,
        blue,
    )

@Composable
fun CustomWindow(
    state: WindowState,
    onCloseRequest: () -> Unit,
    onWindowIconClicked: (() -> Unit)?,
    resizable: Boolean = true,
    titleBarStyle: TitleBarStyle,
    onRequestMinimize: (() -> Unit)? = {
        state.isMinimized = true
    },
    onRequestToggleMaximize: (() -> Unit)? = {
        if (state.placement == WindowPlacement.Maximized) {
            state.placement = WindowPlacement.Floating
        } else {
            state.placement = WindowPlacement.Maximized
        }
    },
    windowController: WindowController =
        remember {
            WindowController()
        },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    alwaysOnTop: Boolean = false,
    preventMinimize: Boolean = onRequestMinimize == null,
    content: @Composable FrameWindowScope.() -> Unit,
) {
    val start = windowController.start
    val end = windowController.end
    val title = windowController.title.orEmpty()
    val showTitleInTitleBar = windowController.showTitleInTitleBar
    val showWindowIconInTitleBar = windowController.showWindowIconInTitleBar
    val icon = windowController.icon

    val transparent: Boolean
    val undecorated: Boolean
    val isAeroSnapSupported = JBR.isWindowDecorationsSupported()
    if (isAeroSnapSupported) {
        // we use aero snap
        transparent = false
        undecorated = false
    } else {
        // we decorate window and add our custom layout
        transparent = true
        undecorated = true
    }
    Window(
        state = state,
        transparent = transparent,
        undecorated = undecorated,
        icon = icon,
        title = title,
        resizable = resizable,
        onCloseRequest = onCloseRequest,
        onKeyEvent = onKeyEvent,
        alwaysOnTop = alwaysOnTop,
    ) {
        val isLight = false // move to style
        val background = MaterialTheme.colorScheme.background
        LaunchedEffect(background) {
            withContext(Dispatchers.Main) {
                // I set window background fix window edge flickering on window resize
                window.background =
                    background
                        .takeOrElse {
                            if (isLight) {
                                Color.White
                            } else {
                                Color.Black
                            }
                        }.toWindowColorType()
            }
        }
        UiScaledContent {
            CompositionLocalProvider(
                LocalWindowController provides windowController,
                LocalWindowState provides state,
                LocalWindow provides window,
                LocalFrameWindowScope provides this,
            ) {
                if (preventMinimize) {
                    PreventMinimize()
                }
                CustomWindowFrame(
                    onRequestMinimize = onRequestMinimize,
                    onRequestClose = onCloseRequest,
                    onRequestToggleMaximize = onRequestToggleMaximize,
                    title = title,
                    titleBarStyle = titleBarStyle,
                    showTitleInTitleBar = showTitleInTitleBar,
                    showWindowIconInTitleBar = showWindowIconInTitleBar,
                    windowIcon = icon,
                    background = background,
                    start = start,
                    end = end,
                    onWindowIconClicked = onWindowIconClicked,
                ) {
                    ResponsiveBox {
                        Box(Modifier.clearFocusOnTap()) {
                            PopUpContainer {
                                content()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreventMinimize() {
    val state = LocalWindowState.current
    LaunchedEffect(state.isMinimized) {
        if (state.isMinimized) {
            state.isMinimized = false
        }
    }
}

private fun Modifier.clearFocusOnTap(): Modifier =
    composed {
        val focusManager = LocalFocusManager.current
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(pass = PointerEventPass.Main)
                val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Main)
                if (upEvent != null) {
                    focusManager.clearFocus()
                }
            }
        }
    }

private val LocalBottomSheetContainer =
    compositionLocalOf<MutableMap<Any, @Composable () -> Unit>> { error("not initialized yet") }

@Composable
private fun PopUpContainer(context: @Composable () -> Unit) {
    val items =
        remember {
            mutableStateMapOf<Any, @Composable () -> Unit>()
        }
    CompositionLocalProvider(
        LocalBottomSheetContainer provides items,
        content = context,
    )
    items.forEach {
        key(it.key) {
            it.value()
        }
    }
}

private val LocalWindowState =
    compositionLocalOf<WindowState> { error("window controller not provided") }

val LocalFrameWindowScope =
    compositionLocalOf<FrameWindowScope> {
        error("LocalFrameWindowScope not provided yet")
    }

private inline fun <Base, T : Base> T.ifThen(
    condition: Boolean,
    block: T.() -> Base,
): Base =
    if (condition) {
        this.block()
    } else {
        this
    }

val LocalWindow =
    staticCompositionLocalOf<Window> {
        error("CompositionLocal LocalWindow not provided")
    }
