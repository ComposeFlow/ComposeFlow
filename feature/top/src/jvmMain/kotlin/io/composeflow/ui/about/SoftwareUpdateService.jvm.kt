package io.composeflow.ui.about

import dev.hydraulic.conveyor.control.SoftwareUpdateController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class SoftwareUpdateService {
    private val updateController: SoftwareUpdateController? = SoftwareUpdateController.getInstance()
    
    actual suspend fun getCurrentVersion(): VersionInfo? {
        return withContext(Dispatchers.IO) {
            updateController?.let {
                VersionInfo(
                    version = it.currentVersion?.version ?: "Unknown",
                    isUpdateAvailable = false
                )
            }
        }
    }
    
    actual suspend fun checkForUpdates(): VersionInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val remoteVersionObj = updateController?.currentVersionFromRepository
                val currentVersionObj = updateController?.currentVersion
                
                if (remoteVersionObj != null && currentVersionObj != null) {
                    VersionInfo(
                        version = remoteVersionObj.version,
                        isUpdateAvailable = remoteVersionObj.compareTo(currentVersionObj) > 0
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    actual fun canDoOnlineUpdates(): Boolean {
        return updateController?.canTriggerUpdateCheckUI() == SoftwareUpdateController.Availability.AVAILABLE
    }
    
    actual fun triggerUpdateUI() {
        updateController?.triggerUpdateCheckUI()
    }
}