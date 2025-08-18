package io.composeflow.platform

import io.composeflow.cloud.storage.GoogleCloudStorageWrapper
import io.composeflow.datastore.ProjectSaver
import io.composeflow.datastore.ProjectYamlNameWithLastModified

actual fun createCloudProjectSaver(cloudStorageWrapper: GoogleCloudStorageWrapper): ProjectSaver {
    // Cloud project saving is not supported in WASM environment
    return object : ProjectSaver {
        override suspend fun saveProjectYaml(userId: String, projectId: String, yamlContent: String, syncWithCloud: Boolean) {
            // No-op for WASM
        }

        override suspend fun loadProject(userId: String, projectId: String): ProjectYamlNameWithLastModified? {
            return null
        }

        override suspend fun deleteProject(userId: String, projectId: String) {
            // No-op for WASM
        }

        override suspend fun loadProjectIdList(userId: String): List<String> {
            return emptyList()
        }

        override suspend fun deleteCacheProjects() {
            // No-op for WASM
        }
    }
}