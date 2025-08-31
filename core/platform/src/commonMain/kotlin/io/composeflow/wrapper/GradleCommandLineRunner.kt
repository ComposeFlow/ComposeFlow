package io.composeflow.wrapper

import io.composeflow.ui.statusbar.StatusBarUiState

expect class GradleCommandLineRunner {
    suspend fun assembleDebug(onStatusBarUiStateChanged: (StatusBarUiState) -> Unit)

    suspend fun jsBrowserDevelopmentRun(onStatusBarUiStateChanged: (StatusBarUiState) -> Unit)
}
