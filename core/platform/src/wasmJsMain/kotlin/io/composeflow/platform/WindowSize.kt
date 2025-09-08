package io.composeflow.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.browser.window
import org.w3c.dom.events.Event

@Composable
actual fun rememberWindowSize(): DpSize {
    var size by remember {
        mutableStateOf(
            DpSize(
                window.innerWidth.dp,
                window.innerHeight.dp
            )
        )
    }
    
    DisposableEffect(Unit) {
        val updateSize: (Event) -> Unit = {
            size = DpSize(
                window.innerWidth.dp,
                window.innerHeight.dp
            )
        }
        
        window.addEventListener("resize", updateSize)
        
        onDispose {
            window.removeEventListener("resize", updateSize)
        }
    }
    
    return size
}