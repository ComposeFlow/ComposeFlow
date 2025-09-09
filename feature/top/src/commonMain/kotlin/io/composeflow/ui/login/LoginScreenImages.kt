package io.composeflow.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource

/**
 * Platform-specific image loading for login screen.
 */
@Composable
expect fun composeFlowLogoPainter(): Painter

@Composable
expect fun googleSignInButtonPainter(): Painter