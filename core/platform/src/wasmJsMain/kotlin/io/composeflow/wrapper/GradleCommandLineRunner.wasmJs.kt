package io.composeflow.wrapper

import io.composeflow.ui.statusbar.StatusBarUiState

actual class GradleCommandLineRunner {
    actual suspend fun assembleDebug(onStatusBarUiStateChanged: (StatusBarUiState) -> Unit) {
        onStatusBarUiStateChanged(StatusBarUiState.Failure("Gradle build is not supported in web version"))
    }
    
    actual suspend fun jsBrowserDevelopmentRun(onStatusBarUiStateChanged: (StatusBarUiState) -> Unit) {
        onStatusBarUiStateChanged(StatusBarUiState.Failure("JS browser run is not supported in web version"))
    }
}