package io.composeflow.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource

@Composable
actual fun composeFlowLogoPainter(): Painter {
    return remember {
        useResource("ComposeFlow_inverted_800x600.png") {
            BitmapPainter(loadImageBitmap(it))
        }
    }
}

@Composable
actual fun googleSignInButtonPainter(): Painter {
    return remember {
        useResource("btn_google_signin_dark.png") {
            BitmapPainter(loadImageBitmap(it))
        }
    }
}