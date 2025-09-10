package io.composeflow.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import io.composeflow.Res
import io.composeflow.composeflow_logo_with_text
import io.composeflow.ic_google
import org.jetbrains.compose.resources.painterResource

@Composable
actual fun composeFlowLogoPainter(): Painter {
    // Use the existing ComposeFlow logo from shared resources
    return painterResource(Res.drawable.composeflow_logo_with_text)
}

@Composable
actual fun googleSignInButtonPainter(): Painter {
    // Use the existing Google icon from shared resources
    return painterResource(Res.drawable.ic_google)
}
