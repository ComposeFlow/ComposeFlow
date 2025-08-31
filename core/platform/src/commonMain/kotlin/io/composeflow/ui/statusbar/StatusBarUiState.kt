package io.composeflow.ui.statusbar

sealed interface StatusBarUiState {
    data object Normal : StatusBarUiState

    data class Loading(
        val message: String?,
    ) : StatusBarUiState

    data class Success(
        val message: String?,
    ) : StatusBarUiState

    /**
     * Special status only for representing the success state for jsBrowserRun.
     * Following messages are emitted to the standard error at the moment when ran from
     * gradle tooling API.
     * To distinguish the success state for the jsBrowserRun task from the [Failure] state,
     * adding this state only for the task.
     *
     * <i> [webpack-dev-server] Project is running at:
     * <i> [webpack-dev-server] Loopback: http://localhost:8081/
     * <i> [webpack-dev-server] On Your Network (IPv4): http://192.168.50.96:8081/
     * <i> [webpack-dev-server] On Your Network (IPv6): http://[fe80::1]:8081/
     * <i> [webpack-dev-server] Content not from webpack is served from 'kotlin, ../../../../jsApp/build/processedResources/js/main' directory
     * <i> [webpack-dev-middleware] wait until bundle finished: /
     */
    data class JsBrowserRunSuccess(
        val message: String?,
    ) : StatusBarUiState

    data class Failure(
        val message: String?,
    ) : StatusBarUiState
}
