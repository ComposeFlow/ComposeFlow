package io.composeflow.ui.popup

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

/**
 * Wrapper of [Popup] with the [popupPositionProvider] can be configured instead of using the
 * default rememberCursorPositionProvider.
 */
@Composable
fun PositionCustomizablePopup(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    focusable: Boolean = true,
    popupPositionProvider: PopupPositionProvider = rememberDefaultPopupPositionProvider(),
    scrollState: ScrollState = rememberScrollState(),
    onKeyEvent: ((KeyEvent) -> Boolean)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = expanded

    if (expandedStates.currentState || expandedStates.targetState) {
        PopupContent(
            popupPositionProvider = popupPositionProvider,
            scrollState = scrollState,
            onDismissRequest = onDismissRequest,
            focusable = focusable,
            modifier = modifier,
            content = content,
            onKeyEvent = onKeyEvent,
        )
    }
}

@Composable
private fun PopupContent(
    popupPositionProvider: PopupPositionProvider,
    scrollState: ScrollState,
    onDismissRequest: () -> Unit,
    focusable: Boolean = true,
    modifier: Modifier = Modifier,
    onKeyEvent: ((KeyEvent) -> Boolean)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    var focusManager: FocusManager? by mutableStateOf(null)
    var inputModeManager: InputModeManager? by mutableStateOf(null)
    Popup(
        onDismissRequest = onDismissRequest,
        popupPositionProvider = popupPositionProvider,
        properties = PopupProperties(focusable = focusable),
        onKeyEvent = onKeyEvent,
    ) {
        focusManager = LocalFocusManager.current
        inputModeManager = LocalInputModeManager.current

        Card {
            Column(
                modifier =
                    modifier
                        .width(IntrinsicSize.Max)
                        .verticalScroll(scrollState),
            ) {
                content()
            }
        }
    }
}
