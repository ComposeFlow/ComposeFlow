package io.composeflow.ui.about

data class VersionInfo(
    val version: String,
    val isUpdateAvailable: Boolean = false,
)

/**
 * Platform-specific software update service interface.
 * JVM implementation uses Conveyor, WASM implementation provides no-op functionality.
 */
expect class SoftwareUpdateService() {
    suspend fun getCurrentVersion(): VersionInfo?

    suspend fun checkForUpdates(): VersionInfo?

    fun canDoOnlineUpdates(): Boolean

    fun triggerUpdateUI()
}
