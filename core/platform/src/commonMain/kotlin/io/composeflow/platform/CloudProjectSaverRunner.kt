package io.composeflow.platform

import co.touchlab.kermit.Logger
import io.composeflow.cloud.storage.GoogleCloudStorageWrapper
import io.composeflow.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

object CloudProjectSaverRunner {
    var userId: String? = null
    var projectId: String? = null
    private val ioDispatcher =
        ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER) {
            Dispatchers.Default
        }

    private val cloudProjectSaver =
        createCloudProjectSaver(
            cloudStorageWrapper = GoogleCloudStorageWrapper(),
        )
    private val localProjectSaver = createLocalProjectSaver()

    suspend fun startSavingProjectPeriodically() {
        Logger.i("Started periodic sync for the project")
        withContext(ioDispatcher) {
            while (isActive) {
                syncProjectYaml()
                delay(3.minutes)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun syncProjectYaml() {
        withContext(ioDispatcher) {
            val userId = userId ?: return@withContext
            val projectId = projectId ?: return@withContext
            val localYaml = localProjectSaver.loadProject(userId, projectId) ?: return@withContext

            Logger.i("Saving the project: $projectId to the cloud. ${Clock.System.now()}")
            cloudProjectSaver.saveProjectYaml(userId, projectId, localYaml.yaml)
        }
    }
}
