package io.composeflow.platform

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.mapBoth
import io.composeflow.cloud.storage.GoogleCloudStorageWrapper
import io.composeflow.datastore.ANONYMOUSE_USER_ID
import io.composeflow.datastore.PROJECT_YAML_FILE_NAME
import io.composeflow.datastore.ProjectSaver
import io.composeflow.datastore.ProjectYamlNameWithLastModified
import io.composeflow.di.ServiceLocator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun createCloudProjectSaver(cloudStorageWrapper: GoogleCloudStorageWrapper): ProjectSaver =
    CloudProjectSaverImpl(cloudStorageWrapper)

class CloudProjectSaverImpl(
    private val cloudStorageWrapper: GoogleCloudStorageWrapper,
    private val ioDispatcher: CoroutineDispatcher =
        ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER) {
            Dispatchers.IO
        },
) : ProjectSaver {
    override suspend fun saveProjectYaml(
        userId: String,
        projectId: String,
        yamlContent: String,
        syncWithCloud: Boolean,
    ) = withContext(ioDispatcher) {
        if (userId != ANONYMOUSE_USER_ID && syncWithCloud) {
            cloudStorageWrapper
                .uploadFile(
                    userId = userId,
                    projectId = projectId,
                    fileName = PROJECT_YAML_FILE_NAME,
                    content = yamlContent,
                ).mapBoth(
                    success = {
                        // Do nothing
                    },
                    failure = {
                        Logger.e("Failed to save the project yaml", it)
                    },
                )
        }
    }

    override suspend fun deleteProject(
        userId: String,
        projectId: String,
    ) = withContext(ioDispatcher) {
        if (userId != ANONYMOUSE_USER_ID) {
            cloudStorageWrapper.deleteFolder("$userId/$projectId/").mapBoth(
                success = {
                    // Do nothing
                },
                failure = {
                    Logger.e("Failed to delete the project yaml", it)
                },
            )
        }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    override suspend fun loadProject(
        userId: String,
        projectId: String,
    ): ProjectYamlNameWithLastModified? =
        if (userId != ANONYMOUSE_USER_ID) {
            withContext(ioDispatcher) {
                cloudStorageWrapper.getFile("$userId/$projectId/$PROJECT_YAML_FILE_NAME").mapBoth(
                    success = {
                        it.contentBytes?.let { contentBytes ->
                            ProjectYamlNameWithLastModified(
                                contentBytes.toString(Charsets.UTF_8),
                                it.updateTime,
                            )
                        }
                    },
                    failure = {
                        Logger.e("Failed to load project", it)
                        null
                    },
                )
            }
        } else {
            null
        }

    override suspend fun loadProjectIdList(userId: String): List<String> =
        if (userId != ANONYMOUSE_USER_ID) {
            withContext(ioDispatcher) {
                cloudStorageWrapper.listFile(userId).mapBoth(
                    success = { blobList ->
                        blobList.map {
                            it.folderName
                        }
                    },
                    failure = {
                        Logger.e("Failed to load projectId list", it)
                        emptyList()
                    },
                )
            }
        } else {
            emptyList()
        }

    override suspend fun deleteCacheProjects() {
        // Intentionally do nothing
    }
}
