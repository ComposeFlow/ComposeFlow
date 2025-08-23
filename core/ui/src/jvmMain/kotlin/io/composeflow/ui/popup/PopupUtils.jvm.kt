package io.composeflow.ui.popup

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.rememberCursorPositionProvider

@Composable
actual fun rememberDefaultPopupPositionProvider(): PopupPositionProvider = rememberCursorPositionProvider()
