package io.composeflow.wrapper

import co.touchlab.kermit.Logger
import io.composeflow.ui.statusbar.StatusBarUiState

expect class GradleCommandLineRunner {
    suspend fun assembleDebug(onStatusBarUiStateChanged: (StatusBarUiState) -> Unit)
    suspend fun jsBrowserDevelopmentRun(onStatusBarUiStateChanged: (StatusBarUiState) -> Unit)
}