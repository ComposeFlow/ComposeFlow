package io.composeflow.ui.about

actual class SoftwareUpdateService {
    actual suspend fun getCurrentVersion(): VersionInfo? {
        // In WASM, we hardcode a version
        return VersionInfo(
            version = "WASM Version",
            isUpdateAvailable = false
        )
    }
    
    actual suspend fun checkForUpdates(): VersionInfo? {
        // Updates are not available in WASM version
        return getCurrentVersion()
    }
    
    actual fun canDoOnlineUpdates(): Boolean {
        // WASM version cannot do online updates
        return false
    }
    
    actual fun triggerUpdateUI() {
        // No-op in WASM
    }
}