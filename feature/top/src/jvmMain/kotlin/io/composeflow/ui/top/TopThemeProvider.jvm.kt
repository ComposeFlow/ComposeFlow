package io.composeflow.ui.top

import androidx.compose.runtime.Composable
import io.composeflow.ui.jewel.ProvideLazyTreeStyle
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme

@Composable
actual fun ProvideTopScreenTheme(
    useDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    IntUiTheme(isDark = useDarkTheme) {
        ProvideLazyTreeStyle {
            content()
        }
    }
}