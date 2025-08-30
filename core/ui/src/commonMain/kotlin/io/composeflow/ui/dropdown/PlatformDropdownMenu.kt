package io.composeflow.ui.dropdown

import androidx.compose.runtime.Composable

@Composable
expect fun PlatformDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
)
