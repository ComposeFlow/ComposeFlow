package io.composeflow.ui.dropdown

import androidx.compose.material.CursorDropdownMenu
import androidx.compose.runtime.Composable

@Composable
actual fun PlatformDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    CursorDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        content()
    }
}
