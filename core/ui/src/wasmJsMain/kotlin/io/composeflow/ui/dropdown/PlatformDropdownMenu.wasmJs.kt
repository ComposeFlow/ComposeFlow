package io.composeflow.ui.dropdown

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable

@Composable
actual fun PlatformDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        content()
    }
}