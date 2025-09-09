package io.composeflow.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import io.composeflow.ComposeFlow_Logo_Symbol
import io.composeflow.Res
import io.composeflow.ic_google
import org.jetbrains.compose.resources.painterResource

@Composable
actual fun composeFlowLogoPainter(): Painter {
    // Use the existing ComposeFlow logo from shared resources
    return painterResource(Res.drawable.ComposeFlow_Logo_Symbol)
}

@Composable
actual fun googleSignInButtonPainter(): Painter {
    // Use the existing Google icon from shared resources
    return painterResource(Res.drawable.ic_google)
}