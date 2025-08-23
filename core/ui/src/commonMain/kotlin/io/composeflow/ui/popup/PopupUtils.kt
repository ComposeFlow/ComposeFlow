package io.composeflow.ui.popup

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.PopupPositionProvider

@Composable
expect fun rememberDefaultPopupPositionProvider(): PopupPositionProvider
